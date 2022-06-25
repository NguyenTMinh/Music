package com.minhntn.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.Settings;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.navigation.NavigationView;
import com.minhntn.music.database.MusicDBHelper;
import com.minhntn.music.frag.AllSongsFragment;
import com.minhntn.music.frag.FavoriteSongsFragment;
import com.minhntn.music.frag.MediaPlaybackFragment;
import com.minhntn.music.interf.ICommunicate;
import com.minhntn.music.interf.IDoInAsyncTask;
import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;
import com.minhntn.music.prov.MusicContacts;
import com.minhntn.music.serv.MediaPlaybackService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ActivityMusic extends AppCompatActivity implements ICommunicate {
    public static final String KEY_IS_LAND = "KEY_IS_LAND";
    public static final String KEY_INDEX_CURRENT = "KEY_INDEX_CURRENT";
    public static final String KEY_LIST_SONG = "KEY_LIST_SONG";
    public static final String KEY_FAV_SONG = "KEY_FAV_SONG";
    public static final String KEY_MUSIC_PLAYING = "KEY_MUSIC_PLAYING";
    public static final String KEY_SCREEN_ROTATE = "KEY_SCREEN_ROTATE";
    public static final String KEY_CHECK_INDEX = "KEY_CHECK_INDEX";
    public static final int UPDATE_FROM_FRAG = -2;
    private static final String KEY_APP_STARTED = "KEY_APP_STARTED";

    private static final int REQUEST_CODE = 1;
    private List<Song> mListSong;
    private List<Song> mFavListSong;
    private List<Album> mListAlbum;
    private MusicDBHelper mMusicDBHelper;
    private AllSongsFragment mAllSongsFragment;
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private FavoriteSongsFragment mFavoriteSongsFragment;
    private boolean mIsLand;
    private int mIndexCurrentSong = -1;
    private MediaPlaybackService mService;
    private boolean mIsServiceBound;
    private boolean mIsPlaying;
    private SharedPreferences mSharedPreferences;
    private boolean mIsRotated = false; // Check if the screen was rotated so app will reset mediaPlayer or not, if true -> no reset else -> reset
    private int mCurrentPlayMode;
    private Intent intent;
    private boolean mServiceAlive;
    private boolean mIsFromPause;
    // Condition to update data when start app but not when the callback is called (prevent app from reset list song after screen rotate)
    private boolean mIsAppStarted;
    private int mCheckIndex;

    // Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    // Media Controller views
    private View mNowPlayingView;
    private ToggleButton mTBPlaySongBottom;

    private final IDoInAsyncTask mIDoInAsyncTask = new IDoInAsyncTask() {
        @Override
        public void doInBackground() {
            mMusicDBHelper.loadDataFromMedia();
        }

        @Override
        public void onPostExecute() {
            mListSong = mMusicDBHelper.getAllSongs();
            mListAlbum = mMusicDBHelper.getAllAlbums();
            mAllSongsFragment.notifyAdapter(mListSong);

            if (mListSong.size() > 0) {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    intent.putParcelableArrayListExtra(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mFavListSong);
                } else {
                    intent.putParcelableArrayListExtra(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mListSong);
                }
                bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlaybackService.MediaBinder binder = (MediaPlaybackService.MediaBinder) service;
            mService = binder.getService();
            mService.setICommunicate(ActivityMusic.this);
            if (!mIsRotated && mService.getCurrentTimeSong() <= 0 && !mServiceAlive && mIndexCurrentSong != -1) {
                mService.setMediaUriSource(mIndexCurrentSong);
            }

            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null) {
                mService.setSongList(mFavListSong);
            } else {
                mService.setSongList(mListSong);
            }
            mService.setCurrentModePlay(mCurrentPlayMode);
            mIsRotated = true;
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init values
        intent = new Intent(ActivityMusic.this, MediaPlaybackService.class);
        mSharedPreferences = getSharedPreferences(MusicContacts.SHARED_PREF_NAME, MODE_PRIVATE);
        mMusicDBHelper = new MusicDBHelper(this);

        checkAppPermission();
        mFavListSong = new ArrayList<>();

        if (savedInstanceState != null) {
            mListSong = savedInstanceState.getParcelableArrayList(KEY_LIST_SONG);
            mFavListSong = savedInstanceState.getParcelableArrayList(KEY_FAV_SONG);
            mIndexCurrentSong = savedInstanceState.getInt(KEY_INDEX_CURRENT);
            mIsPlaying = savedInstanceState.getBoolean(KEY_MUSIC_PLAYING, false);
            mIsRotated = savedInstanceState.getBoolean(KEY_SCREEN_ROTATE, false);
            mCurrentPlayMode = savedInstanceState.getInt(MusicContacts.PREF_SONG_PLAY_MODE,
                    MediaPlaybackFragment.PLAY_MODE_DEFAULT);
            mServiceAlive = savedInstanceState.getBoolean(MusicContacts.PREF_SERVICE_ALIVE, false);
            mIsAppStarted = savedInstanceState.getBoolean(KEY_APP_STARTED, false);
            mCheckIndex = savedInstanceState.getInt(KEY_CHECK_INDEX, -1);
        } else {
            mListSong = mMusicDBHelper.getAllSongs();
            mIndexCurrentSong = mSharedPreferences.getInt(MusicContacts.PREF_SONG_CURRENT, -1);
            mCurrentPlayMode = mSharedPreferences.getInt(MusicContacts.PREF_SONG_PLAY_MODE,
                    MediaPlaybackFragment.PLAY_MODE_DEFAULT);
            mServiceAlive = mSharedPreferences.getBoolean(MusicContacts.PREF_SERVICE_ALIVE, false);
            mIsPlaying = mSharedPreferences.getBoolean(MusicContacts.PREF_MUSIC_PLAYING, false);
        }
        mListAlbum = mMusicDBHelper.getAllAlbums();

        mIsLand = getResources().getBoolean(R.bool.is_land);

        // Get view from layout
        if (!mIsLand) {
            mNowPlayingView = findViewById(R.id.layout_main_portrait).findViewById(R.id.v_now_playing);
            mNowPlayingView.setOnClickListener(v -> {
                transition(mIndexCurrentSong);
            });
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_IS_LAND, mIsLand);
        bundle.putBoolean(KEY_MUSIC_PLAYING, mIsPlaying);
        bundle.putInt(MusicContacts.PREF_SONG_PLAY_MODE, mCurrentPlayMode);
        bundle.putBoolean(MusicContacts.PREF_SERVICE_ALIVE, mServiceAlive);

        // Get the instance exists of the fragment
        mAllSongsFragment = (AllSongsFragment)
                getSupportFragmentManager().findFragmentByTag(AllSongsFragment.FRAGMENT_TAG);
        mMediaPlaybackFragment = (MediaPlaybackFragment)
                getSupportFragmentManager().findFragmentByTag(MediaPlaybackFragment.FRAGMENT_TAG);
        mFavoriteSongsFragment = (FavoriteSongsFragment)
                getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG);
        if (mAllSongsFragment == null) {
            mAllSongsFragment = new AllSongsFragment();
        } else {
            getSupportFragmentManager().beginTransaction().remove(mAllSongsFragment).commit();
            mAllSongsFragment = (AllSongsFragment) recreateFragment(mAllSongsFragment);
        }

        if (mMediaPlaybackFragment == null) {
            mMediaPlaybackFragment = new MediaPlaybackFragment();
        } else {
            getSupportFragmentManager().beginTransaction().remove(mMediaPlaybackFragment)
                    .commit();
            mMediaPlaybackFragment = (MediaPlaybackFragment) recreateFragment(mMediaPlaybackFragment);
        }

        if (mFavoriteSongsFragment == null) {
            mFavoriteSongsFragment = new FavoriteSongsFragment();
        } else {
            getSupportFragmentManager().beginTransaction().remove(mFavoriteSongsFragment)
                    .commit();
            mFavoriteSongsFragment = (FavoriteSongsFragment) recreateFragment(mFavoriteSongsFragment);
        }

        // Set the data needed to fragment
        mAllSongsFragment.setListSong(mListSong);
        mAllSongsFragment.setArguments(bundle);

        mFavoriteSongsFragment.setListSong(mFavListSong);
        mFavoriteSongsFragment.setArguments(bundle);

        mMediaPlaybackFragment.setArguments(bundle);

        if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null) {
            mFavoriteSongsFragment.setAdapterIndex(mIndexCurrentSong);
        } else {
            mAllSongsFragment.setAdapterIndex(mIndexCurrentSong);
        }

        // Check the orientation of device so then app can behave correctly
        if (mIsLand) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSupportActionBar().show();
                }
            }, 100);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mMediaPlaybackFragment,
                    MediaPlaybackFragment.FRAGMENT_TAG).commit();
            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_1, mFavoriteSongsFragment,
                        FavoriteSongsFragment.FRAGMENT_TAG).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_1, mAllSongsFragment,
                        AllSongsFragment.FRAGMENT_TAG).commit();
            }
            if (savedInstanceState != null) {
                if (mIndexCurrentSong != -1) {
                    transition(mIndexCurrentSong);
                }
            }
        } else {
            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mAllSongsFragment,
                        AllSongsFragment.FRAGMENT_TAG).commit();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mFavoriteSongsFragment,
                        FavoriteSongsFragment.FRAGMENT_TAG).addToBackStack(null).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mAllSongsFragment,
                        AllSongsFragment.FRAGMENT_TAG).commit();
            }
        }

        // bind service to this activity
        intent.putParcelableArrayListExtra(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mListSong);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);

        // Setup navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_drawer);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_all_songs: {
                        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                            getSupportFragmentManager().popBackStack();

                            if (mFavoriteSongsFragment != null) {
                                try {
                                    mIndexCurrentSong = mAllSongsFragment.setAdapterIndex(getPositionFromID(mService.getExactSong().getID(),
                                            AllSongsFragment.FRAGMENT_TAG));
                                } catch (Exception e) {
                                    mIndexCurrentSong = mAllSongsFragment.setAdapterIndex(getPositionFromID(mFavoriteSongsFragment.getIDFromSongOnList(),
                                            AllSongsFragment.FRAGMENT_TAG));
                                }
                            }
                            if (mService != null) {
                                mService.setSongList(mListSong);
                            }
                        }
                        break;
                    }
                    case R.id.action_fav_songs: {
                        if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) == null) {
                            mFavListSong.clear();
                            for (Song song: mListSong) {
                                if (song.isFavorite()) {
                                    mFavListSong.add(song);
                                }
                            }
                            mFavoriteSongsFragment = new FavoriteSongsFragment();
                            mFavoriteSongsFragment.setListSong(mFavListSong);
                            mFavoriteSongsFragment.setArguments(bundle);

                            // Check if the favorite list in favFrag have the song that is currently played
                            mCheckIndex = mFavoriteSongsFragment.setAdapterIndex(getPositionFromID(mAllSongsFragment.getIDFromSongOnList(),
                                    FavoriteSongsFragment.FRAGMENT_TAG));

                            mIndexCurrentSong = (mCheckIndex == -1)? mIndexCurrentSong: mCheckIndex;
                            if (mIndexCurrentSong > mFavListSong.size()) {
                                mService.setIsSongPlayInList(false);
                            }


                            if (mService != null) {
                                mService.setSongList(mFavListSong);
                                if (mService.getCurrentTimeSong() > 0) {
                                    mService.setIsSongPlayInList(false);
                                }
                                if ( mCheckIndex != -1) {
                                    mService.setCurrentSongIndex(mCheckIndex);
                                    mService.setIsSongPlayInList(true);
                                }
                            }

                            if (mIsLand) {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container_1, mFavoriteSongsFragment, FavoriteSongsFragment.FRAGMENT_TAG)
                                        .addToBackStack(null)
                                        .commit();
                            } else {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, mFavoriteSongsFragment, FavoriteSongsFragment.FRAGMENT_TAG)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }

                    }
                }
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.close();
                }
                return true;
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("MinhNTn", "onCreate: " + mFavListSong.size());
    }

    private void checkUpdateDatabase() {
        new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mIDoInAsyncTask);
    }

    private void checkAppPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }

        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permission = Environment.isExternalStorageManager();
        } else {
            int res_1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int res_2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            permission = res_1 == PackageManager.PERMISSION_GRANTED && res_2 == PackageManager.PERMISSION_GRANTED;
        }

        if (!permission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        REQUEST_CODE);;
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new MyAsyncTask().execute(mIDoInAsyncTask);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
        }
        if (mIsLand) {
            super.onBackPressed();
        } else {
            if (mMediaPlaybackFragment != null) {
                mMediaPlaybackFragment.resetListener();
            }
            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null
                    && getSupportFragmentManager().getBackStackEntryCount() < 2) {
                finish();
            } else {
                super.onBackPressed();
                mNowPlayingView.setVisibility(View.VISIBLE);
                mAllSongsFragment.setButtonState(mIsPlaying);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (!isAppRunning()) {
            unbindService(mServiceConnection);
        }

        if (!mService.isMediaPlaying() && !isAppRunning()) {
            stopService(intent);
            mServiceAlive = false;
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(MusicContacts.PREF_SERVICE_ALIVE, mServiceAlive);
        editor.putBoolean(MusicContacts.PREF_MUSIC_PLAYING, mService.isMediaPlaying());
        editor.apply();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mService != null) {
            mIsPlaying = mService.isMediaPlaying();
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(MusicContacts.PREF_SONG_PLAY_MODE, mCurrentPlayMode);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIndexCurrentSong != -1) {
            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) == null) {
                onResumeFromBackScreen();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Load data again if there is a change in database
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsAppStarted) {
                    checkUpdateDatabase();
                    mIsAppStarted = true;
                }
            }
        }, 100);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mListSong);
        outState.putParcelableArrayList(KEY_FAV_SONG, (ArrayList<? extends Parcelable>) mFavListSong);
        outState.putInt(KEY_INDEX_CURRENT, mIndexCurrentSong);
        outState.putBoolean(KEY_MUSIC_PLAYING, mIsPlaying);
        outState.putBoolean(KEY_SCREEN_ROTATE, mIsRotated);
        outState.putInt(MusicContacts.PREF_SONG_PLAY_MODE, mCurrentPlayMode);
        outState.putBoolean(MusicContacts.PREF_SERVICE_ALIVE, mServiceAlive);
        outState.putBoolean(KEY_APP_STARTED, mIsAppStarted);
        outState.putInt(KEY_CHECK_INDEX, mCheckIndex);
    }

    /* Recreate with the existed fragment, so that it can move from a different container to another container
    without create new instance */
    private Fragment recreateFragment(Fragment f) {
        try {
            Fragment.SavedState savedState = getSupportFragmentManager().saveFragmentInstanceState(f);

            Fragment newInstance = f.getClass().newInstance();
            newInstance.setInitialSavedState(savedState);

            return newInstance;
        } catch (Exception e) // InstantiationException, IllegalAccessException
        {
            throw new RuntimeException("Cannot reinstantiate fragment " + f.getClass().getName(), e);
        }
    }

    /**
     * Check if this app is killed or still runs in background (or foreground)
     * @return
     */
    private boolean isAppRunning() {
        ActivityManager m = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList =  m.getRunningTasks(10);
        Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();
        int n=0;
        while(itr.hasNext()){
            n++;
            itr.next();
        }
        if(n==1){ // App is killed
            return false;
        }
        return true; // App is in background or foreground
    }

    public void onResumeFromBackScreen() {
        mIsFromPause = true;
        displayControlMedia(mIndexCurrentSong, true, AllSongsFragment.FRAGMENT_TAG);
        mAllSongsFragment.onResumeFromScreen(mIndexCurrentSong);
    }

    private Bundle getArgumentsSetToFrag() {
        Bundle bundle = new Bundle();
        Song song;

        try {
            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null && mService.isSongPlayInList()) {
                song = mFavListSong.get(mIndexCurrentSong);
            } else {
                song = mListSong.get(mIndexCurrentSong);
            }
        } catch (NullPointerException e) {
            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null && mCheckIndex != -1) {
                song = mFavListSong.get(mIndexCurrentSong);
            } else {
                song = mListSong.get(mIndexCurrentSong);
            }
        }

        byte[] cover = new byte[1];
        String albumName = "";
        for (int i = 0; i < mListAlbum.size(); i++) {
            if (song.getAlbumID() == mListAlbum.get(i).getID()) {
                cover = mListAlbum.get(i).getArt();
                albumName = mListAlbum.get(i).getName();
                break;
            }
        }

        bundle.putByteArray(MediaPlaybackFragment.KEY_COVER, cover);
        bundle.putString(MediaPlaybackFragment.KEY_ALBUM_NAME, albumName);
        bundle.putBoolean(KEY_IS_LAND, mIsLand);
        bundle.putBoolean(KEY_MUSIC_PLAYING, mIsPlaying);
        bundle.putInt(MusicContacts.PREF_SONG_PLAY_MODE, mCurrentPlayMode);
        bundle.putBoolean(MusicContacts.PREF_SERVICE_ALIVE, mServiceAlive);
        return bundle;
    }

    public void setCurrentPlayMode(int mode) {
        mCurrentPlayMode = mode;
    }

    /**
     * Return the position in current list when know id of song
     * @param id: the id of song
     * @param forFrag: key to know what list should be used
     * @return position in that list
     */
    private int getPositionFromID(int id, String forFrag) {
        if (forFrag.equals(FavoriteSongsFragment.FRAGMENT_TAG)) {
            for (int i = 0; i < mFavListSong.size(); i++) {
                if (mFavListSong.get(i).getID() == id) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < mListSong.size(); i++) {
                if (mListSong.get(i).getID() == id) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * These override methods below is the method of ICommunicate interface
     */
    @Override
    public void transition(int position) {
        mIndexCurrentSong = position;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(MusicContacts.PREF_SONG_CURRENT, mIndexCurrentSong);
        editor.apply();

        Bundle bundle = getArgumentsSetToFrag();
        final Song song;
        Song song1;

        try {
            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null && mService.isSongPlayInList()) {
                song1 = mFavListSong.get(mIndexCurrentSong);
            } else {
                song1 = mListSong.get(mIndexCurrentSong);
            }
        } catch (NullPointerException e) {
            if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null && mCheckIndex != -1) {
                song1 = mFavListSong.get(mIndexCurrentSong);
            } else {
                song1 = mListSong.get(mIndexCurrentSong);
            }
        }

        song = song1;
        FragmentManager fragManager = getSupportFragmentManager();

        if (mIsLand) {
            mMediaPlaybackFragment.setArguments(bundle);
            mMediaPlaybackFragment.setCurrentSong(song);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMediaPlaybackFragment.onUpdateCurrentView(song);
                }
            }, 150);
        } else {
            hideActionBar();
            mNowPlayingView.setVisibility(View.GONE);
            mMediaPlaybackFragment.setArguments(bundle);
            FragmentTransaction transaction = fragManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.slowly_disappear, 0, R.anim.exit_to_bottom)
                    .replace(R.id.fragment_container, mMediaPlaybackFragment, MediaPlaybackFragment.FRAGMENT_TAG)
                    .addToBackStack("").commit();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMediaPlaybackFragment.onUpdateCurrentView(song);
                }
            }, 150);
        }
    }

    @Override
    public void hideActionBar() {
        if (Objects.requireNonNull(getSupportActionBar()).isShowing()) {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void passCurrentPositionIfPortrait(int position) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(MusicContacts.PREF_SONG_CURRENT, mIndexCurrentSong);
        editor.apply();
        if (mMediaPlaybackFragment != null) {
            if (mMediaPlaybackFragment.getContext() != null) {
                Bundle bundle = getArgumentsSetToFrag();
                mMediaPlaybackFragment.setArguments(bundle);
                final Song song;
                if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null && mService.isSongPlayInList()) {
                    song = mFavListSong.get(mIndexCurrentSong);
                } else {
                    song = mListSong.get(mIndexCurrentSong);
                }
                mMediaPlaybackFragment.setCurrentSong(song);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMediaPlaybackFragment.onUpdateCurrentView(song);
                    }
                }, 150);
            }
        }
    }

    @Override
    public void playMusic(int position) {
        if (mService != null) {
            mService.playSong(position);
        }
    }

    @Override
    public void pauseMusic(boolean fromService) {
        mIsPlaying = false;
        if (mService != null) {
            mService.pauseSong();
        }
        if (mAllSongsFragment != null && fromService) {
            setPauseButton(true);
        }
        if (mTBPlaySongBottom != null) {
            mTBPlaySongBottom.setChecked(true);
        }
    }

    @Override
    public void resumeMusic(boolean fromService) {
        if (mIndexCurrentSong != -1) {
            if (!mServiceAlive) {
                if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null && mCheckIndex != -1) {
                    intent.putParcelableArrayListExtra(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mFavListSong);
                } else {
                    intent.putParcelableArrayListExtra(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mListSong);
                }
                startService(intent);
                mServiceAlive = true;
            }
            if (mService != null) {
                mService.resumeSong();
            }
            if (mAllSongsFragment != null && fromService) {
                setPauseButton(false);
            }

            if (mTBPlaySongBottom != null) {
                mTBPlaySongBottom.setChecked(false);
            }
        }
    }

    @Override
    public int getTimeCurrentPlay() {
        if (mService != null) {
            return mService.getCurrentTimeSong();
        }
        return 0;
    }

    @Override
    public void startService() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            intent.putParcelableArrayListExtra(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mFavListSong);
        } else {
            intent.putParcelableArrayListExtra(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mListSong);
        }
        startService(intent);
        mServiceAlive = true;
    }

    @Override
    public void seekTimeTo(int time) {
        if (mService != null) {
            mService.seekPlayTimeTo(time);
        }
    }

    @Override
    public boolean isMusicPlaying() {
        if (mService != null) {
            return mService.isMediaPlaying();
        }
        return mIsPlaying;
    }

    @Override
    public void playNextSong() {
        if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null) {
            mFavoriteSongsFragment.nextSong();
        } else {
            mAllSongsFragment.nextSong();
        }
        if (getSupportFragmentManager().findFragmentByTag(MediaPlaybackFragment.FRAGMENT_TAG) != null) {
            if (mNowPlayingView != null) {
                mNowPlayingView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void playPreviousSong() {
        if (mService != null) {
            if (mService.getCurrentTimeSong() > 3000) {
                mService.seekPlayTimeTo(0);
            } else {
                if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null) {
                    mFavoriteSongsFragment.previousSong();
                } else {
                    mAllSongsFragment.previousSong();
                }
                if (getSupportFragmentManager().findFragmentByTag(MediaPlaybackFragment.FRAGMENT_TAG) != null) {
                    if (mNowPlayingView != null) {
                        mNowPlayingView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @Override
    public void playRandom() {
        if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null) {
            mFavoriteSongsFragment.randomSong();
        } else {
            mAllSongsFragment.randomSong();
        }
        if (getSupportFragmentManager().findFragmentByTag(MediaPlaybackFragment.FRAGMENT_TAG) != null) {
            if (mNowPlayingView != null) {
                mNowPlayingView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void playRepeatOneSong() {
        if (mService != null && mMediaPlaybackFragment.getContext() != null) {
            mMediaPlaybackFragment.setCountdownTimer(mListSong.get(mIndexCurrentSong).getDuration() - mService.getCurrentTimeSong());
        }
    }

    @Override
    public void setStatePlaying(boolean state) {
        mIsPlaying = state;
    }

    @Override
    public void setModePlay(int modePlay) {
        if (mService != null) {
            mService.setCurrentModePlay(modePlay);
        }
    }

    @Override
    public void setPauseButton(boolean state) {
        if (mMediaPlaybackFragment.getContext() != null) {
            mMediaPlaybackFragment.setCheckedButton(state);
        }
        if (mAllSongsFragment.getContext() != null) {
            mAllSongsFragment.setButtonState(!state);
        }
    }

    @Override
    public void displayControlMedia(int position, boolean isClicked, String fromFrag) {
        mIndexCurrentSong = position;
        if (!mIsLand) {
            if (mIndexCurrentSong != -1) {
                Song currentSong;
                if (fromFrag.equals(AllSongsFragment.FRAGMENT_TAG)) {
                    currentSong = mListSong.get(mIndexCurrentSong);
                } else {
                    currentSong = mFavListSong.get(mIndexCurrentSong);
                    if (mAllSongsFragment.getCurrentIndexSong() != -1) {
                        if (mListSong.get(mAllSongsFragment.getCurrentIndexSong()).getID()
                                != currentSong.getID()) {
                            mListSong.get(mAllSongsFragment.getCurrentIndexSong()).setPlaying(false);
                        }
                    }
                }
                int lengthAllow = getResources().getInteger(R.integer.length_in_line);
                if (mNowPlayingView != null) {
                    mNowPlayingView.setVisibility(View.VISIBLE);
                    TextView name = mNowPlayingView.findViewById(R.id.tv_song_name_now_playing);
                    if (mTBPlaySongBottom == null) {
                        mTBPlaySongBottom = mNowPlayingView.findViewById(R.id.toggle_play_pause);
                    }
                    mTBPlaySongBottom.forceLayout();

                    mTBPlaySongBottom.setChecked(!mIsPlaying);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isClicked) {
                                mTBPlaySongBottom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            pauseMusic(false);
                                        } else {
                                            if (!mServiceAlive) {
                                                startService();
                                                mServiceAlive = true;
                                            }
                                            resumeMusic(false);
                                        }
                                    }
                                });
                            }
                        }
                    }, 100);

                    String nameDisplay = (currentSong.getTitle().length() < lengthAllow)? currentSong.getTitle()
                            : currentSong.getTitle().substring(0, lengthAllow - 3) + "..";
                    name.setText(nameDisplay);
                    new DBAsyncTask().execute(currentSong.getID());
                }
            }
            passCurrentPositionIfPortrait(position);
        } else {
            if (fromFrag.equals(FavoriteSongsFragment.FRAGMENT_TAG)) {
                Song currentSong = mFavListSong.get(mIndexCurrentSong);
                if (mAllSongsFragment.getCurrentIndexSong() != -1) {
                    if (mListSong.get(mAllSongsFragment.getCurrentIndexSong()).getID()
                            != currentSong.getID()) {
                        mListSong.get(mAllSongsFragment.getCurrentIndexSong()).setPlaying(false);
                    }
                }
                if (mService != null) {
                    mService.setIsSongPlayInList(true);
                }
            }
            transition(mIndexCurrentSong);
        }

        // start playing music
        if (mIndexCurrentSong != -1) {
            if (!mIsFromPause) {
                if (!mServiceAlive) {
                    startService();
                    mServiceAlive = true;
                    mIsPlaying = true;
                }
                playMusic(mIndexCurrentSong);
            }
        }
    }

    @Override
    public void setIsFromPause(boolean fromPause) {
        mIsFromPause = fromPause;
    }

    @Override
    public boolean isSongOnList() {
        if (mService != null) {
            return mService.isSongPlayInList();
        }
        return false;
    }

    @Override
    public void setSongOnList(boolean onList) {
        if (mService != null) {
            mService.setIsSongPlayInList(onList);
        }
    }

    @Override
    public void updateOnLikeButton(int id, boolean isChecked, int index, boolean active) {
        if (active) {
            ContentValues values = new ContentValues();
            Uri newUri = Uri.parse(MusicContacts.CONTENT_URI.toString() + "/" + id);
            Song song = null;
            if (index == UPDATE_FROM_FRAG) {
                if (getSupportFragmentManager().findFragmentByTag(FavoriteSongsFragment.FRAGMENT_TAG) != null && mService.isSongPlayInList()) {
                    song = mFavListSong.get(mIndexCurrentSong);
                } else {
                    song = mListSong.get(mIndexCurrentSong);
                }
            } else {
                for (Song song1: mListSong) {
                    if (song1.getID() == id) {
                        song = song1;
                        break;
                    }
                }
                if (song == null) {
                    song = new Song();
                }
            }

            if (isChecked) {
                song.setFavLevel(2);
                song.setIsFavorite(true);
                song.setDislike(false);
                if (mFavoriteSongsFragment != null) {
                    if (!mFavListSong.contains(song)) {
                        mFavListSong.add(song);
                        mFavoriteSongsFragment.setAdapterIndex(mFavListSong.size() -1);
                        mFavoriteSongsFragment.notifyAddFavSong();
                    }
                }
                if (mService != null) {
                    mService.setIsSongPlayInList(true);
                }
            } else {
                song.setFavLevel(0);
                song.setIsFavorite(false);
                if (mFavoriteSongsFragment != null) {
                    mCheckIndex =
                            getPositionFromID(mFavoriteSongsFragment.getIDFromSongNotOnList(), AllSongsFragment.FRAGMENT_TAG);
                    mIndexCurrentSong = (mCheckIndex == -1)? mIndexCurrentSong: mCheckIndex;
                }
                if (mFavListSong != null) {
                    mFavListSong.remove(song);
                }
                mService.setIsSongPlayInList(false);
                if (mFavoriteSongsFragment != null) {
                    mFavoriteSongsFragment.notifyAdapter();
                    mFavoriteSongsFragment.setCurrentIndexSong(-1);
                    mFavoriteSongsFragment.resetAdapterIndex();
                }
            }

            values.put(MusicContacts.FAVORITE_COLUMN_IS_FAVORITE, song.getFavLevel());
            int row = getContentResolver().update(newUri, values, null, null);
            if (row > 0) {
                mMusicDBHelper.updateSong(song);
            }
        }
    }

    /**
     * update count of play times and also update favorite if condition required meets
     * @param id: id of song need to be updated
     */
    @Override
    public void updateCountPlay(int id) {
        ContentValues values = new ContentValues();
        Uri newUri = Uri.parse(MusicContacts.CONTENT_URI.toString() + "/" + id);
        Song song = mListSong.get(mIndexCurrentSong);

        values.put(MusicContacts.FAVORITE_COLUMN_COUNT_OF_PLAY, song.getCountOfPlay());
        if (song.getCountOfPlay() >= 3) {
            if (!song.isDislike()) {
                song.setFavLevel(2);
                song.setIsFavorite(true);
            }
        }
        values.put(MusicContacts.FAVORITE_COLUMN_IS_FAVORITE, song.getFavLevel());
        int row = getContentResolver().update(newUri, values, null, null);

        if (row > 0) {
            if (song.getCountOfPlay() >= 3) {
                mMusicDBHelper.updateSong(song);
            }
        }
    }

    @Override
    public void updateOnDislikeButton(int id, boolean isChecked, boolean active) {
        if (active) {
            ContentValues values = new ContentValues();
            Uri newUri = Uri.parse(MusicContacts.CONTENT_URI.toString() + "/" + id);
            Song song = null;
            for (Song song1: mListSong) {
                if (song1.getID() == id) {
                    song = song1;
                }
            }
            if (song == null) {
                song = new Song();
            }

            if (isChecked) {
                song.setDislike(true);
                song.setFavLevel(1);
                song.setIsFavorite(false);
                if (mFavoriteSongsFragment != null) {
                    mFavoriteSongsFragment.setSongOnList();
                    mCheckIndex =
                            getPositionFromID(mFavoriteSongsFragment.getIDFromSongNotOnList(), AllSongsFragment.FRAGMENT_TAG);
                    mIndexCurrentSong = (mCheckIndex == -1)? mIndexCurrentSong: mCheckIndex;
                }
                mFavListSong.remove(song);
                if (mService != null) {
                    mService.setIsSongPlayInList(false);
                }
                if (mFavoriteSongsFragment != null) {
                    mFavoriteSongsFragment.notifyAdapter();
                    mFavoriteSongsFragment.setCurrentIndexSong(-1);
                    mFavoriteSongsFragment.resetAdapterIndex();
                }
            } else {
                song.setFavLevel(0);
                song.setDislike(false);
            }

            values.put(MusicContacts.FAVORITE_COLUMN_IS_FAVORITE, song.getFavLevel());
            int row = getContentResolver().update(newUri, values, null, null);
            if (row > 0) {
                mMusicDBHelper.updateSong(song);
            }
        }
    }

    @Override
    public void removeFavorite(int id) {
        ContentValues values = new ContentValues();
        Uri newUri = Uri.parse(MusicContacts.CONTENT_URI.toString() + "/" + id);
        Song song = null;

        for (int i = 0; i < mFavListSong.size(); i++) {
            if (mFavListSong.get(i).getID() == id) {
                song = mFavListSong.get(i);
                // Update the check song on list
                if (mService != null) {
                    if (mService.isSongPlayInList()) {
                        if (mFavListSong.get(mIndexCurrentSong).getID() == song.getID()) {
                            mService.setIsSongPlayInList(false);
                            if (mFavoriteSongsFragment != null) {
                                mFavoriteSongsFragment.setCurrentIndexSong(-1);
                                mIndexCurrentSong =
                                        getPositionFromID(mFavoriteSongsFragment.getIDFromSongNotOnList(), AllSongsFragment.FRAGMENT_TAG);
                            }

                        } else {
                            if (mIndexCurrentSong > i) {
                                mIndexCurrentSong--;
                            }
                        }
                    }
                }
                break;
            }
        }
        // Update button state
        if (mMediaPlaybackFragment != null) {
            if (mMediaPlaybackFragment.getContext() != null) {
                if (mMediaPlaybackFragment.getCurrentSong().getID() == song.getID()) {
                    mMediaPlaybackFragment.setCheckedLikeButton(false);
                }
            }
        }
        // Update adapter
        if (song != null) {
            song.resetCountOfPlay();
            song.setIsFavorite(false);
            song.setFavLevel(0);
            values.put(MusicContacts.FAVORITE_COLUMN_IS_FAVORITE, song.getFavLevel());
            values.put(MusicContacts.FAVORITE_COLUMN_COUNT_OF_PLAY, song.getCountOfPlay());
            int row = getContentResolver().update(newUri, values, null, null);
            if (row > 0) {
                mMusicDBHelper.updateSong(song);
            }

            mFavListSong.remove(song);
            mFavoriteSongsFragment.notifyAdapter();
        }
    }

    @Override
    public void removeFromDatabase(int id) {
        Uri newUri = Uri.parse(MusicContacts.CONTENT_URI.toString() + "/" + id);
        String whereClause = MusicContacts.SONG_COLUMN_ID + " = ?";
        String[] selectionArgs = new String[] {String.valueOf(id)};
        Song song = null;

        for (Song song1: mListSong) {
            if (song1.getID() == id) {
                song = song1;
                break;
            }
        }
        if (song != null) {
            int row = getContentResolver().delete(newUri, null, null);
            if (row > 0) {
                mMusicDBHelper.deleteSong(whereClause, selectionArgs, song.getUri());
            }

            mListSong.remove(song);
            mFavListSong.remove(song);
            mAllSongsFragment.notifyAdapter(mListSong);
        }
    }

    class DBAsyncTask extends AsyncTask<Integer, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Integer... integers) {
            return mMusicDBHelper.getInfoNowPlayingSong(integers[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            TextView album = mNowPlayingView.findViewById(R.id.tv_song_album_now_playing);
            ImageView cover = mNowPlayingView.findViewById(R.id.iv_album_cover);

            cursor.moveToFirst();
            String albumName = cursor.getString(0);
            byte[] albumCover = cursor.getBlob(1);

            int lengthAllow = getResources().getInteger(R.integer.length_in_line);
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);
            String albumDisplay = (albumName.length() <= lengthAllow)? albumName : albumName.substring(0, lengthAllow - 3) + "..";

            cover.setImageBitmap(bitmap);
            album.setText(albumDisplay);
            mTBPlaySongBottom.setChecked(!mIsPlaying);
        }
    }
}