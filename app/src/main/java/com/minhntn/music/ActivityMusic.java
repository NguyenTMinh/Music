package com.minhntn.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.minhntn.music.database.MusicDBHelper;
import com.minhntn.music.model.Song;

import java.util.List;

public class ActivityMusic extends AppCompatActivity {
    private RecyclerView mRVSongs;
    private SongAdapter mSongAdapter;
    private List<Song> mListSong;
    private MusicDBHelper mMusicDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMusicDBHelper = new MusicDBHelper(this);
        mListSong = mMusicDBHelper.getAllSongs();

        mSongAdapter = new SongAdapter(mListSong, this);
        mRVSongs = findViewById(R.id.rv_list_song);
        mRVSongs.setAdapter(mSongAdapter);
        mRVSongs.setLayoutManager(new LinearLayoutManager(this));

        Log.d("minhntn", "onCreate: " + mListSong.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }


}