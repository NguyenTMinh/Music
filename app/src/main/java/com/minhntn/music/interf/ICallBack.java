package com.minhntn.music.interf;

import com.minhntn.music.model.Song;

public interface ICallBack {
    void displayNowPlayingView(Song song, int position);
}
