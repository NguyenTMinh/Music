package com.minhntn.music.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.minhntn.music.R;
import com.minhntn.music.frag.AllSongsFragment;
import com.minhntn.music.frag.FavoriteSongsFragment;
import com.minhntn.music.interf.ICallBack;
import com.minhntn.music.model.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> mListSong;
    private Context mContext;
    private ICallBack mICallBack;
    private int mIndex = -1;
    private boolean mIsClickedOnItem;
    private int mResMenu;

    public SongAdapter(List<Song> mListSong, Context mContext, ICallBack iCallBack, int mResMenu) {
        this.mListSong = mListSong;
        this.mContext = mContext;
        this.mResMenu = mResMenu;
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

        if (song.isPlaying()) {
            holder.mTVSongName.setTypeface(null, Typeface.BOLD);
            holder.mTVOrderNumber.setText("");
            holder.mTVOrderNumber.setBackgroundResource(R.drawable.ic_now_playing);
        } else {
            holder.mTVSongName.setTypeface(null, Typeface.NORMAL);
            holder.mTVOrderNumber.setText(String.valueOf(position + 1));
            holder.mTVOrderNumber.setBackgroundResource(0);
        }

        holder.mTVSongName.setText(song.getTitle());
        holder.mTVSongTime.setText(song.getDurationTimeFormat());
    }

    @Override
    public int getItemCount() {
        return mListSong.size();
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTVOrderNumber;
        TextView mTVSongName;
        TextView mTVSongTime;
        ImageButton mIBItemMenu;
        PopupMenu popupMenu;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            mTVOrderNumber = itemView.findViewById(R.id.tv_order_number);
            mTVSongName = itemView.findViewById(R.id.tv_song_name);
            mTVSongTime = itemView.findViewById(R.id.tv_song_time);
            mIBItemMenu = itemView.findViewById(R.id.bt_song_item_menu);
            itemView.setOnClickListener(this);
            mIBItemMenu.setOnClickListener(this);

            popupMenu = new PopupMenu(mContext, mIBItemMenu);
            popupMenu.getMenuInflater().inflate(mResMenu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (mResMenu == R.menu.item_menu_all_songs) {
                        Log.d("MinhNTn", "onMenuItemClick: " + mIndex + "," + getAdapterPosition());
                        switch (item.getItemId()) {
                            case R.id.remove_list: {
                                if (getAdapterPosition() < mIndex) {
                                    mIndex--;
                                }
                                mICallBack.updateSong(getAdapterPosition(), AllSongsFragment.ACTION_DELETE_SONG);
                                break;
                            }
                            case R.id.add_fav: {
                                mListSong.get(getAdapterPosition()).setIsFavorite(true);
                                mICallBack.updateSong(getAdapterPosition(), AllSongsFragment.ACTION_ADD_FAVORITE);
                                break;
                            }
                        }
                    } else {
                        if (item.getItemId() == R.id.remove_fav) {
                            // Decrease index if the song remove has position lower than the current song playing
                            if (getAdapterPosition() < mIndex) {
                                mIndex--;
                            }
                            mICallBack.setSongOnList();
                            mICallBack.updateSong(getAdapterPosition(), FavoriteSongsFragment.ACTION_REMOVE_FAVORITE);
                        }
                    }
                    return true;
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == itemView.getId()) {
                if (mIndex != -1) {
                    Song song = mListSong.get(mIndex);
                    song.setPlaying(false);
                    notifyItemChanged(mIndex);// update lai bai hat truoc do
                }
                mIndex = getAdapterPosition(); // click vi tri hien tai

                Song songCurrent = mListSong.get(mIndex);
                mICallBack.setStatePlay(true);
                mICallBack.displayNowPlayingView(mIndex);
                songCurrent.setPlaying(true);

                mTVOrderNumber.setText("");
                mTVOrderNumber.setBackgroundResource(R.drawable.ic_now_playing);
                mTVSongName.setTypeface(null, Typeface.BOLD);

                notifyItemChanged(mIndex);

                if (!mIsClickedOnItem) {
                    mICallBack.increaseCount();
                } else {
                    mIsClickedOnItem = false;
                }
            }
            if (v.getId() == R.id.bt_song_item_menu) {
                popupMenu.show();
            }
        }

    }

    public void playNextSongIfViewHolderNull() {
        if (mIndex != -1) {
            Song song = mListSong.get(mIndex);
            song.setPlaying(false);
            notifyItemChanged(mIndex);
        }
        mIndex++;
        if (mIndex >= mListSong.size()) {
            mIndex = 0;
        }

        Song songCurrent = mListSong.get(mIndex);
        songCurrent.setPlaying(true);
        mICallBack.setStatePlay(true);
        mICallBack.displayNowPlayingView(mIndex);
        notifyItemChanged(mIndex);
    }

    public void playPreviousSongIfViewHolderNull() {
        if (mIndex != -1) {
            Song song = mListSong.get(mIndex);
            song.setPlaying(false);
            notifyItemChanged(mIndex);
        }
        mIndex--;
        if (mIndex < 0) {
            mIndex = mListSong.size() -1;
        }

        Song songCurrent = mListSong.get(mIndex);
        songCurrent.setPlaying(true);
        mICallBack.setStatePlay(true);
        mICallBack.displayNowPlayingView(mIndex);
        notifyItemChanged(mIndex);
    }

    public void playRandom(int index) {
        if (mIndex != -1) {
            Song song = mListSong.get(mIndex);
            song.setPlaying(false);
            notifyItemChanged(mIndex);
        }
        mIndex = index;
        Song songCurrent = mListSong.get(mIndex);
        songCurrent.setPlaying(true);
        mICallBack.setStatePlay(true);
        mICallBack.displayNowPlayingView(mIndex);
        notifyItemChanged(mIndex);
    }

    public void setIsClickedOnItem(boolean mIsClickedOnItem) {
        this.mIsClickedOnItem = mIsClickedOnItem;
    }

    public int getmIndex() {
        return mIndex;
    }

}
