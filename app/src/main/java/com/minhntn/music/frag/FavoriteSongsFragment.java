package com.minhntn.music.frag;

import com.minhntn.music.R;

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
            mICommunicate.removeFavorite(id);
        }
    }

}
