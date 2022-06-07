package com.minhntn.music.interf;

import com.minhntn.music.model.Song;

public interface ICallBack {
    void displayNowPlayingView(int position);
    void setStatePlay(boolean state);
}
