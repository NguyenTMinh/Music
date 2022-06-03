package com.minhntn.music.frag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public static final String KEY_COVER = "KEY_COVER";
    private View mRootView;
    private ImageView mIVBackground;
    private ImageView mIVAlbumCoverHead;
    private TextView mTVSongNameHead;
    private TextView mTVSongAlbumHead;

    private Song mCurrentSong;
    private ITransitionFragment mITransitionFragment;

    public MediaPlaybackFragment(Song song) {
        super();
        mCurrentSong = song;
    }

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
        mITransitionFragment.hideActionBar();
        mRootView = inflater.inflate(R.layout.fragment_media_playback, container, false);
        mIVBackground = mRootView.findViewById(R.id.iv_album_cover_large);
        mIVAlbumCoverHead = mRootView.findViewById(R.id.iv_album_cover_head);
        mTVSongNameHead = mRootView.findViewById(R.id.tv_song_name_now_playing_head);
        mTVSongAlbumHead = mRootView.findViewById(R.id.tv_song_album_now_playing_head);

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        byte[] cover = getArguments().getByteArray(KEY_COVER);
        Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
        Glide.with(getContext()).load(bitmap).into(mIVBackground);
        Glide.with(getContext()).load(bitmap).into(mIVAlbumCoverHead);
        mTVSongNameHead.setText(mCurrentSong.getTitle());

    }
}
