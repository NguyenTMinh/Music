package com.minhntn.music.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import com.minhntn.music.R;
import com.minhntn.music.model.Album;
import com.minhntn.music.model.Song;
import com.minhntn.music.prov.MusicContacts;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

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
                MusicContacts.SONG_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MusicContacts.SONG_COLUMN_NAME + " TEXT NOT NULL," +
                MusicContacts.SONG_COLUMN_DURATION + " LONG NOT NULL," +
                MusicContacts.SONG_COLUMN_URI_SONG + " TEXT NOT NULL," +
                MusicContacts.SONG_COLUMN_ARTIST_NAME + " TEXT NOT NULL," +
                MusicContacts.SONG_COLUMN_ALBUM_ID + " INTEGER NOT NULL" + ")";

        // Create album table
        String queryCreateAlbumTable = "CREATE TABLE " + MusicContacts.ALBUM_TABLE_NAME + "(" +
                MusicContacts.ALBUM_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MusicContacts.ALBUM_COLUMN_NAME + " TEXT NOT NULL," +
                MusicContacts.ALBUM_COLUMN_ART + " BLOB" + ")";

        db.execSQL(queryCreateSongTable);
        db.execSQL(queryCreateAlbumTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MusicContacts.SONG_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MusicContacts.ALBUM_TABLE_NAME);
        onCreate(db);
    }

    /**
     * This method is used to get the infos needed then save them to database
     * and may use only once when app first installed in order to copy the data to database
     */
    public void insertValuesToTables() {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                .getPath();
        String[] files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                .list();
        if (files != null) {
            for (String file : files) {
                mmr.setDataSource(path + "/" + file);
                // Info about song
                String songTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                long songDuration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                String songUri = path + "/" + file;
                String songArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                // Info about album
                String albumTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                byte[] imageCover = mmr.getEmbeddedPicture();
                if (imageCover == null) {
                    Bitmap bitmap = BitmapFactory.decodeResource(mContextWeakReference.get().getResources(),
                            R.drawable.bg_default_album_art);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    imageCover = stream.toByteArray();
                    bitmap.recycle();
                }

                insertTB(new Song(0, songTitle, songDuration, songUri, songArtist, 0),
                        new Album(0, albumTitle, imageCover));
            }
        }
    }

    /**
     *
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
            String[] params = {song.getTitle(), String.valueOf(song.getDuration()), song.getUri().toString(),
                    String.valueOf(tempAlbumID)};
            database.execSQL("INSERT INTO " + MusicContacts.SONG_TABLE_NAME + " VALUES (null, ?, ?, ?, ?)",
                    params);
        }
        database.close();
    }

    public Album getAlbumWithExactName(String albumName) {
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
        database.close();
        return album;
    }

    public Song getSongWithExactDetail(Song song) {
        Song temp = null;
        SQLiteDatabase database = getReadableDatabase();
        String[] selection = {song.getTitle(), String.valueOf(song.getAlbumID())};
        Cursor cursor = database.query(MusicContacts.SONG_TABLE_NAME, null,
                MusicContacts.SONG_COLUMN_NAME + "= ? AND " +
                MusicContacts.SONG_COLUMN_ALBUM_ID + "= ? ", selection, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            temp = new Song(cursor.getInt(0), cursor.getString(1), cursor.getLong(2),
                    cursor.getString(3), cursor.getString(4), cursor.getInt(5));
            cursor.moveToNext();
        }
        return temp;
    }

}
