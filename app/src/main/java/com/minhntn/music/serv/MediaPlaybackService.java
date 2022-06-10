package com.minhntn.music.serv;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.interf.ICommunicate;
import com.minhntn.music.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final int NEXT_SONG = -2;
    private MediaPlayer mMediaPlayer;
    private List<Song> mSongList;
    private IBinder mBinder;
    private int mCurrentSongIndex;
    private ICommunicate mICommunicate;

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mICommunicate.playNextSong();
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
        mSongList = intent.getParcelableArrayListExtra(ActivityMusic.KEY_LIST_SONG);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mSongList = intent.getParcelableArrayListExtra(ActivityMusic.KEY_LIST_SONG);
        return mBinder;
    }

    public void playSong(int position) {
        // Check the index of song should be played
        if (position >= 0) {
            mCurrentSongIndex = position;
        } else {
            if (position == NEXT_SONG) {
                mCurrentSongIndex++;
            } else {
                mCurrentSongIndex = 0;
            }
        }
        try {
            Song song = mSongList.get(mCurrentSongIndex);
            try {
                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                    mMediaPlayer.stop();
                }

                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(this, song.getUri());
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnCompletionListener(this);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void pauseSong() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void resumeSong() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public int getCurrentTimeSong() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void seekPlayTimeTo(int seekTime) {
        mMediaPlayer.seekTo(seekTime);
    }

    public boolean isMediaPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void setICommunicate(ICommunicate iCommunicate) {
        mICommunicate = iCommunicate;
    }

    public void setMediaUriSource(int index) {
        Song song = mSongList.get(index);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, song.getUri());
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
