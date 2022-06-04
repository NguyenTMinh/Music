package com.minhntn.music;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.minhntn.music.interf.ICallBack;
import com.minhntn.music.model.Song;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> mListSong;
    private Context mContext;
    private ICallBack mICallBack;
    private int index = -1;
    private int mPreIndex = -1;

    public SongAdapter(List<Song> mListSong, Context mContext, ICallBack iCallBack) {
        this.mListSong = mListSong;
        this.mContext = mContext;
        mICallBack = iCallBack;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = mListSong.get(position);

        if (holder.getAdapterPosition() == mPreIndex) {
            Log.d("MinhNTn", "onBindViewHolder: set " + mPreIndex);
            holder.mTVSongName.setTypeface(null, Typeface.NORMAL);
            holder.mTVOrderNumber.setBackgroundResource(0);
        }
        holder.mTVOrderNumber.setText(String.valueOf(position + 1));
        holder.mTVSongName.setText(song.getTitle());
        holder.mTVSongTime.setText(song.getDurationTimeFormat());
    }

    @Override
    public int getItemCount() {
        return mListSong.size();
    }

    public void setPreIndex(int preIndex) {
        mPreIndex = preIndex;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTVOrderNumber;
        TextView mTVSongName;
        TextView mTVSongTime;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            mTVOrderNumber = itemView.findViewById(R.id.tv_order_number);
            mTVSongName = itemView.findViewById(R.id.tv_song_name);
            mTVSongTime = itemView.findViewById(R.id.tv_song_time);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mICallBack.displayNowPlayingView(mListSong.get(getAdapterPosition()), index);

            mTVOrderNumber.setText("");
            mTVOrderNumber.setBackgroundResource(R.drawable.ic_now_playing);
            mTVSongName.setTypeface(null, Typeface.BOLD);

            index = getAdapterPosition();
        }
    }
}
