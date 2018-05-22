package com.xthpasserby.lib.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xthpasserby.lib.DownloadItem;
import com.xthpasserby.lib.LogUtil;

import java.util.List;

/**
 * Created on 2018/5/8.
 */
public class DownloadDataBaseManager<T extends DownloadItem> {
    static final String TABLE_NAME = "download_item";
    private SQLiteDatabase mDataBase;

    public DownloadDataBaseManager(Context context) {
        this.mDataBase = new DownloadDataBaseHelper(context).getWritableDatabase();
    }

    public boolean addDownloadItem(T item) {
        if (DownloadItem.DOWNLOAD_CANCEL == item.getDownloadStatus()) return false;
        LogUtil.e("addDownloadItem id =" + item.getId() +  ", file_name = " + item.getFileName() + ", status = " + item.getDownloadStatus());
        ContentValues values = new ContentValues();
        values.put(DownloadItem.DOWNLOAD_URL, item.getDownloadUrl());
        values.put(DownloadItem.DOWNLOAD_STATUS, item.getDownloadStatus());
        values.put(DownloadItem.FILE_PATH, item.getFilePath());
        values.put(DownloadItem.FILE_NAME, item.getFileName());
        values.put(DownloadItem.FILE_SIZE, item.getFileSize());
        values.put(DownloadItem.PROGRESS_COUNT, item.getProgressCount());
        values.put(DownloadItem.CURRENT_PROGRESS, item.getCurrentProgress());
        values.put(DownloadItem.PERCENTAGE, item.getPercentage());
        synchronized (this) {
            return mDataBase.insert(TABLE_NAME, null, values) > 0;
        }
    }

    public boolean removeDownloadItem(T item) {
        boolean res = true;
        int delRes = 0;
        synchronized (this) {
            delRes = mDataBase.delete(TABLE_NAME, String.format("%1$s=%2$s", DownloadItem.ID, item.getId()), null);
        }
        if (item.getId() != -1 && 1 > delRes)
            res = false;
        LogUtil.e("removeDownloadItem id =" + item.getId() + ", file_name = " + item.getFileName() + ", status = " + item.getDownloadStatus() + ", delRes = " + delRes);
        if (!res) {
            for (int i = 0; i < 3; i++) {
                synchronized (this) {
                    delRes = mDataBase.delete(TABLE_NAME, String.format("%1$s=%2$s", DownloadItem.ID, item.getId()), null);
                }
                LogUtil.e("removeDownloadItem FOR i = " + i + ", id =" + item.getId() + ", file_name = " + item.getFileName() + ", status = " + item.getDownloadStatus() + ", delRes = " + delRes);
                if (delRes > 0) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    public boolean updateDownloadItem(T item) {
        boolean res = true;
        ContentValues values = new ContentValues();
        values.put(DownloadItem.DOWNLOAD_URL, item.getDownloadUrl());
        values.put(DownloadItem.DOWNLOAD_STATUS, item.getDownloadStatus());
        values.put(DownloadItem.FILE_PATH, item.getFilePath());
        values.put(DownloadItem.FILE_NAME, item.getFileName());
        values.put(DownloadItem.FILE_SIZE, item.getFileSize());
        values.put(DownloadItem.PROGRESS_COUNT, item.getProgressCount());
        values.put(DownloadItem.CURRENT_PROGRESS, item.getCurrentProgress());
        values.put(DownloadItem.PERCENTAGE, item.getPercentage());
        synchronized (this) {
            if (mDataBase.update(TABLE_NAME, values, String.format("%1$s=%2$s", DownloadItem.ID, item.getId()), null) < 1) {
                res = false;
            }
        }
        return res;
    }

    public void updateDownloadList(List<T> list) {
        if (null == list) {
            return;
        }

        for (T item : list) {
            if (-1 == item.getId()) {
                addDownloadItem(item);
            } else {
                updateDownloadItem(item);
            }
        }
    }
}
