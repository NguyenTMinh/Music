package com.minhntn.music.frag;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.SongAdapter;
import com.minhntn.music.interf.ICallBack;
import com.minhntn.music.interf.ITransitionFragment;
import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;

import java.util.List;

public class AllSongsFragment extends Fragment implements ICallBack {

    private View mRootView;
    private RecyclerView mRVSongs;
    private SongAdapter mSongAdapter;
    private View mNowPlayingView;
    private List<Song> mListSong;
    private ITransitionFragment iTransitionFragment;
    private Song mCurrentSong;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ITransitionFragment) {
            iTransitionFragment = (ITransitionFragment) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_all_songs, container, false);
            mSongAdapter = new SongAdapter(mListSong, getContext(), this);
            mRVSongs = mRootView.findViewById(R.id.rv_list_song);
            mRVSongs.setAdapter(mSongAdapter);
            mRVSongs.setLayoutManager(new LinearLayoutManager(getContext()));

            mNowPlayingView = mRootView.findViewById(R.id.v_now_playing);
            mNowPlayingView.setOnClickListener(v -> {
                iTransitionFragment.transition(mCurrentSong);
            });
        } else {

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
    public void displayNowPlayingView(Song song, int position) {
        if (position != -1) {
            SongAdapter.SongViewHolder viewHolder = (SongAdapter.SongViewHolder) mRVSongs.findViewHolderForAdapterPosition(position);
            if ( viewHolder != null) {
                TextView textView = ((TextView) viewHolder.itemView.findViewById(R.id.tv_order_number));
                TextView textView1 = viewHolder.itemView.findViewById(R.id.tv_song_name);

                textView.setText(String.valueOf(position + 1));
                textView.setBackgroundResource(0);
                textView1.setTypeface(null, Typeface.NORMAL);
            }
        }

        mNowPlayingView.setVisibility(View.VISIBLE);
        TextView name = mNowPlayingView.findViewById(R.id.tv_song_name_now_playing);
        name.setText(song.getTitle());
        new DBAsyncTask().execute(song.getID());
        mCurrentSong = song;
    }

    public void setListSong(List<Song> list) {
        mListSong = list;
    }

    public void notifyAdapter() {
        mSongAdapter.notifyDataSetChanged();
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

            Bitmap bitmap = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);

            cover.setImageBitmap(bitmap);
            album.setText(albumName);
        }
    }

}
