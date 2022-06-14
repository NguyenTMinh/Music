package com.minhntn.music.app;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.minhntn.music.MyAsyncTask;
import com.minhntn.music.MyBroadcastReceiver;
import com.minhntn.music.database.MusicDBHelper;
import com.minhntn.music.interf.IDoInAsyncTask;

public class MyApplication extends Application {
    private MusicDBHelper mDBHelper;

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

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = new MusicDBHelper(this);
        new MyAsyncTask().execute(mIDoInAsyncTask);
    }
}
