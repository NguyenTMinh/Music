package com.minhntn.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.minhntn.music.interf.ICommunicate;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_LOAD_DONE = "ACTION_LOAD_DONE";
    public static final String ACTION_NEXT_SONG = "ACTION_NEXT_SONG";
    public static final String ACTION_PRE_SONG = "ACTION_PRE_SONG";
    private ICommunicate mICommunicate;

    public MyBroadcastReceiver(ICommunicate mICommunicate) {
        super();
        this.mICommunicate = mICommunicate;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_LOAD_DONE: {
                mICommunicate.doOnLoadDone();
                break;
            }
        }
    }

}
