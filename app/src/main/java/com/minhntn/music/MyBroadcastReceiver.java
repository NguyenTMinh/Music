package com.minhntn.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_LOAD_DONE = "ACTION_LOAD_DONE";
    private IDoOnLoadDone mIDoOnLoadDone;

    public MyBroadcastReceiver(IDoOnLoadDone mIDoOnLoadDone) {
        super();
        this.mIDoOnLoadDone = mIDoOnLoadDone;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_LOAD_DONE: {
                mIDoOnLoadDone.doOnLoadDone();
                break;
            }
        }
    }

    public interface IDoOnLoadDone {
        void doOnLoadDone();
    }
}
