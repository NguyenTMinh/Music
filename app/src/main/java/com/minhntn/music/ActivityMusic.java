package com.minhntn.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageButton;

import com.minhntn.music.database.MusicDBHelper;
import com.minhntn.music.frag.AllSongsFragment;
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
import java.util.concurrent.Executor;

public class ActivityMusic extends AppCompatActivity implements ICommunicate {
    public static final String KEY_IS_LAND = "KEY_IS_LAND";
    public static final String KEY_INDEX_CURRENT = "KEY_INDEX_CURRENT";
    public static final String KEY_LIST_SONG = "KEY_LIST_SONG";
    public static final String KEY_LIST_ALBUM = "KEY_LIST_ALBUM";
    public static final String KEY_MUSIC_PLAYING = "KEY_MUSIC_PLAYING";
    public static final String KEY_SCREEN_ROTATE = "KEY_SCREEN_ROTATE";
    private static final String KEY_APP_STARTED = "KEY_APP_STARTED";

    private static final int REQUEST_CODE = 1;
    private List<Song> mListSong;
    private List<Album> mListAlbum;
    private MusicDBHelper mMusicDBHelper;
    private AllSongsFragment mAllSongsFragment;
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private MyBroadcastReceiver mBroadcastReceiver;
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
    // Condition to update data when start app but not when the callback is called (prevent app from reset list song after screen rotate)
    private boolean mIsAppStarted;

    private IDoInAsyncTask mIDoInAsyncTask = new IDoInAsyncTask() {
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
                intent.putParcelableArrayListExtra(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mListSong);
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

            mService.setSongList(mListSong);
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
        intent = new Intent(ActivityMusic.this, MediaPlaybackService.class);

        mSharedPreferences = getSharedPreferences(MusicContacts.SHARED_PREF_NAME, MODE_PRIVATE);
        mMusicDBHelper = new MusicDBHelper(this);

        checkAppPermission();

        if (savedInstanceState != null) {
            mListSong = savedInstanceState.getParcelableArrayList(KEY_LIST_SONG);
            mListAlbum = savedInstanceState.getParcelableArrayList(KEY_LIST_ALBUM);
            mIndexCurrentSong = savedInstanceState.getInt(KEY_INDEX_CURRENT);
            mIsPlaying = savedInstanceState.getBoolean(KEY_MUSIC_PLAYING, false);
            mIsRotated = savedInstanceState.getBoolean(KEY_SCREEN_ROTATE, false);
            mCurrentPlayMode = savedInstanceState.getInt(MusicContacts.PREF_SONG_PLAY_MODE,
                    MediaPlaybackFragment.PLAY_MODE_DEFAULT);
            mServiceAlive = savedInstanceState.getBoolean(MusicContacts.PREF_SERVICE_ALIVE, false);
            mIsAppStarted = savedInstanceState.getBoolean(KEY_APP_STARTED, false);
        } else {
            mListSong = mMusicDBHelper.getAllSongs();
            mListAlbum = mMusicDBHelper.getAllAlbums();
            mIndexCurrentSong = mSharedPreferences.getInt(MusicContacts.PREF_SONG_CURRENT, -1);
            mCurrentPlayMode = mSharedPreferences.getInt(MusicContacts.PREF_SONG_PLAY_MODE,
                    MediaPlaybackFragment.PLAY_MODE_DEFAULT);
            mServiceAlive = mSharedPreferences.getBoolean(MusicContacts.PREF_SERVICE_ALIVE, false);
            mIsPlaying = mSharedPreferences.getBoolean(MusicContacts.PREF_MUSIC_PLAYING, false);
        }

        mIsLand = getResources().getBoolean(R.bool.is_land);
        
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
        if (mAllSongsFragment == null) {
            mAllSongsFragment = new AllSongsFragment();
        } else {
            getSupportFragmentManager().beginTransaction().remove(mAllSongsFragment).commit();
            mAllSongsFragment = (AllSongsFragment) recreateFragment(mAllSongsFragment);
        }
        mAllSongsFragment.setListSong(mListSong);
        mAllSongsFragment.setAdapterIndex(mIndexCurrentSong);
        mAllSongsFragment.setArguments(bundle);

        if (mMediaPlaybackFragment == null) {
            mMediaPlaybackFragment = new MediaPlaybackFragment();
        } else {
            getSupportFragmentManager().beginTransaction().remove(mMediaPlaybackFragment)
                    .commit();
            mMediaPlaybackFragment = (MediaPlaybackFragment) recreateFragment(mMediaPlaybackFragment);
        }

        // Set the data needed to fragment
        mMediaPlaybackFragment.setArguments(bundle);

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
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_1, mAllSongsFragment,
                    AllSongsFragment.FRAGMENT_TAG).commit();
            if (savedInstanceState != null) {
                if (mIndexCurrentSong != -1) {
                    transition(mIndexCurrentSong);
                }
            }
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mAllSongsFragment,
                    AllSongsFragment.FRAGMENT_TAG).commit();
        }

        // Register broadcast so when the data is finished loading to database, the app will update the current list
        // And for notification
        mBroadcastReceiver = new MyBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyBroadcastReceiver.ACTION_LOAD_DONE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, intentFilter);


    }

    private void checkUpdateDatabase() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mIDoInAsyncTask);
        } else {
            new MyAsyncTask().execute(mIDoInAsyncTask);
        }
    }

    private void checkAppPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }
    }

    public MusicDBHelper getMusicDBHelper() {
        return mMusicDBHelper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
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
            super.onBackPressed();
            mAllSongsFragment.setButtonState(mService.isMediaPlaying());
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("MinhNTn", "onDestroy: ");
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mBroadcastReceiver);
        unbindService(mServiceConnection);
        if (!mService.isMediaPlaying() && !isAppRunning()) {
            Log.d("MinhNTn", "onDestroy: service");
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
            onResumeFromBackScreen();
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

    // delete this method later
    @Override
    public void doOnLoadDone() {
        Log.d("MinhNTn", "doOnLoadDone: ");
        mIDoInAsyncTask.onPostExecute();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mListSong);
        outState.putParcelableArrayList(KEY_LIST_ALBUM, (ArrayList<? extends Parcelable>) mListAlbum);
        outState.putInt(KEY_INDEX_CURRENT, mIndexCurrentSong);
        outState.putBoolean(KEY_MUSIC_PLAYING, mIsPlaying);
        outState.putBoolean(KEY_SCREEN_ROTATE, mIsRotated);
        outState.putInt(MusicContacts.PREF_SONG_PLAY_MODE, mCurrentPlayMode);
        outState.putBoolean(MusicContacts.PREF_SERVICE_ALIVE, mServiceAlive);
        outState.putBoolean(KEY_APP_STARTED, mIsAppStarted);
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
        mAllSongsFragment.onResumeFromScreen(mIndexCurrentSong);
    }

    private Bundle getArgumentsSetToFrag() {
        Bundle bundle = new Bundle();
        Song song = mListSong.get(mIndexCurrentSong);
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
     * These override methods below is the method of ICommunicate interface
     */
    @Override
    public void transition(int position) {
        mIndexCurrentSong = position;

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(MusicContacts.PREF_SONG_CURRENT, mIndexCurrentSong);
        editor.apply();

        Bundle bundle = getArgumentsSetToFrag();
        Song song = mListSong.get(mIndexCurrentSong);

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
        if (getSupportActionBar().isShowing()) {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void passCurrentPositionIfPortrait(int position) {
        mIndexCurrentSong = position;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(MusicContacts.PREF_SONG_CURRENT, mIndexCurrentSong);
        editor.apply();
        if (mMediaPlaybackFragment.getContext() != null) {
            Bundle bundle = getArgumentsSetToFrag();
            mMediaPlaybackFragment.setArguments(bundle);
            mMediaPlaybackFragment.setCurrentSong(mListSong.get(mIndexCurrentSong));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMediaPlaybackFragment.onUpdateCurrentView(mListSong.get(mIndexCurrentSong));
                }
            }, 150);
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
        Log.d("MinhNTn", "pauseMusic: ");
        if (mService != null) {
            if (mService.isMediaPlaying()) {
                mService.pauseSong();
            }
            mIsPlaying = false;
        }
        if (mAllSongsFragment != null && fromService) {
            setPauseButton(true);
        }
    }

    @Override
    public void resumeMusic(boolean fromService) {
        Log.d("MinhNTn", "resumeMusic: ");
        if (mIndexCurrentSong != -1) {
            if (!mServiceAlive && mIsPlaying) {
                startService(intent);
                mServiceAlive = true;
            }
            if (mService != null) {
                mService.resumeSong();
                mIsPlaying = true;
            }
            if (mAllSongsFragment != null && fromService) {
                setPauseButton(false);
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
        mAllSongsFragment.nextSong();
    }

    @Override
    public void playPreviousSong() {
        if (mService != null) {
            if (mService.getCurrentTimeSong() > 3000) {
                mService.seekPlayTimeTo(0);
            } else {
                mAllSongsFragment.previousSong();
            }
        }
    }

    @Override
    public void playRandom() {
        mAllSongsFragment.randomSong();
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

}