package com.minhntn.music.frag;

import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.model.Song;
import com.minhntn.music.prov.MusicContacts;

public class AllSongsFragment extends BaseSongListFragment {
    public static final String FRAGMENT_TAG = "AllSongsFragment";
    public static final String ACTION_DELETE_SONG = "ACTION_DELETE_SONG";
    public static final String ACTION_ADD_FAVORITE = "ACTION_ADD_FAVORITE";

    @Override
    protected int getMenuRes() {
        return R.menu.item_menu_all_songs;
    }

    @Override
    protected String getFragTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public void updateSong(int index, String action) {
        if (action.equals(ACTION_ADD_FAVORITE)) {
            updateOnAddFavorite(mListSong.get(index));
        } else {
            if (index < mCurrentIndexSong) {
                mCurrentIndexSong--;
            }
            removeSongFromDatabase(mListSong.get(index), index);
        }
    }

    private void updateOnAddFavorite(Song currentSong) {
        if (getContext() != null) {
            ContentValues values = new ContentValues();
            Uri newUri = Uri.parse(MusicContacts.CONTENT_URI.toString() + "/" + currentSong.getID());

            currentSong.setFavLevel(2);
            currentSong.setIsFavorite(true);
            currentSong.setDislike(false);

            values.put(MusicContacts.FAVORITE_COLUMN_IS_FAVORITE, currentSong.getFavLevel());
            int row = getContext().getContentResolver().update(newUri, values, null, null);
            mICommunicate.updateOnAddingNewFavorite(row, currentSong, FRAGMENT_TAG);
            Log.d("MinhNTn", "updateOnAddFavorite: " + mSongAdapter.getmIndex());
        }
    }

    private void removeSongFromDatabase(Song songToChange, int index) {
        if (getContext() != null) {
            Uri newUri = Uri.parse(MusicContacts.CONTENT_URI.toString() + "/" + songToChange.getID());
            int row = getContext().getContentResolver().delete(newUri, null, null);

            mICommunicate.removeFromDatabase(row, songToChange);
            mListSong.remove(songToChange);
            notifyAdapter();
        }
    }
}
