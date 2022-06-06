package com.minhntn.music.frag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.interf.ITransitionFragment;
import com.minhntn.music.model.Song;

public class MediaPlaybackFragment extends Fragment {
    public static final String FRAGMENT_TAG = "MediaPlaybackFragment";
    public static final String KEY_COVER = "KEY_COVER";
    public static final String KEY_ALBUM_NAME = "KEY_ALBUM_NAME";
    private static final String KEY_PARCEL = "KEY_PARCEL";
    private View mRootView;
    private ImageView mIVBackground;
    private ImageView mIVAlbumCoverHead;
    private TextView mTVSongNameHead;
    private TextView mTVSongAlbumHead;
    private ImageButton mBTBackToList;
    private TextView mTVSongDuration;
    private boolean mIsLand;

    private Song mCurrentSong;
    private ITransitionFragment mITransitionFragment;

    public MediaPlaybackFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ITransitionFragment) {
            mITransitionFragment = (ITransitionFragment) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mIsLand = getArguments().getBoolean(ActivityMusic.KEY_IS_LAND, false);
        if (savedInstanceState != null){
            mCurrentSong = savedInstanceState.getParcelable(KEY_PARCEL);
        }
       if (mRootView == null) {
           mRootView = inflater.inflate(R.layout.fragment_media_playback, container, false);
           mIVBackground = mRootView.findViewById(R.id.iv_album_cover_large);
           mIVAlbumCoverHead = mRootView.findViewById(R.id.iv_album_cover_head);
           mTVSongNameHead = mRootView.findViewById(R.id.tv_song_name_now_playing_head);
           mTVSongAlbumHead = mRootView.findViewById(R.id.tv_song_album_now_playing_head);
           mTVSongDuration = mRootView.findViewById(R.id.tv_song_duration_below);
           mBTBackToList = mRootView.findViewById(R.id.bt_back_to_list);
           mBTBackToList.setOnClickListener(v -> {
               getActivity().onBackPressed();
           });
           if (!mIsLand) {
               mBTBackToList.setVisibility(View.VISIBLE);
               mITransitionFragment.hideActionBar();
           } else {
               mBTBackToList.setVisibility(View.INVISIBLE);
           }
       }

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setCurrentView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_PARCEL, mCurrentSong);
    }

    public void setCurrentSong(Song song) {
        mCurrentSong = song;
    }

    public void onUpdateCurrentView() {
        setCurrentView();
    }

    private void setCurrentView() {
        Log.d("MinhNTn", "setCurrentView: " + mIVBackground);
        byte[] cover = getArguments().getByteArray(KEY_COVER);
        String alName = getArguments().getString(KEY_ALBUM_NAME);
        Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
        Glide.with(getContext()).load(bitmap).into(mIVBackground);
        Glide.with(getContext()).load(bitmap).into(mIVAlbumCoverHead);
        mTVSongNameHead.setText(mCurrentSong.getTitle());
        mTVSongAlbumHead.setText(alName);
        mTVSongDuration.setText(mCurrentSong.getDurationTimeFormat());
    }
}
