package com.minhntn.music.frag;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.minhntn.music.R;
import com.minhntn.music.model.Song;
import com.minhntn.music.prov.MusicContacts;

public class FavoriteSongsFragment extends BaseSongListFragment{
    public static final String FRAGMENT_TAG = "FavoriteSongsFragment";
    public static final String ACTION_REMOVE_FAVORITE = "ACTION_REMOVE_FAVORITE";

    @Override
    protected int getMenuRes() {
        return R.menu.item_menu_fav_songs;
    }

    @Override
    protected String getFragTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public void updateSong(int index, String action) {
        int id = mListSong.get(index).getID();
        if (action.equals(ACTION_REMOVE_FAVORITE)) {
            removeFavorite(mListSong.get(index), index);
        }
    }

    public void notifyAddFavSong() {
        mSongAdapter.notifyDataSetChanged();
    }

    private void removeFavorite(Song songToChange, int index) {
        if (getContext() != null) {
            boolean isRemoveItemPlaying;
            ContentValues values = new ContentValues();
            Uri newUri = Uri.parse(MusicContacts.CONTENT_URI.toString() + "/" + songToChange.getID());

            if (index != mCurrentIndexSong) {
                isRemoveItemPlaying = false;
                if (mCurrentIndexSong > index) {
                    mCurrentIndexSong--;
                }
            } else {
                isRemoveItemPlaying = true;
                mCurrentIndexSong = -1;
            }

            songToChange.resetCountOfPlay();
            songToChange.setIsFavorite(false);
            songToChange.setFavLevel(0);
            values.put(MusicContacts.FAVORITE_COLUMN_IS_FAVORITE, songToChange.getFavLevel());
            values.put(MusicContacts.FAVORITE_COLUMN_COUNT_OF_PLAY, songToChange.getCountOfPlay());
            int row = getContext().getContentResolver().update(newUri, values, null, null);

            mICommunicate.updateRemoveFavorite(row, index, songToChange, isRemoveItemPlaying);

            mListSong.remove(songToChange);
            notifyAdapter();
        }
    }
}
