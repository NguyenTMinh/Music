package com.minhntn.music.frag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.interf.ICommunicate;
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
    private ToggleButton mTBPlaySong;
    private TextView mTVSongCurrentTime;
    private SeekBar mSBSongProgress;

    private boolean mIsLand;
    private Song mCurrentSong;
    private ICommunicate mICommunicate;
    private CountDownTimer mTimer;

    public MediaPlaybackFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ICommunicate) {
            mICommunicate = (ICommunicate) context;
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
           mTBPlaySong = mRootView.findViewById(R.id.toggle_play_pause_bottom);
           mTBPlaySong.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   if (isChecked) {
                       mICommunicate.pauseMusic();
                       if (mTimer != null) {
                           mTimer.cancel();
                       }
                   } else {
                       mICommunicate.resumeMusic();
                       setCountdownTimer(mCurrentSong.getDuration() - mICommunicate.getTimeCurrentPlay());
                   }
               }
           });
           mTVSongCurrentTime = mRootView.findViewById(R.id.tv_song_time_current_below);
           mSBSongProgress = mRootView.findViewById(R.id.sb_progress);

           if (!mIsLand) {
               mBTBackToList.setVisibility(View.VISIBLE);
               mICommunicate.hideActionBar();
           } else {
               mBTBackToList.setVisibility(View.INVISIBLE);
           }
       }

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (mCurrentSong != null) {
            setCurrentView(mCurrentSong);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_PARCEL, mCurrentSong);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTimer != null) {
            setCountdownTimer(mCurrentSong.getDuration() - mICommunicate.getTimeCurrentPlay());
        }
    }

    public void setCurrentSong(Song song) {
        mCurrentSong = song;
    }

    public void onUpdateCurrentView(Song song) {
        mTBPlaySong.setChecked(!mICommunicate.isMusicPlaying());
        if (getActivity() != null) {
            setCurrentView(song);
        }
    }

    private void setCurrentView(Song song) {
        mCurrentSong = song;
        byte[] cover = getArguments().getByteArray(KEY_COVER);
        String alName = getArguments().getString(KEY_ALBUM_NAME);
        Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
        Glide.with(getContext()).load(bitmap).into(mIVBackground);
        Glide.with(getContext()).load(bitmap).into(mIVAlbumCoverHead);
        mTVSongNameHead.setText(mCurrentSong.getTitle());
        mTVSongAlbumHead.setText(alName);
        mTVSongDuration.setText(mCurrentSong.getDurationTimeFormat());
        mSBSongProgress.setMax((int) mCurrentSong.getDuration());
        mSBSongProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mICommunicate.seekTimeTo(progress);
                    if (mTimer != null) {
                        mTimer.cancel();
                    }
                    setCountdownTimer(mCurrentSong.getDuration() - mICommunicate.getTimeCurrentPlay());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        setCountdownTimer(mCurrentSong.getDuration());
    }

    public void setCountdownTimer(long duration) {
        mTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int timeS = (int) mICommunicate.getTimeCurrentPlay() / 1000;
                int min = (int) (timeS / 60);
                int sec = (int) (timeS % 60);
                String timeFormat = String.format("%d:%d",min, sec);
                mTVSongCurrentTime.setText(timeFormat);
                mSBSongProgress.setProgress(mICommunicate.getTimeCurrentPlay());
            }

            @Override
            public void onFinish() {
                mTBPlaySong.setChecked(true);
            }
        };

        mTimer.start();
    }
}
