package com.minhntn.music.serv;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.database.MusicDBHelper;
import com.minhntn.music.frag.MediaPlaybackFragment;
import com.minhntn.music.interf.ICommunicate;
import com.minhntn.music.model.Song;
import com.minhntn.music.prov.MusicContacts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final String ACTION_NEXT_SONG = "ACTION_NEXT_SONG";
    public static final String ACTION_PRE_SONG = "ACTION_PRE_SONG";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    private static final String PRIMARY_CHANNEL_ID = "PRIMARY_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mMediaPlayer;
    private List<Song> mSongList;
    private IBinder mBinder;
    private ICommunicate mICommunicate;
    private NotificationManager mNotificationManager;
    private MediaBroadcastReceiver mBroadcastReceiver;
    private MusicDBHelper mMusicDBHelper;
    private RemoteViews remoteViewsDefault;
    private RemoteViews remoteViewsBig;
    private SharedPreferences mSharedPreferences;

    private int mCurrentSongIndex = -1;
    private int mCurrentModePlay;
    private int mButtonState;
    private boolean mIsSongPlayInList;

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mIsSongPlayInList) {
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
                        mICommunicate.pauseMusic(true);
                        setButtonStateNotification();
                    }
                }
            }
        } else {
            mICommunicate.pauseMusic(true);
            setButtonStateNotification();
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
        mBroadcastReceiver = new MediaBroadcastReceiver();
        mMusicDBHelper = new MusicDBHelper(this);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mSharedPreferences = getSharedPreferences(MusicContacts.SHARED_PREF_NAME, MODE_PRIVATE);
        mIsSongPlayInList = true;

        // Register receiver for control action on notification
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NEXT_SONG);
        intentFilter.addAction(ACTION_PRE_SONG);
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        mSongList = intent.getParcelableArrayListExtra(ActivityMusic.KEY_LIST_SONG);
        Notification notification = getNotificationBuilder(null, null, null).build();

        startForeground(NOTIFICATION_ID, notification);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mSongList = intent.getParcelableArrayListExtra(ActivityMusic.KEY_LIST_SONG);
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.d("minhntn", "onDestroy: service");
        if (mMediaPlayer.isPlaying()) {
            // write something here
        }
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * play a song at specified position of list
     * @param position
     */
    public void playSong(int position) {
        mIsSongPlayInList = true;
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

        mButtonState = 0;
        updateNotification();
    }

    public void pauseSong() {
        mButtonState = 1;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            setButtonStateNotification();

            // edit SharePreference for start app later
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(MusicContacts.PREF_MUSIC_PLAYING, isMediaPlaying());
            editor.apply();
        }
    }

    public void resumeSong() {
        if (!mMediaPlayer.isPlaying() && mCurrentSongIndex != -1) {
            mMediaPlayer.start();
            mButtonState = 0;

            if (mIsSongPlayInList) {
                updateNotification();
            }

            // edit SharePreference for start app later
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(MusicContacts.PREF_MUSIC_PLAYING, isMediaPlaying());
            editor.apply();
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
     * to prepare the data to media playing when launch app again after close app and relaunch
     * @param index
     */
    public void setMediaUriSource(int index) {
        if (index != -1) {
            mCurrentSongIndex = index;
        }
        Song song = mSongList.get(mCurrentSongIndex);
        try {
            if (mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
                mMediaPlayer.stop();
            }
            
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

    public void setCurrentSongIndex(int index) {
        this.mCurrentSongIndex = index;
    }

    public boolean isSongPlayInList() {
        return mIsSongPlayInList;
    }

    public void setIsSongPlayInList(boolean mIsSongPlayInList) {
        this.mIsSongPlayInList = mIsSongPlayInList;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "play song", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setLightColor(Color.RED);
            channel.setDescription("Notification Play Song");

            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(Bitmap bitmap, String artist, String song) {
        // Create pendingIntent for notification
        // pendingIntent to start Activity when click on notification
        Intent openIntent = new Intent(this, ActivityMusic.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID,
                openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // pendingIntent to play next song
        Intent nextSongIntent = new Intent(ACTION_NEXT_SONG);
        PendingIntent nextSongPendingIntent = PendingIntent.getBroadcast(this, R.id.bt_fwd_notification,
                nextSongIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // pendingIntent to play previous song
        Intent preSongIntent = new Intent(ACTION_PRE_SONG);
        PendingIntent preSongPendingIntent = PendingIntent.getBroadcast(this, R.id.bt_rew_notification,
                preSongIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // pendingIntent to play or pause the current song
        Intent playPauseIntent = new Intent(ACTION_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, R.id.ib_play_pause_notification,
                playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // remoteView for default notification
        remoteViewsDefault = new RemoteViews(getPackageName(), R.layout.notification_default_layout);
        remoteViewsDefault.setOnClickPendingIntent(R.id.bt_fwd_notification, nextSongPendingIntent);
        remoteViewsDefault.setOnClickPendingIntent(R.id.bt_rew_notification, preSongPendingIntent);
        if (bitmap != null) {
            remoteViewsDefault.setImageViewBitmap(R.id.iv_album_cover_notification, bitmap);
        }
        remoteViewsDefault.setOnClickPendingIntent(R.id.ib_play_pause_notification, playPausePendingIntent);

        // remoteView for big notification
        remoteViewsBig = new RemoteViews(getPackageName(), R.layout.notification_big_layout);
        remoteViewsBig.setOnClickPendingIntent(R.id.bt_fwd_notification, nextSongPendingIntent);
        remoteViewsBig.setOnClickPendingIntent(R.id.bt_rew_notification, preSongPendingIntent);
        remoteViewsBig.setOnClickPendingIntent(R.id.ib_play_pause_notification, playPausePendingIntent);
        if (bitmap != null) {
            remoteViewsBig.setImageViewBitmap(R.id.iv_album_cover_notification, bitmap);
        }
        if (artist != null && song != null) {
            remoteViewsBig.setTextViewText(R.id.tv_artist_notification, artist);
            remoteViewsBig.setTextViewText(R.id.tv_song_name_notification, song);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setContentIntent(openPendingIntent)
                .setCustomContentView(remoteViewsDefault)
                .setCustomBigContentView(remoteViewsBig)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        return builder;
    }

    public void setSongList(List<Song> list) {
        mSongList = list;
    }

    private void updateNotification() {
        Song song = mSongList.get(mCurrentSongIndex);
        new UpdateNotificationAsync().execute(song.getID());
    }

    private void playOrPause() {
        if (mButtonState == 0) {
            mICommunicate.pauseMusic(true);
        } else {
            mICommunicate.resumeMusic(true);
        }

        setButtonStateNotification();
    }

    private void setButtonStateNotification() {
        NotificationCompat.Builder builder = getNotificationBuilder(null, null, null);

        remoteViewsDefault.setInt(R.id.ib_play_pause_notification, "setImageLevel",
                mButtonState);
        remoteViewsBig.setInt(R.id.ib_play_pause_notification, "setImageLevel",
                mButtonState);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    class MediaBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_NEXT_SONG: {
                    mICommunicate.playNextSong();
                    updateNotification();
                    break;
                }
                case ACTION_PRE_SONG: {
                    mICommunicate.playPreviousSong();
                    updateNotification();
                    break;
                }
                case ACTION_PLAY_PAUSE: {
                    playOrPause();
                    break;
                }
            }
        }
    }

    class UpdateNotificationAsync extends AsyncTask<Integer, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Integer... integers) {
            return mMusicDBHelper.getInfoNowPlayingSong(integers[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

            Song song = mSongList.get(mCurrentSongIndex);
            cursor.moveToFirst();
            String albumName = cursor.getString(0);
            byte[] albumCover = cursor.getBlob(1);

            Bitmap bitmap = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);
            NotificationCompat.Builder builder = getNotificationBuilder(bitmap, song.getmArtist(), song.getTitle());

            remoteViewsDefault.setInt(R.id.ib_play_pause_notification, "setImageLevel",
                    mButtonState);
            remoteViewsBig.setInt(R.id.ib_play_pause_notification, "setImageLevel",
                    mButtonState);

            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
