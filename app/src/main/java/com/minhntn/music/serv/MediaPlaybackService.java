package com.minhntn.music.serv;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.frag.MediaPlaybackFragment;
import com.minhntn.music.interf.ICommunicate;
import com.minhntn.music.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private static final String PRIMARY_CHANNEL_ID = "PRIMARY_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mMediaPlayer;
    private List<Song> mSongList;
    private IBinder mBinder;
    private ICommunicate mICommunicate;
    private NotificationManager mNotificationManager;

    private int mCurrentSongIndex;
    private int mCurrentModePlay;


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (mCurrentModePlay) {
            case MediaPlaybackFragment.PLAY_MODE_REPEAT_SINGLE: {
                mp.start();
                mICommunicate.playRepeatOneSong();
                break;
            }
            case MediaPlaybackFragment.PLAY_MODE_SHUFFLE_ON: {
                mICommunicate.playRandom();
                break;
            }
            case MediaPlaybackFragment.PLAY_MODE_REPEAT_LIST: {
                mICommunicate.playNextSong();
                break;
            }
            case MediaPlaybackFragment.PLAY_MODE_DEFAULT: {
                if (mCurrentSongIndex < mSongList.size() - 1) {
                    mICommunicate.playNextSong();
                } else {
                    mICommunicate.setPauseButton();
                }
            }
        }

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
        createNotificationChannel();
        mSongList = intent.getParcelableArrayListExtra(ActivityMusic.KEY_LIST_SONG);
        Notification notification = getNotificationBuilder().build();

        startForeground(NOTIFICATION_ID, notification);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mSongList = intent.getParcelableArrayListExtra(ActivityMusic.KEY_LIST_SONG);
        return mBinder;
    }

    /**
     * play a song at specified position of list
     * @param position
     */
    public void playSong(int position) {
        // Check the index of song should be played
        if (position >= 0) {
            mCurrentSongIndex = position;
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

    /**
     * to prepare the data to media playing when launch app again after the previous session
     * @param index
     */
    public void setMediaUriSource(int index) {
        if (index != -1) {
            mCurrentSongIndex = index;
        }
        Song song = mSongList.get(mCurrentSongIndex);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, song.getUri());
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * set mode play song
     * @param modePlay
     */
    public void setCurrentModePlay(int modePlay) {
        mCurrentModePlay = modePlay;
    }

    private void createNotificationChannel() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "play song", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            channel.setDescription("Notification Play Song");

            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        Intent openIntent = new Intent(this, ActivityMusic.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID,
                openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViewsDefault = new RemoteViews(getPackageName(), R.layout.notification_default_layout);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setContentIntent(openPendingIntent)
                .setCustomContentView(remoteViewsDefault)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        return builder;
    }

}
