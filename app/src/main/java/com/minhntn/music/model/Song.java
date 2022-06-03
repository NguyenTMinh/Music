package com.minhntn.music.model;

import android.net.Uri;
import android.util.Log;

public class Song {
    private int mID;
    private String mTitle;
    private long mDuration;
    private String mUri;
    private int mAlbumID;
    private String mArtist;

    public Song(int mID, String mTitle, long mDuration, String mUri, String mArtist, int mAlbumID) {
        this.mID = mID;
        this.mTitle = mTitle;
        this.mDuration = mDuration;
        this.mUri = mUri;
        this.mArtist = mArtist;
        this.mAlbumID = mAlbumID;
    }

    public int getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public long getDuration() {
        return mDuration;
    }

    public String getDurationTimeFormat() {
        int timeS = (int) (mDuration / 1000);
        int min = (int) (timeS / 60);
        int sec = (int) (timeS % 60);
        return String.format("%d:%d",min, sec);
    }

    public Uri getUri() {
        return Uri.parse(mUri);
    }

    public int getAlbumID() {
        return mAlbumID;
    }

    public String getmArtist() {
        return mArtist;
    }

    public String info() {
        return mID + ", " + mTitle + ", " + mDuration + ", " + mUri + ", " + mArtist + ", " + mAlbumID;
    }
}
