package com.minhntn.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.minhntn.music.model.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> mListSong;
    private Context mContext;

    public SongAdapter(List<Song> mListSong, Context mContext) {
        this.mListSong = mListSong;
        this.mContext = mContext;
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
        holder.mTVOrderNumber.setText(String.valueOf(position + 1));
        holder.mTVSongName.setText(song.getTitle());
        holder.mTVSongTime.setText(song.getDurationTimeFormat());
    }

    @Override
    public int getItemCount() {
        return mListSong.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        TextView mTVOrderNumber;
        TextView mTVSongName;
        TextView mTVSongTime;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            mTVOrderNumber = itemView.findViewById(R.id.tv_order_number);
            mTVSongName = itemView.findViewById(R.id.tv_song_name);
            mTVSongTime = itemView.findViewById(R.id.tv_song_time);
        }

    }
}
