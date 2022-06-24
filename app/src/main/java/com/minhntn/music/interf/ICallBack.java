package com.minhntn.music.interf;

import com.minhntn.music.model.Song;

public interface ICallBack {
    void displayNowPlayingView(int position, boolean isClicked);
    void setStatePlay(boolean state);
    void increaseCount();
    void updateSong(int index, String action);
    void setSongOnList();
}
