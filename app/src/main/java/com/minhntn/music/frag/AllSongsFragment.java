package com.minhntn.music.frag;

import android.util.Log;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;
import com.minhntn.music.model.Song;

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
            mICommunicate.updateOnLikeButton(mListSong.get(index).getID(), true, index, true);
        } else {
            if (index < mCurrentIndexSong) {
                mCurrentIndexSong--;
            }
            mICommunicate.removeFromDatabase(mListSong.get(index).getID());
        }
    }

}
