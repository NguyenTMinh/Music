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
        loadMusicToExternal();
    }

    // copy the mp3 to the external storage
    private void loadMusicToExternal() {
        if (isExternalStorageWritable()) {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC);
            Field[] fields = R.raw.class.getFields();
            Log.d("MinhNTn", "loadMusicToExternal: " + path.getPath());
            for (int i = 0; i < fields.length; i++) {
                int id = getResources().getIdentifier(fields[i].getName(), "raw", getPackageName());
                InputStream in = getResources().openRawResource(id);
                try {
                    FileOutputStream out = new FileOutputStream(path.getPath() + "/");
                    byte[] buff = new byte[1024];
                    int read = 0;

                    while ((read = in.read(buff)) > 0) {
                        out.write(buff,0,read);
                    }

                    in.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Check if external storage is mounted to device
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    // Check if external storage is readable
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
           return true;
        }
        return false;
    }

}
