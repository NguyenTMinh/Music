package com.minhntn.music.prov;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.minhntn.music.database.MusicDBHelper;

public class FavoriteSongsProvider extends ContentProvider {
    static final int NOTES = 1;
    static final int NOTE_ID = 2;

    static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MusicContacts.AUTHORITY, MusicContacts.CONTENT_PATH, NOTES);
        sUriMatcher.addURI(MusicContacts.AUTHORITY, MusicContacts.CONTENT_PATH + "/#", NOTE_ID);
    }

    private SQLiteDatabase mSqLiteDatabase;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        MusicDBHelper musicDBHelper = new MusicDBHelper(context);
        mSqLiteDatabase = musicDBHelper.getWritableDatabase();
        return mSqLiteDatabase != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MusicContacts.FAVORITE_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case NOTES: break;
            case NOTE_ID: {
                queryBuilder.appendWhere(MusicContacts.FAVORITE_COLUMN_ID + " = " + uri.getPathSegments().get(1));
                break;
            }
            default: throw new IllegalArgumentException("Uri: " + uri);
        }

        Cursor cursor = queryBuilder.query(mSqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case NOTES: return MusicContacts.MULTIPLE_FAV_MIME_TYPE;
            case NOTE_ID: return MusicContacts.SINGLE_FAV_MIME_TYPE;
            default: throw new IllegalArgumentException("Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long rowID = mSqLiteDatabase.insert(MusicContacts.FAVORITE_TABLE_NAME, null, values);

        if (rowID > 0) {
            Uri newUri = ContentUris.withAppendedId(MusicContacts.CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(newUri,null);
            return newUri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case NOTES: {
                count = mSqLiteDatabase.delete(MusicContacts.FAVORITE_TABLE_NAME, selection, selectionArgs);
                break;
            }
            case NOTE_ID: {
                String id = uri.getPathSegments().get(1);
                count = mSqLiteDatabase.delete(MusicContacts.FAVORITE_TABLE_NAME,
                        MusicContacts.FAVORITE_COLUMN_ID_PROVIDER + "=" + id
                                + ((!TextUtils.isEmpty(selection))? " AND ( " + selection + ")" : ""),
                        selectionArgs);
                break;
            }
            default: throw new IllegalArgumentException("Uri: " + uri);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;

        switch (sUriMatcher.match(uri)) {
            case NOTES: {
                count = mSqLiteDatabase.update(MusicContacts.FAVORITE_TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case NOTE_ID: {
                String id = uri.getPathSegments().get(1);
                count = mSqLiteDatabase.update(MusicContacts.FAVORITE_TABLE_NAME, values,
                        MusicContacts.FAVORITE_COLUMN_ID_PROVIDER + "=" + id +
                                ((!TextUtils.isEmpty(selection))? " AND ( " + selection + ")" : ""),
                        selectionArgs);
                break;
            }
            default: throw new IllegalArgumentException("Uri: " + uri);
        }

        return count;
    }
}
