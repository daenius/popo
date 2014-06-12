package com.popo.mrpopo.com.popo.mrpopo.contentprovider;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.net.URL;

/**
 * Created by dennizhu on 6/11/14.
 */
public class DbAsyncTask extends AsyncTask<URL, Integer, Long> {
    protected Long doInBackground(URL... urls) {
        return Long.valueOf(1);
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(Long result) {

    }
}

