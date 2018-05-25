package com.xthpasserby.lib.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xthpasserby.lib.DownloadStatus;
import com.xthpasserby.lib.DownloadTask;
import com.xthpasserby.lib.DownloadTaskFactory;
import com.xthpasserby.lib.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018/5/8.
 */
public class DownloadDataBaseManager {
    private static final int MAX_POOL_SIZE = 100;
    private static final List<ContentValues> contentValuesPool = new ArrayList<>();
    static final String TABLE_NAME = "download_task";
    private SQLiteDatabase dataBase;

    public DownloadDataBaseManager(Context context) {
        this.dataBase = new DownloadDataBaseHelper(context).getWritableDatabase();
    }

    private ContentValues obtainContentValues(DownloadTask task) {
        ContentValues values;
        synchronized (contentValuesPool) {
            int size = contentValuesPool.size();
            if (size < 1) {
                values = new ContentValues();
            } else {
                values = contentValuesPool.remove(size - 1);
            }
        }
        values.put(DownloadTask.DOWNLOAD_URL, task.getDownloadUrl());
        values.put(DownloadTask.DOWNLOAD_STATUS, task.getDownloadStatus().name());
        values.put(DownloadTask.FILE_PATH, task.getFilePath());
        values.put(DownloadTask.FILE_NAME, task.getFileName());
        values.put(DownloadTask.FILE_SIZE, task.getFileSize());
        values.put(DownloadTask.PROGRESS_COUNT, task.getProgressCount());
        values.put(DownloadTask.CURRENT_PROGRESS, task.getCurrentProgress());
        values.put(DownloadTask.PERCENTAGE, task.getPercentage());
        return values;
    }

    public boolean addDownloadTask(DownloadTask task) {
        if (DownloadStatus.CANCEL == task.getDownloadStatus()) return false;
        LogUtil.e("addDownloadTask id =" + task.getId() +  ", file_name = " + task.getFileName() + ", status = " + task.getDownloadStatus());
        ContentValues values = obtainContentValues(task);
        long line = 0;
        synchronized (this) {
            line =  dataBase.insert(TABLE_NAME, null, values);
        }
        synchronized (contentValuesPool) {
            values.clear();
            if (contentValuesPool.size() < MAX_POOL_SIZE) {
                contentValuesPool.add(values);
            }
        }
        return line > 0;
    }

    public boolean removeDownloadTask(DownloadTask task) {
        boolean res = true;
        int delRes = 0;
        synchronized (this) {
            delRes = dataBase.delete(TABLE_NAME, DownloadTask.ID + "=" + task.getId(), null);
        }
        if (task.getId() != -1 && 1 > delRes)
            res = false;
        LogUtil.e("removeDownloadTask id =" + task.getId() + ", file_name = " + task.getFileName() + ", status = " + task.getDownloadStatus() + ", delRes = " + delRes);
        if (!res) {
            for (int i = 0; i < 3; i++) {
                synchronized (this) {
                    delRes = dataBase.delete(TABLE_NAME, String.format("%1$s=%2$s", DownloadTask.ID, task.getId()), null);
                }
                LogUtil.e("removeDownloadTask FOR i = " + i + ", id =" + task.getId() + ", file_name = " + task.getFileName() + ", status = " + task.getDownloadStatus() + ", delRes = " + delRes);
                if (delRes > 0) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    public boolean updateDownloadTask(DownloadTask task) {
        boolean res = true;
        ContentValues values = obtainContentValues(task);
        synchronized (this) {
            if (dataBase.update(TABLE_NAME, values, DownloadTask.ID + "=" + task.getId(), null) < 1) {
                res = false;
            }
        }
        synchronized (contentValuesPool) {
            values.clear();
            if (contentValuesPool.size() < MAX_POOL_SIZE) {
                contentValuesPool.add(values);
            }
        }
        return res;
    }

    public void updateDownloadList(List<DownloadTask> list) {
        if (null == list) {
            return;
        }

        for (DownloadTask task : list) {
            if (-1 == task.getId()) {
                addDownloadTask(task);
            } else {
                updateDownloadTask(task);
            }
        }
    }

    public List<DownloadTask> getAllDownloadTask() {
        List<DownloadTask> list = null;
        Cursor cursor = dataBase.rawQuery("select * from " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            if (null == list) list = new ArrayList<>();
            DownloadTask task = DownloadTaskFactory.buildTask();
            task.setId(cursor.getInt(cursor.getColumnIndex(DownloadTask.ID)));
            task.setDownloadUrl(cursor.getString(cursor.getColumnIndex(DownloadTask.DOWNLOAD_URL)));
            task.setDownloadStatus(DownloadStatus.valueOf(cursor.getString(cursor.getColumnIndex(DownloadTask.DOWNLOAD_STATUS))));
            task.setFilePath(cursor.getString(cursor.getColumnIndex(DownloadTask.FILE_PATH)));
            task.setFileName(cursor.getString(cursor.getColumnIndex(DownloadTask.FILE_NAME)));
            task.setFileSize(cursor.getString(cursor.getColumnIndex(DownloadTask.FILE_SIZE)));
            task.setProgressCount(cursor.getLong(cursor.getColumnIndex(DownloadTask.PROGRESS_COUNT)));
            task.setCurrentProgress(cursor.getLong(cursor.getColumnIndex(DownloadTask.CURRENT_PROGRESS)));
            task.setPercentage(cursor.getInt(cursor.getColumnIndex(DownloadTask.PERCENTAGE)));
            list.add(task);
        }
        cursor.close();
        return list;
    }
}