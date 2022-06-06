package com.minhntn.music.model;

import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Song implements Parcelable {
    private int mID;
    private String mTitle;
    private long mDuration;
    private String mUri;
    private int mAlbumID;
    private String mArtist;
    private boolean mIsPlaying;

    public Song(int mID, String mTitle, long mDuration, String mUri, String mArtist, int mAlbumID) {
        this.mID = mID;
        this.mTitle = mTitle;
        this.mDuration = mDuration;
        this.mUri = mUri;
        this.mArtist = mArtist;
        this.mAlbumID = mAlbumID;
    }

    protected Song(Parcel in) {
        mID = in.readInt();
        mTitle = in.readString();
        mDuration = in.readLong();
        mUri = in.readString();
        mAlbumID = in.readInt();
        mArtist = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mIsPlaying = in.readBoolean();
        }
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

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

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public void setPlaying(boolean playing) {
        mIsPlaying = playing;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mTitle);
        dest.writeLong(mDuration);
        dest.writeString(mUri);
        dest.writeInt(mAlbumID);
        dest.writeString(mArtist);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(mIsPlaying);
        }
    }
}
