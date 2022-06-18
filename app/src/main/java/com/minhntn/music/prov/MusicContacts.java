package com.minhntn.music.prov;

import android.net.Uri;

public class MusicContacts {
    public static final String SHARED_PREF_NAME = "com.minhntn.music.prov.spref";
    public static final String PREF_SONG_CURRENT = "PREF_SONG_CURRENT";
    public static final String PREF_SONG_PLAY_MODE = "PREF_SONG_PLAY_MODE";
    public static final String PREF_SERVICE_ALIVE = "PREF_SERVICE_ALIVE";
    public static final String PREF_MUSIC_PLAYING = "PREF_MUSIC_PLAYING";
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
    public static final String SONG_COLUMN_FAVORITE = "favorite";

    // Table: Album
    public static final String ALBUM_TABLE_NAME = "album";
    public static final String ALBUM_COLUMN_ID = "id";
    public static final String ALBUM_COLUMN_NAME = "name";
    public static final String ALBUM_COLUMN_ART = "art";

    // Table: Favorite
    public static final String FAVORITE_TABLE_NAME = "favorite";
    public static final String FAVORITE_COLUMN_ID = "ID";
    public static final String FAVORITE_COLUMN_ID_PROVIDER = "ID_PROVIDER";
    public static final String FAVORITE_COLUMN_IS_FAVORITE = "IS_FAVORITE";
    public static final String FAVORITE_COLUMN_COUNT_OF_PLAY = "COUNT_OF_PLAY";

    // Provider
    public static final String SINGLE_FAV_MIME_TYPE =
            "vnd.android.cursor.item/com.minhntn.music.databases.favorite";
    public static final String MULTIPLE_FAV_MIME_TYPE =
            "vnd.android.cursor.dir/com.minhntn.music.databases.favorite";
    public static final String AUTHORITY =
            "com.minhntn.music.FavoriteSongsProvider";
    public static final String CONTENT_PATH = "favorite";
    public static final String URL = "content://" + AUTHORITY + "/" + CONTENT_PATH;
    public static final Uri CONTENT_URI = Uri.parse(URL);
}
