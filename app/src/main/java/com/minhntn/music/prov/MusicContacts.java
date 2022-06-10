package com.minhntn.music.prov;

public class MusicContacts {
    public static final String SHARED_PREF_NAME = "com.minhntn.music.prov.spref";
    public static final String PREF_SONG_CURRENT = "PREF_SONG_CURRENT";
    public static final String PREF_APP_CLOSED = "PREF_APP_CLOSED";
    public static final String PREF_IS_CREATED = "is_created";
    public static final String MUSIC_DB_NAME = "music.db";
    public static final int MUSIC_DB_VERSION = 1;

    public static final String VIEW_JOIN_FOR_NOW_PLAY = "A";

    // Table: Song
    public static final String SONG_TABLE_NAME = "song";
    public static final String SONG_COLUMN_ID = "id";
    public static final String SONG_COLUMN_NAME = "name";
    public static final String SONG_COLUMN_DURATION = "duration";
    public static final String SONG_COLUMN_URI_SONG = "uri";
    public static final String SONG_COLUMN_ARTIST_NAME = "artist";
    public static final String SONG_COLUMN_ALBUM_ID = "album_id";

    // Table: Album
    public static final String ALBUM_TABLE_NAME = "album";
    public static final String ALBUM_COLUMN_ID = "id";
    public static final String ALBUM_COLUMN_NAME = "name";
    public static final String ALBUM_COLUMN_ART = "art";

}
