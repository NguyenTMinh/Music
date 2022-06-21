package com.minhntn.music.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.minhntn.music.R;
import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;
import com.minhntn.music.prov.MusicContacts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MusicDBHelper extends SQLiteOpenHelper {
    private WeakReference<Context> mContextWeakReference;

    public MusicDBHelper(Context context) {
        super(context, MusicContacts.MUSIC_DB_NAME, null, MusicContacts.MUSIC_DB_VERSION);
        mContextWeakReference = new WeakReference<>(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create song table
        String queryCreateSongTable = "CREATE TABLE " + MusicContacts.SONG_TABLE_NAME + "(" +
                MusicContacts.SONG_COLUMN_ID + " INTEGER PRIMARY KEY," +
                MusicContacts.SONG_COLUMN_NAME + " TEXT NOT NULL," +
                MusicContacts.SONG_COLUMN_DURATION + " LONG NOT NULL," +
                MusicContacts.SONG_COLUMN_URI_SONG + " TEXT NOT NULL," +
                MusicContacts.SONG_COLUMN_ARTIST_NAME + " TEXT NOT NULL," +
                MusicContacts.SONG_COLUMN_ALBUM_ID + " INTEGER NOT NULL," +
                MusicContacts.SONG_COLUMN_FAVORITE + " INTEGER DEFAULT -1," +
                MusicContacts.SONG_COLUMN_DISLIKE + " INTEGER DEFAULT 0" + ")";

        // Create album table
        String queryCreateAlbumTable = "CREATE TABLE " + MusicContacts.ALBUM_TABLE_NAME + "(" +
                MusicContacts.ALBUM_COLUMN_ID + " INTEGER PRIMARY KEY," +
                MusicContacts.ALBUM_COLUMN_NAME + " TEXT NOT NULL," +
                MusicContacts.ALBUM_COLUMN_ART + " BLOB" + ")";

        // Create favorite table
        String queryCreateFavTable = "CREATE TABLE " + MusicContacts.FAVORITE_TABLE_NAME + "(" +
                MusicContacts.FAVORITE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MusicContacts.FAVORITE_COLUMN_ID_PROVIDER + " INTEGER, " +
                MusicContacts.FAVORITE_COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0, " +
                MusicContacts.FAVORITE_COLUMN_COUNT_OF_PLAY + " INTEGER DEFAULT 0" + ")";

        db.execSQL(queryCreateSongTable);
        db.execSQL(queryCreateAlbumTable);
        db.execSQL(queryCreateFavTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MusicContacts.SONG_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MusicContacts.ALBUM_TABLE_NAME);
        onCreate(db);
    }

    /**
     * @param song
     * @param album
     */
    public void insertTB(Song song, Album album) {
        SQLiteDatabase database = getWritableDatabase();

        // Insert new value to album table
        Album tempAlbum = getAlbumWithExactName(album.getName());
        int tempAlbumID;
        if (tempAlbum != null) {
            tempAlbumID = tempAlbum.getID();
        } else {
            String insertAlbums = "INSERT INTO " + MusicContacts.ALBUM_TABLE_NAME + " VALUES (null, ?, ?)";
            SQLiteStatement ssm = database.compileStatement(insertAlbums);
            ssm.clearBindings();
            ssm.bindString(1, album.getName());
            ssm.bindBlob(2, album.getArt());
            ssm.execute();

            tempAlbumID = getAlbumWithExactName(album.getName()).getID();
        }

        /* Insert new value to song table
         * But first check the exist of row
         * */
        Song song1 = getSongWithExactDetail(song);
        if (song1 == null) {
            String songID = String.valueOf(song.getID());
            String songTitle = song.getTitle();
            String songDuration = String.valueOf(song.getDuration());
            String songUri = song.getUri().toString();
            String songArtist = song.getmArtist();
            String songIDAlbum = String.valueOf(tempAlbumID);
            // Insert new song to Song table
            String[] paramsSong = {songID, songTitle, songDuration, songUri, songArtist, songIDAlbum};
            database.execSQL("INSERT INTO " + MusicContacts.SONG_TABLE_NAME
                            + "(" + MusicContacts.SONG_COLUMN_ID + "," + MusicContacts.SONG_COLUMN_NAME + ","
                            + MusicContacts.SONG_COLUMN_DURATION + "," + MusicContacts.SONG_COLUMN_URI_SONG + ","
                            + MusicContacts.SONG_COLUMN_ARTIST_NAME + "," + MusicContacts.SONG_COLUMN_ALBUM_ID + ")"
                            +" VALUES (?, ?, ?, ?, ?, ?)",
                    paramsSong);

            // Insert new song to favorite table
            String[] paramsFav = {songID};
            database.execSQL("INSERT INTO " + MusicContacts.FAVORITE_TABLE_NAME + "(" + MusicContacts.FAVORITE_COLUMN_ID_PROVIDER
                    +")" + " VALUES (?)", paramsFav);
        }
    }

    private Album getAlbumWithExactName(String albumName) {
        Album album = null;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(MusicContacts.ALBUM_TABLE_NAME, null,
                MusicContacts.ALBUM_COLUMN_NAME + " = ?", new String[]{albumName},
                null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            album = new Album(cursor.getInt(0), cursor.getString(1),
                    cursor.getBlob(2));
            cursor.moveToNext();
        }
        return album;
    }

    private Song getSongWithExactDetail(Song song) {
        Song temp = null;
        SQLiteDatabase database = getReadableDatabase();
        String[] selection = {String.valueOf(song.getID())};

        // Query song table for info
        Cursor cursorSong = database.query(MusicContacts.SONG_TABLE_NAME, null,
                MusicContacts.SONG_COLUMN_ID + " = ?", selection, null, null, null);
        cursorSong.moveToFirst();

        // Query favorite table for info
        Cursor cursorFav = database.query(MusicContacts.FAVORITE_TABLE_NAME, null,
                MusicContacts.FAVORITE_COLUMN_ID_PROVIDER + "= ?", selection, null, null, null);
        cursorFav.moveToFirst();

        if (!cursorSong.isAfterLast() && !cursorFav.isAfterLast()) {
            boolean isDislike = (cursorSong.getInt(7) == 1);
            temp = new Song(cursorSong.getInt(0), cursorSong.getString(1), cursorSong.getLong(2),
                    cursorSong.getString(3), cursorSong.getString(4), cursorSong.getInt(5),
                    cursorSong.getInt(6), cursorFav.getInt(3), cursorFav.getInt(2), isDislike);
        }
        return temp;
    }

    public List<Song> getAllSongs() {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursorSong = database.query(MusicContacts.SONG_TABLE_NAME, null, null,
                null, null, null, null);
        cursorSong.moveToFirst();

        // Query favorite table for info
        Cursor cursorFav = database.query(MusicContacts.FAVORITE_TABLE_NAME, null, null,
                null, null, null, null);
        cursorFav.moveToFirst();

        while (!cursorSong.isAfterLast() && !cursorFav.isAfterLast()) {
            boolean isDislike = (cursorSong.getInt(7) == 1);
            list.add(new Song(cursorSong.getInt(0), cursorSong.getString(1), cursorSong.getLong(2),
                    cursorSong.getString(3), cursorSong.getString(4), cursorSong.getInt(5),
                    cursorSong.getInt(6), cursorFav.getInt(3), cursorFav.getInt(2), isDislike));
            cursorSong.moveToNext();
        }
        return list;
    }

    public List<Album> getAllAlbums() {
        List<Album> list = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(MusicContacts.ALBUM_TABLE_NAME, null, null,
                null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(new Album(cursor.getInt(0), cursor.getString(1), cursor.getBlob(2)));
            cursor.moveToNext();
        }
        return list;
    }

    public void loadDataFromMedia() {
        Cursor cursor = mContextWeakReference.get().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int songID = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            String songUri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

            String songTitle2 = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            long songDuration2 = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            String artistName2 = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            String albumName2 = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(songUri);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            byte[] imageCover = mmr.getEmbeddedPicture();
            if (imageCover == null) {
                Bitmap bitmap = BitmapFactory.decodeResource(mContextWeakReference.get().getResources(),
                        R.drawable.bg_default_album_art);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imageCover = stream.toByteArray();
                bitmap.recycle();
            }

            Song song = new Song(songID, songTitle2, songDuration2, songUri, artistName2, albumId, 0, 0, 0,false);
            Album album = new Album(albumId, albumName2, imageCover);
            insertTB(song, album);

            cursor.moveToNext();
        }
    }

    public Cursor getInfoNowPlayingSong(int songID) {
        SQLiteDatabase database = getReadableDatabase();
        String queryJoin = "SELECT " + MusicContacts.ALBUM_TABLE_NAME + "." + MusicContacts.ALBUM_COLUMN_NAME + ", " +
                MusicContacts.ALBUM_COLUMN_ART + ", " + MusicContacts.ALBUM_TABLE_NAME + "." + MusicContacts.ALBUM_COLUMN_ID +
                " FROM " + MusicContacts.SONG_TABLE_NAME + " JOIN " + MusicContacts.ALBUM_TABLE_NAME +
                " WHERE " + MusicContacts.SONG_TABLE_NAME + "." + MusicContacts.SONG_COLUMN_ALBUM_ID + " = " +
                MusicContacts.ALBUM_TABLE_NAME + "." + MusicContacts.ALBUM_COLUMN_ID + " AND " + MusicContacts.SONG_TABLE_NAME +
                "." + MusicContacts.SONG_COLUMN_ID + " = " + songID;
        return database.rawQuery(queryJoin, null);
    }

    public void updateSong(Song song) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MusicContacts.SONG_COLUMN_NAME, song.getTitle());
        values.put(MusicContacts.SONG_COLUMN_DURATION, song.getDuration());
        values.put(MusicContacts.SONG_COLUMN_URI_SONG, song.getUri().toString());
        values.put(MusicContacts.SONG_COLUMN_ARTIST_NAME, song.getmArtist());
        values.put(MusicContacts.SONG_COLUMN_ALBUM_ID, song.getAlbumID());
        values.put(MusicContacts.SONG_COLUMN_FAVORITE, song.isFavorite()? 1:-1);
        values.put(MusicContacts.SONG_COLUMN_DISLIKE, song.isDislike()? 1:0);
        database.update(MusicContacts.SONG_TABLE_NAME, values, MusicContacts.SONG_COLUMN_ID + " = ?",
                new String[]{String.valueOf(song.getID())});
    }

    public void deleteSong(String whereClause, String[] selectionArgs, Uri uri) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(MusicContacts.SONG_TABLE_NAME, whereClause, selectionArgs);

        Uri delUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(selectionArgs[0]));
        int row1 = mContextWeakReference.get().getContentResolver().delete(delUri, null, null);

        if (row1 > 0)
        mContextWeakReference.get().getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null);

    }
}
