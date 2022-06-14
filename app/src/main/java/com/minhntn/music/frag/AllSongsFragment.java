package com.minhntn.music.frag;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.minhntn.music.adapter.SongAdapter;
import com.minhntn.music.interf.ICallBack;
import com.minhntn.music.interf.ICommunicate;
import com.minhntn.music.model.Song;
import com.minhntn.music.prov.MusicContacts;

import java.util.List;
import java.util.Random;

public class AllSongsFragment extends Fragment implements ICallBack {
    public static final String FRAGMENT_TAG = "AllSongsFragment";

    private View mRootView;
    private RecyclerView mRVSongs;
    private SongAdapter mSongAdapter;
    private View mNowPlayingView;
    private ToggleButton mTBPlaySongBottom;

    private List<Song> mListSong;
    private ICommunicate mICommunicate;
    private boolean mIsLand;
    private boolean mIsPlaying;
    private boolean mIsFromPause;
    private int mCurrentIndexSong = -1;
    private boolean mIsServiceAlive;

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
        mIsPlaying = getArguments().getBoolean(ActivityMusic.KEY_MUSIC_PLAYING, false);
        mIsServiceAlive = getArguments().getBoolean(MusicContacts.PREF_SERVICE_ALIVE, false);


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
                mICommunicate.transition(mCurrentIndexSong);
            });

            if (!mIsLand && savedInstanceState != null) {
                if (mListSong != null) {
                    displayNowPlayingView(mCurrentIndexSong, true);
                }
            }

        }
        return mRootView;
    }

    @Override
    public void displayNowPlayingView(int position, boolean isClicked) {
        mCurrentIndexSong = position;
        if (!mIsLand) {
            if (mCurrentIndexSong != -1 && getContext() != null) {
                Song currentSong = mListSong.get(position);
                int lengthAllow = getResources().getInteger(R.integer.length_in_line);
                mNowPlayingView.setVisibility(View.VISIBLE);
                TextView name = mNowPlayingView.findViewById(R.id.tv_song_name_now_playing);
                if (mTBPlaySongBottom == null) {
                    mTBPlaySongBottom = mNowPlayingView.findViewById(R.id.toggle_play_pause);
                }
                mTBPlaySongBottom.forceLayout();

                mTBPlaySongBottom.setChecked(!mIsPlaying);

                if (isClicked) {
                    mTBPlaySongBottom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                mICommunicate.pauseMusic();
//                                mIsPlaying = false;
                            } else {
                                mICommunicate.resumeMusic();
                                if (!mIsServiceAlive) {
                                    mICommunicate.startService();
                                    mIsServiceAlive = true;
                                    getArguments().putBoolean(MusicContacts.PREF_SERVICE_ALIVE, true);
                                    mIsPlaying = true;
                                }
//                                mIsPlaying = true;
                            }
                        }
                    });
                }

                String nameDisplay = (currentSong.getTitle().length() < lengthAllow)? currentSong.getTitle()
                        : currentSong.getTitle().substring(0, lengthAllow - 3) + "..";
                name.setText(nameDisplay);
                new DBAsyncTask().execute(currentSong.getID());
            }
            mICommunicate.passCurrentPositionIfPortrait(position);
        } else {
            mICommunicate.transition(mCurrentIndexSong);
        }

        // start playing music
        if (mCurrentIndexSong != -1) {
            if (!mIsFromPause) {
                if (!mIsServiceAlive) {
                    mICommunicate.startService();
                    mIsServiceAlive = true;
                    getArguments().putBoolean(MusicContacts.PREF_SERVICE_ALIVE, true);
                    mIsPlaying = true;
                }
                mICommunicate.playMusic(mCurrentIndexSong);
            }
        }
    }

    @Override
    public void setStatePlay(boolean state) {
        mIsFromPause = false;
        mICommunicate.setStatePlaying(state);
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
            mIsPlaying = state;
        }
    }

    public void nextSong() {
        mCurrentIndexSong++;
        if (mCurrentIndexSong >= mListSong.size()) {
            mCurrentIndexSong = 0;
        }
        SongAdapter.SongViewHolder viewHolder = (SongAdapter.SongViewHolder) mRVSongs.findViewHolderForAdapterPosition(mCurrentIndexSong);
        if (viewHolder != null) {
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
            viewHolder.onClick(viewHolder.itemView);
        } else {
            mSongAdapter.playRandom(mCurrentIndexSong);
        }
    }

    public void onResumeFromScreen(int position) {
        mIsFromPause = true;
        displayNowPlayingView(position, true);
        mRVSongs.smoothScrollToPosition(position);
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
