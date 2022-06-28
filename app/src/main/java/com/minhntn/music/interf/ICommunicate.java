package com.minhntn.music.interf;

import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;

public interface ICommunicate {
    void transition(int position);
    void hideActionBar();
    void passCurrentPositionIfPortrait();
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
    void displayControlMedia(int position, String fromFrag);
    void setIsFromPause(boolean fromPause);
    boolean isSongOnList();

    // Control service
    void startService();

    // update fav table
    void updateOnAddingNewFavorite(int rowCount, Song currentSong, String keyFrag);
    void updateCountPlayDatabase(int rowCount, Song currentSong);
    void updateOnDislikeButton(int rowCount, Song song);
    void updateRemoveFavorite(int rowCount, int index, Song songToChange, boolean isRemoveItemPlaying);
    void removeFromDatabase(int rowCount, Song song);

}
