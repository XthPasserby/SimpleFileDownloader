package com.xthpasserby.lib.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xthpasserby.lib.DownloadItem;

/**
 * Created on 2018/4/29.
 */
class DownloadDataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "simple_download.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_DOWNLOAD_TABLE = "CREATE TABLE IF NOT EXISTS "
            + DownloadDataBaseManager.TABLE_NAME + "("
            + DownloadItem.ID + " INTEGER PRIMARY KEY,"
            + DownloadItem.DOWNLOAD_URL + " VARCHAR,"
            + DownloadItem.DOWNLOAD_STATUS + " INTEGER,"
            + DownloadItem.FILE_PATH + " VARCHAR,"
            + DownloadItem.FILE_NAME + " VARCHAR,"
            + DownloadItem.FILE_SIZE + " VARCHAR,"
            + DownloadItem.PROGRESS_COUNT + " INTEGER,"
            + DownloadItem.CURRENT_PROGRESS + " INTEGER,"
            + DownloadItem.PERCENTAGE + " INTEGER"
            + ")";

    DownloadDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DOWNLOAD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // this is the first version so there is nothing to do
    }
}
