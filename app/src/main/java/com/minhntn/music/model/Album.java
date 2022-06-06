package com.minhntn.music.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable {
    private int mID;
    private String mName;
    private byte[] mArt;

    public Album(int mID, String mName, byte[] mArt) {
        this.mID = mID;
        this.mName = mName;
        this.mArt = mArt;
    }

    protected Album(Parcel in) {
        mID = in.readInt();
        mName = in.readString();
        mArt = in.createByteArray();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mName);
        dest.writeByteArray(mArt);
    }
}
