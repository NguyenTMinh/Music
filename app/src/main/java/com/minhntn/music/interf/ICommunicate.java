package com.minhntn.music.interf;

import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;

public interface ICommunicate {
    void transition(int position);
    void hideActionBar();
    void passCurrentPositionIfPortrait(int position);
    void playMusic(int position);
    void pauseMusic(boolean fromService);
    void resumeMusic(boolean fromService);
    int getTimeCurrentPlay();
    void seekTimeTo(int time);
    boolean isMusicPlaying();
    void playNextSong();
    void playPreviousSong();
    void playRandom();
    void playRepeatOneSong();
    void setStatePlaying(boolean state);
    void setModePlay(int modePlay);
    void setPauseButton(boolean state);
    void displayControlMedia(int position, boolean isClicked, String fromFrag);
    void setIsFromPause(boolean fromPause);
    boolean isSongOnList();
    void setSongOnList(boolean onList);

    // Control service
    void startService();

    // update fav table
    void updateOnLikeButton(int id, boolean isChecked, int index, boolean active);
    void updateCountPlay(int id);
    void updateOnDislikeButton(int id, boolean isChecked, boolean active);
    void removeFavorite(int id);
    void removeFromDatabase(int id);

}
