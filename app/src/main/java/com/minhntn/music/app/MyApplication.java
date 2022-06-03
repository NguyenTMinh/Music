package com.minhntn.music.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.minhntn.music.R;
import com.minhntn.music.database.MusicDBHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class MyApplication extends Application {
    private MusicDBHelper mDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mDBHelper = new MusicDBHelper(this);
        mDBHelper.loadDataFromMedia();
    }

}
