package com.minhntn.music.serv;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener {
    private MediaPlayer mMediaPlayer;
    private List<Song> mSongList;
    private IBinder mBinder;

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public class MediaBinder extends Binder {
         public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mSongList = new ArrayList<>();
        mBinder = new MediaBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mSongList = intent.getParcelableArrayListExtra(ActivityMusic.KEY_LIST_SONG);
        return mBinder;
    }

    public void playSong(int position) {
        Song song = mSongList.get(position);
        try {
            if (mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }
            mMediaPlayer.setDataSource(this, song.getUri());
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseSong() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void resumeSong() {
        mMediaPlayer.start();
    }

    public int getCurrentTimeSong() {
        return mMediaPlayer.getCurrentPosition();
    }

}
