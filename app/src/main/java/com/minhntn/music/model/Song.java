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
    private boolean mIsPlaying; // for displaying in adapter
    private boolean mIsFavorite;
    private int mCountOfPlay;
    private int mFavLevel;
    private boolean mIsDislike;

    public Song() {}

    public Song(int mID, String mTitle, long mDuration, String mUri, String mArtist, int mAlbumID, int favorite, int mCountOfPlay, int mFavLevel, boolean mIsDislike) {
        this.mID = mID;
        this.mTitle = mTitle;
        this.mDuration = mDuration;
        this.mUri = mUri;
        this.mArtist = mArtist;
        this.mAlbumID = mAlbumID;
        if (favorite == 1) {
            this.mIsFavorite = true;
        } else {
            this.mIsFavorite = false;
        }
        this.mCountOfPlay = mCountOfPlay;
        this.mFavLevel = mFavLevel;
        this.mIsDislike = mIsDislike;
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
        long timeA = mDuration;
        if (mDuration % 1000 > 100) {
            timeA += 1000;
        } else if (mDuration % 1000 < 10) {
            timeA -= 1000;
        }
        int timeS = (int) ((timeA) / 1000);
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

    public void setIsFavorite(boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public int getCountOfPlay() {
        return mCountOfPlay;
    }

    public void setCountOfPlay() {
        if (mCountOfPlay < 3) {
            this.mCountOfPlay++;
        }
    }

    public int getFavLevel() {
        return mFavLevel;
    }

    public void setFavLevel(int mFavLevel) {
        this.mFavLevel = mFavLevel;
    }

    public boolean isDislike() {
        return mIsDislike;
    }

    public void setDislike(boolean mIsDislike) {
        this.mIsDislike = mIsDislike;
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
