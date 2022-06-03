package com.minhntn.music;

import android.os.AsyncTask;

import com.minhntn.music.interf.IDoInAsyncTask;

public class MyAsyncTask extends AsyncTask<IDoInAsyncTask, Void, IDoInAsyncTask> {

    @Override
    protected IDoInAsyncTask doInBackground(IDoInAsyncTask... iDoInAsyncTasks) {
        iDoInAsyncTasks[0].doInBackground();
        return iDoInAsyncTasks[0];
    }

    @Override
    protected void onPostExecute(IDoInAsyncTask iDoInAsyncTask) {
        super.onPostExecute(iDoInAsyncTask);
        iDoInAsyncTask.onPostExecute();
    }
}
