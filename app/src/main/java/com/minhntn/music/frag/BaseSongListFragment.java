package com.minhntn.music.frag;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.adapter.SongAdapter;
import com.minhntn.music.interf.ICallBack;
import com.minhntn.music.interf.ICommunicate;
import com.minhntn.music.model.Song;
import com.minhntn.music.prov.MusicContacts;

import java.util.List;
import java.util.Random;

public abstract class BaseSongListFragment extends Fragment implements ICallBack {

    protected View mRootView;
    protected RecyclerView mRVSongs;
    protected List<Song> mListSong;
    protected ICommunicate mICommunicate;
    protected SongAdapter mSongAdapter;

    protected boolean mIsLand;
    protected boolean mIsPlaying;
    protected int mCurrentIndexSong = -1;
    protected boolean mIsServiceAlive;

    private int tempID;

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
        mIsServiceAlive = getArguments().getBoolean(MusicContacts.PREF_SERVICE_ALIVE, false);
        mIsPlaying = getArguments().getBoolean(ActivityMusic.KEY_MUSIC_PLAYING, false);

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_all_songs, container, false);
            if (mListSong != null) {
                mSongAdapter = new SongAdapter(mListSong, getContext(), this, getMenuRes());
                mSongAdapter.setIndex(mCurrentIndexSong);
            }

            mRVSongs = mRootView.findViewById(R.id.rv_list_song);
            mRVSongs.setAdapter(mSongAdapter);
            mRVSongs.setLayoutManager(new LinearLayoutManager(getContext()));

            if (!mIsLand && savedInstanceState != null) {
                if (mListSong != null) {
                    displayNowPlayingView(mCurrentIndexSong, true);
                }
            }

        }

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ActivityMusic.KEY_MUSIC_PLAYING, mIsPlaying);
    }

    @Override
    public void onDestroyView() {
        if (mRootView.getParent() != null) {
            ((ViewGroup)mRootView.getParent()).removeView(mRootView);
        }
        super.onDestroyView();
    }

    @Override
    public void displayNowPlayingView(int position, boolean isClicked) {
        mCurrentIndexSong = position;
        mICommunicate.displayControlMedia(position, isClicked, getFragTag());
    }

    @Override
    public void setStatePlay(boolean state) {
        mICommunicate.setIsFromPause(false);
        mICommunicate.setStatePlaying(state);
        mIsPlaying = state;
    }

    @Override
    public void increaseCount() {
        if (mCurrentIndexSong != -1) {
            mListSong.get(mCurrentIndexSong).setCountOfPlay();
            mICommunicate.updateCountPlay(mListSong.get(mCurrentIndexSong).getID());
        }
    }

    @Override
    public void setSongOnList() {
        if (mCurrentIndexSong != -1) {
            tempID = mListSong.get(mCurrentIndexSong).getID();
        }
    }

    public void setListSong(List<Song> list) {
        mListSong = list;
    }

    public void notifyAdapter(List<Song> list) {
        mListSong.clear();
        mListSong.addAll(list);
        mSongAdapter.notifyDataSetChanged();
    }

    public void notifyAdapter() {
        mSongAdapter.notifyDataSetChanged();
    }

    public int setAdapterIndex(int index) {
        if (index != -1) {
            if (mCurrentIndexSong != -1) {
                mSongAdapter.notifyItemChanged(mCurrentIndexSong);
            }
            if (mSongAdapter != null) {
                mSongAdapter.notifyItemChanged(index);
                mSongAdapter.setIndex(index);
            }
            mCurrentIndexSong = index;
        }
        return mCurrentIndexSong;
    }

    public void resetAdapterIndex() {
        mSongAdapter.setIndex(-1);
    }

    public void setButtonState(boolean state) {
        mIsPlaying = state;
    }

    public void nextSong() {
        mCurrentIndexSong++;
        if (mCurrentIndexSong >= mListSong.size()) {
            mCurrentIndexSong = 0;
        }
        SongAdapter.SongViewHolder viewHolder = (SongAdapter.SongViewHolder) mRVSongs.findViewHolderForAdapterPosition(mCurrentIndexSong);
        if (viewHolder != null) {
            mSongAdapter.setIsClickedOnItem(true);
            viewHolder.onClick(viewHolder.itemView);
        } else {
            mSongAdapter.playNextSongIfViewHolderNull();
        }
    }

    public void previousSong() {
        mCurrentIndexSong--;
        if (mCurrentIndexSong < 0) {
            mCurrentIndexSong = mListSong.size() -1;
        }
        SongAdapter.SongViewHolder viewHolder = (SongAdapter.SongViewHolder) mRVSongs.findViewHolderForAdapterPosition(mCurrentIndexSong);
        if (viewHolder != null) {
            mSongAdapter.setIsClickedOnItem(true);
            viewHolder.onClick(viewHolder.itemView);
        } else {
            mSongAdapter.playPreviousSongIfViewHolderNull();
        }
    }

    public void randomSong() {
        int rand;
        do {
            rand = new Random().nextInt(mListSong.size());
        }while (rand == mCurrentIndexSong);
        mCurrentIndexSong = rand;
        SongAdapter.SongViewHolder viewHolder = (SongAdapter.SongViewHolder) mRVSongs.findViewHolderForAdapterPosition(mCurrentIndexSong);
        if (viewHolder != null) {
            mSongAdapter.setIsClickedOnItem(true);
            viewHolder.onClick(viewHolder.itemView);
        } else {
            mSongAdapter.playRandom(mCurrentIndexSong);
        }
    }

    public void onResumeFromScreen(int position) {
        mRVSongs.smoothScrollToPosition(position);
    }

    public int getIDFromSongOnList() {
        try {
            if (mCurrentIndexSong != -1) {
                return mListSong.get(mCurrentIndexSong).getID();
            }
        } catch (IndexOutOfBoundsException e) {
            return tempID;
        }
        return -1;
    }

    public int getIDFromSongNotOnList() {
        if (mCurrentIndexSong == -1) {
            return tempID;
        }
        return -1;
    }

    public int getCurrentIndexSong() {
        return this.mCurrentIndexSong;
    }

    public void setCurrentIndexSong(int indexSong) {
        mCurrentIndexSong = indexSong;
    }

    protected abstract int getMenuRes();

    protected abstract String getFragTag();
}
