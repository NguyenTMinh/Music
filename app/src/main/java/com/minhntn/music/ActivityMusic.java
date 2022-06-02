package com.minhntn.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.minhntn.music.interf.ITransitionFragment;
import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;

import java.util.List;

public class ActivityMusic extends AppCompatActivity implements ITransitionFragment {
    private List<Song> mListSong;
    private List<Album> mListAlbum;
    private MusicDBHelper mMusicDBHelper;
    private AllSongsFragment mAllSongsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMusicDBHelper = new MusicDBHelper(this);
        mListSong = mMusicDBHelper.getAllSongs();
        mListAlbum = mMusicDBHelper.getAllAlbums();

        mAllSongsFragment = new AllSongsFragment();
        mAllSongsFragment.setListSong(mListSong);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mAllSongsFragment,
                null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    public MusicDBHelper getMusicDBHelper() {
        return mMusicDBHelper;
    }

    @Override
    public void transition(Song song) {
        MediaPlaybackFragment mediaPlaybackFragment = new MediaPlaybackFragment();
        Bundle bundle = new Bundle();
        byte[] cover = new byte[1];
        for (int i =0; i < mListAlbum.size(); i++) {
            if (song.getAlbumID() == mListAlbum.get(i).getID()) {
                cover = mListAlbum.get(i).getArt();
                break;
            }
        }
        bundle.putByteArray(MediaPlaybackFragment.KEY_COVER, cover);
        mediaPlaybackFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mediaPlaybackFragment)
                .addToBackStack("").commit();
    }
}