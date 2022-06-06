package com.minhntn.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.minhntn.music.database.MusicDBHelper;
import com.minhntn.music.frag.AllSongsFragment;
import com.minhntn.music.frag.MediaPlaybackFragment;
import com.minhntn.music.interf.ICallBack;
import com.minhntn.music.interf.IDoInAsyncTask;
import com.minhntn.music.interf.ITransitionFragment;
import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;

import java.util.ArrayList;
import java.util.List;

public class ActivityMusic extends AppCompatActivity implements ITransitionFragment, MyBroadcastReceiver.IDoOnLoadDone {
    public static final String KEY_IS_LAND = "KEY_IS_LAND";
    public static final String KEY_INDEX_CURRENT = "KEY_INDEX_CURRENT";
    public static final String KEY_LIST_SONG = "KEY_LIST_SONG";
    public static final String KEY_LIST_ALBUM = "KEY_LIST_ALBUM";
    private static final int REQUEST_CODE = 1;
    private List<Song> mListSong;
    private List<Album> mListAlbum;
    private MusicDBHelper mMusicDBHelper;
    private AllSongsFragment mAllSongsFragment;
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private MyBroadcastReceiver mBroadcastReceiver;
    private boolean mIsLand;
    private int mIndexCurrentSong = -1;

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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mMusicDBHelper = new MusicDBHelper(this);
            mListSong = savedInstanceState.getParcelableArrayList(KEY_LIST_SONG);
            mListAlbum = savedInstanceState.getParcelableArrayList(KEY_LIST_ALBUM);
            mIndexCurrentSong = savedInstanceState.getInt(KEY_INDEX_CURRENT);
        } else {
            mMusicDBHelper = new MusicDBHelper(this);
            mListSong = mMusicDBHelper.getAllSongs();
            mListAlbum = mMusicDBHelper.getAllAlbums();
            checkAppPermission();
        }
        mIsLand = getResources().getBoolean(R.bool.is_land);

        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_IS_LAND, mIsLand);

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
        mMediaPlaybackFragment.setArguments(bundle);

        if (mIsLand) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mMediaPlaybackFragment,
                    MediaPlaybackFragment.FRAGMENT_TAG).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_1, mAllSongsFragment,
                    AllSongsFragment.FRAGMENT_TAG).commit();
            if (savedInstanceState != null) {
                if (!getSupportActionBar().isShowing()) {
                    getSupportActionBar().show();
                }
                if (mIndexCurrentSong != -1) {
                    transition(mIndexCurrentSong);
                }
            }
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mAllSongsFragment,
                    AllSongsFragment.FRAGMENT_TAG).commit();
        }

        mBroadcastReceiver = new MyBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyBroadcastReceiver.ACTION_LOAD_DONE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, intentFilter);

    }

    private void checkAppPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }
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
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new MyAsyncTask().execute(mIDoInAsyncTask);
            }
        }
    }

    public MusicDBHelper getMusicDBHelper() {
        return mMusicDBHelper;
    }

    @Override
    public void transition(int position) {
        mIndexCurrentSong = position;
        Song song = mListSong.get(mIndexCurrentSong);
        Bundle bundle = new Bundle();
        byte[] cover = new byte[1];
        String albumName = "";
        for (int i =0; i < mListAlbum.size(); i++) {
            if (song.getAlbumID() == mListAlbum.get(i).getID()) {
                cover = mListAlbum.get(i).getArt();
                albumName = mListAlbum.get(i).getName();
                break;
            }
        }
        bundle.putByteArray(MediaPlaybackFragment.KEY_COVER, cover);
        bundle.putString(MediaPlaybackFragment.KEY_ALBUM_NAME, albumName);
        bundle.putBoolean(KEY_IS_LAND, mIsLand);

        FragmentManager fragManager = getSupportFragmentManager();

//        if (fragManager.getBackStackEntryCount() == 0 && !mIsLand) {
//            mMediaPlaybackFragment.setCurrentSong(song);
//            mMediaPlaybackFragment.setArguments(bundle);
//
//            FragmentTransaction transaction =  fragManager.beginTransaction();
//            transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.slowly_disappear, 0, R.anim.exit_to_bottom)
//                    .replace(R.id.fragment_container, mMediaPlaybackFragment, MediaPlaybackFragment.FRAGMENT_TAG)
//                    .commit();
//        } else {
//            if (fragManager.getBackStackEntryCount() == 0) {
//                mMediaPlaybackFragment.setCurrentSong(song);
//                mMediaPlaybackFragment.setArguments(bundle);
//
//                FragmentTransaction transaction =  fragManager.beginTransaction();
//                transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.slowly_disappear, 0, R.anim.exit_to_bottom)
//                        .replace(R.id.fragment_container, mMediaPlaybackFragment, MediaPlaybackFragment.FRAGMENT_TAG)
//                        .commit();
//            } else {
//
//                mMediaPlaybackFragment.setArguments(bundle);
//                mMediaPlaybackFragment.setCurrentSong(song);
//                mMediaPlaybackFragment.onUpdateCurrentView();
//
//                FragmentTransaction transaction =  fragManager.beginTransaction();
//                transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.slowly_disappear, 0, R.anim.exit_to_bottom)
//                        .replace(R.id.fragment_container, mMediaPlaybackFragment, MediaPlaybackFragment.FRAGMENT_TAG)
//                        .commit();
//            }
//        }

        if (mIsLand) {
            mMediaPlaybackFragment.setCurrentSong(song);
            mMediaPlaybackFragment.setArguments(bundle);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMediaPlaybackFragment.onUpdateCurrentView(song);
                }
            }, 150);
        } else {
            mMediaPlaybackFragment.setArguments(bundle);
            Log.d("MinhNTn", "transition: " + song);

            FragmentTransaction transaction =  fragManager.beginTransaction();
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
        getSupportActionBar().hide();
    }

    @Override
    public void passCurrentPositionIfPortrait(int position) {
        mIndexCurrentSong = position;
    }

    @Override
    public void onBackPressed() {
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
        }
        if (mIsLand) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void doOnLoadDone() {
        mIDoInAsyncTask.onPostExecute();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_LIST_SONG, (ArrayList<? extends Parcelable>) mListSong);
        outState.putParcelableArrayList(KEY_LIST_ALBUM, (ArrayList<? extends Parcelable>) mListAlbum);
        outState.putInt(KEY_INDEX_CURRENT, mIndexCurrentSong);
    }

    private Fragment recreateFragment(Fragment f)
    {
        try {
            Fragment.SavedState savedState = getSupportFragmentManager().saveFragmentInstanceState(f);

            Fragment newInstance = f.getClass().newInstance();
            newInstance.setInitialSavedState(savedState);

            return newInstance;
        }
        catch (Exception e) // InstantiationException, IllegalAccessException
        {
            throw new RuntimeException("Cannot reinstantiate fragment " + f.getClass().getName(), e);
        }
    }
}