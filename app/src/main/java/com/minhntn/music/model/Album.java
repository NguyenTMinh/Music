package com.minhntn.music.model;

public class Album {
    private int mID;
    private String mName;
    private byte[] mArt;

    public Album(int mID, String mName, byte[] mArt) {
        this.mID = mID;
        this.mName = mName;
        this.mArt = mArt;
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public byte[] getArt() {
        return mArt;
    }

    public String info() {
        return mID + ", " + mName + ", ";
    }
}
