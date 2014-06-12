package com.popo.mrpopo.com.popo.mrpopo.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

/**
 * Created by dennizhu on 6/11/14.
 */
public class ContentDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "content.db";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + LocationContent.PointsOfInterest.TABLE_NAME
            + "(" + LocationContent.PointsOfInterest.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "
            + LocationContent.PointsOfInterest.COLUMN_NAME_NAME + " TEXT, "
            + LocationContent.PointsOfInterest.COLUMN_NAME_TYPE + " TEXT, "
            + LocationContent.PointsOfInterest.COLUMN_NAME_LATITUDE + " REAL, "
            + LocationContent.PointsOfInterest.COLUMN_NAME_LONGITUDE + " REAL, "
            + LocationContent.PointsOfInterest.COLUMN_NAME_CONTENT_TEXT + " TEXT, "
            + LocationContent.PointsOfInterest.COLUMN_NAME_PICTURE + " BLOB, "
            + LocationContent.PointsOfInterest.COLUMN_NAME_LAST_UPDATED + " INTEGER, "
            + LocationContent.PointsOfInterest.COLUMN_NAME_NOTES + " TEXT );";

    public ContentDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}