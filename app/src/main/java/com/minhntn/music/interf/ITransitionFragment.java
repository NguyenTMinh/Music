package com.minhntn.music.interf;

import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;

public interface ITransitionFragment {
    void transition(Song song);
    void hideActionBar();
}
