package com.minhntn.music.frag;

import com.minhntn.music.ActivityMusic;
import com.minhntn.music.R;

public class AllSongsFragment extends BaseSongListFragment {
    public static final String FRAGMENT_TAG = "AllSongsFragment";

    @Override
    protected int getMenuRes() {
        return R.menu.item_menu_all_songs;
    }

    @Override
    public void updateSong(int index) {
        mICommunicate.updateOnLikeButton(mListSong.get(index).getID(), true, index);
    }
}
