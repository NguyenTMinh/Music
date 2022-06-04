package com.minhntn.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.util.List;

public class ActivityMusic extends AppCompatActivity implements ITransitionFragment, MyBroadcastReceiver.IDoOnLoadDone {
    private static final int REQUEST_CODE = 1;
    private List<Song> mListSong;
    private List<Album> mListAlbum;
    private MusicDBHelper mMusicDBHelper;
    private AllSongsFragment mAllSongsFragment;
    private MyBroadcastReceiver mBroadcastReceiver;

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

        mMusicDBHelper = new MusicDBHelper(this);
        mListSong = mMusicDBHelper.getAllSongs();
        mListAlbum = mMusicDBHelper.getAllAlbums();
        checkAppPermission();

        mAllSongsFragment = new AllSongsFragment();
        mAllSongsFragment.setListSong(mListSong);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mAllSongsFragment,
                null).commit();

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
    public void transition(Song song) {
        MediaPlaybackFragment mediaPlaybackFragment = new MediaPlaybackFragment(song);
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
        mediaPlaybackFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mediaPlaybackFragment)
                .addToBackStack("").commit();
    }

    @Override
    public void hideActionBar() {
        getSupportActionBar().hide();
    }

    @Override
    public void onBackPressed() {
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
        }
        super.onBackPressed();
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
}