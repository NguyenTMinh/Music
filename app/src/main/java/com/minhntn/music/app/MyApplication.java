package com.minhntn.music.app;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.minhntn.music.MyAsyncTask;
import com.minhntn.music.MyBroadcastReceiver;
import com.minhntn.music.database.MusicDBHelper;
import com.minhntn.music.interf.IDoInAsyncTask;
import com.minhntn.music.serv.MediaPlaybackService;


public class MyApplication extends Application {
    private MusicDBHelper mDBHelper;
    private static MediaPlaybackService mMediaService;

    private IDoInAsyncTask mIDoInAsyncTask = new IDoInAsyncTask() {
        @Override
        public void doInBackground() {
            mDBHelper.loadDataFromMedia();
        }

        @Override
        public void onPostExecute() {
            LocalBroadcastManager.getInstance(MyApplication.this)
                    .sendBroadcast(new Intent(MyBroadcastReceiver.ACTION_LOAD_DONE));
        }
    };

    private static ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlaybackService.MediaBinder binder = (MediaPlaybackService.MediaBinder) service;
            mMediaService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = new MusicDBHelper(this);
        new MyAsyncTask().execute(mIDoInAsyncTask);

        Intent intent = new Intent(getApplicationContext(), MediaPlaybackService.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    public static MediaPlaybackService getMediaPlaybackService() {
        return mMediaService;
    }

    public static ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }
}
