package com.minhntn.music.frag;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.SongAdapter;
import com.minhntn.music.interf.ICallBack;
import com.minhntn.music.interf.ICommunicate;
import com.minhntn.music.model.Song;

import java.util.List;

public class AllSongsFragment extends Fragment implements ICallBack {
    public static final String FRAGMENT_TAG = "AllSongsFragment";

    private View mRootView;
    private RecyclerView mRVSongs;
    private SongAdapter mSongAdapter;
    private View mNowPlayingView;
    private ToggleButton mTBPlaySongBottom;

    private List<Song> mListSong;
    private ICommunicate iCommunicate;
    private boolean mIsLand;
    private boolean mIsPlaying;
    private int mCurrentIndexSong = -1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ICommunicate) {
            iCommunicate = (ICommunicate) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mIsLand = getArguments().getBoolean(ActivityMusic.KEY_IS_LAND, false);
        mIsPlaying = getArguments().getBoolean(ActivityMusic.KEY_MUSIC_PLAYING, false);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_all_songs, container, false);
            if (mListSong != null) {
                mSongAdapter = new SongAdapter(mListSong, getContext(), this);
                mSongAdapter.setIndex(mCurrentIndexSong);
            }

            mRVSongs = mRootView.findViewById(R.id.rv_list_song);
            mRVSongs.setAdapter(mSongAdapter);
            mRVSongs.setLayoutManager(new LinearLayoutManager(getContext()));

            mNowPlayingView = mRootView.findViewById(R.id.v_now_playing);
            mNowPlayingView.setOnClickListener(v -> {
                iCommunicate.transition(mCurrentIndexSong);
            });

            if (!mIsLand && savedInstanceState != null) {
                if (mListSong != null) {
                    displayNowPlayingView(mCurrentIndexSong);
                }
            }
        }
        return mRootView;
    }

    @Override
    public void onDestroyView() {
        if (mRootView.getParent() != null) {
            ((ViewGroup)mRootView.getParent()).removeView(mRootView);
        }
        super.onDestroyView();
    }

    @Override
    public void displayNowPlayingView(int position) {
        mCurrentIndexSong = position;
        if (mCurrentIndexSong != -1) {
            iCommunicate.playMusic(mCurrentIndexSong);
            iCommunicate.startService();
        }
        if (!mIsLand) {
            if (mCurrentIndexSong != -1) {

                Song currentSong = mListSong.get(position);
                int lengthAllow = getResources().getInteger(R.integer.length_in_line);
                mNowPlayingView.setVisibility(View.VISIBLE);
                TextView name = mNowPlayingView.findViewById(R.id.tv_song_name_now_playing);
                mTBPlaySongBottom = mNowPlayingView.findViewById(R.id.toggle_play_pause);
                Log.d("MinhNTn", "displayNowPlayingView: " + mIsPlaying);
                mTBPlaySongBottom.forceLayout();
                mTBPlaySongBottom.setChecked(!mIsPlaying);

                mTBPlaySongBottom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            iCommunicate.pauseMusic();
                        } else {
                            iCommunicate.resumeMusic();
                        }
                    }
                });

                String nameDisplay = (currentSong.getTitle().length() < lengthAllow)? currentSong.getTitle()
                        : currentSong.getTitle().substring(0, lengthAllow - 3) + "..";
                name.setText(nameDisplay);
                new DBAsyncTask().execute(currentSong.getID());
            }
            iCommunicate.passCurrentPositionIfPortrait(position);
        } else {
            iCommunicate.transition(mCurrentIndexSong);
        }
    }

    @Override
    public void setStatePlay(boolean state) {
        mIsPlaying = state;
    }

    public void setListSong(List<Song> list) {
        mListSong = list;
    }

    public void notifyAdapter(List<Song> list) {
        mListSong.clear();
        mListSong.addAll(list);
        mSongAdapter.notifyDataSetChanged();
    }

    public void setAdapterIndex(int index) {
        mCurrentIndexSong = index;
    }

    public void setButtonState(boolean state) {
        if (mTBPlaySongBottom != null) {
            mTBPlaySongBottom.setChecked(!state);
        }
    }

    class DBAsyncTask extends AsyncTask<Integer, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Integer... integers) {
            return ((ActivityMusic) getContext()).getMusicDBHelper().getInfoNowPlayingSong(integers[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            TextView album = mNowPlayingView.findViewById(R.id.tv_song_album_now_playing);
            ImageView cover = mNowPlayingView.findViewById(R.id.iv_album_cover);

            cursor.moveToFirst();
            String albumName = cursor.getString(0);
            byte[] albumCover = cursor.getBlob(1);

            int lengthAllow = getResources().getInteger(R.integer.length_in_line);
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);
            String albumDisplay = (albumName.length() <= lengthAllow)? albumName : albumName.substring(0, lengthAllow - 3) + "..";

            cover.setImageBitmap(bitmap);
            album.setText(albumDisplay);
            mTBPlaySongBottom.setChecked(!mIsPlaying);
        }
    }

}
