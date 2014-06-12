package com.popo.mrpopo.com.popo.mrpopo.contentprovider;

import android.provider.BaseColumns;

/**
 * Created by dennizhu on 6/11/14.
 */
public final class LocationContent {
    public LocationContent(){
    }

    public static abstract class PointsOfInterest implements BaseColumns{
        public static final String TABLE_NAME = "poi";
        public static final String COLUMN_NAME_ID = BaseColumns._ID;
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_CONTENT_TEXT = "contenttext";
        public static final String COLUMN_NAME_PICTURE = "picture";
        public static final String COLUMN_NAME_LAST_UPDATED = "lastupdated";
        public static final String COLUMN_NAME_NOTES = "notes";
    }

}
