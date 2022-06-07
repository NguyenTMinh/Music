package com.minhntn.music.interf;

import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;

public interface ITransitionFragment {
    void transition(int position);
    void hideActionBar();
    void passCurrentPositionIfPortrait(int position);
    void playMusic(int position);
    void pauseMusic();
    void resumeMusic();
    int getTimeCurrentPlay();
    void startService();
}
