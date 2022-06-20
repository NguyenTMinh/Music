package com.minhntn.music.frag;

import com.minhntn.music.R;

public class FavoriteSongsFragment extends BaseSongListFragment{
    public static final String TAG = "FavoriteSongsFragment";

    @Override
    protected int getMenuRes() {
        return R.menu.item_menu_fav_songs;
    }

    @Override
    public void updateSong(int index) {
        int id = mListSong.get(index).getID();
        mICommunicate.removeFavorite(id);
    }
}
