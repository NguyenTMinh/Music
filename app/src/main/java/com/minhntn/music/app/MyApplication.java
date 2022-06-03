package com.minhntn.music.app;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.minhntn.music.MyAsyncTask;
import com.minhntn.music.MyBroadcastReceiver;
import com.minhntn.music.R;
import com.minhntn.music.database.MusicDBHelper;
import com.minhntn.music.interf.IDoInAsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

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
