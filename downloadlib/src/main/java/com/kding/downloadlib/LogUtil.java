package com.kding.downloadlib;

import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ZC on 2016/3/16.
 */
public class LogUtil {
    private static final String LOG_TAG = "kdingdownloadlib_log";
    private static final String LOG_FILENAME = "download_log.txt";

    public static void e(String msg) {
        if (KdingDownloadHelper.isDebug) {
            Log.e(LOG_TAG, msg);
        }
    }

    public static void d(String msg) {
        if (KdingDownloadHelper.isDebug) {
            Log.d(LOG_TAG, msg);
        }
    }

    public static void w(String msg) {
        if (KdingDownloadHelper.isDebug) {
            Log.w(LOG_TAG, msg);
        }
    }

    public static void i(String msg) {
        if (KdingDownloadHelper.isDebug) {
            Log.i(LOG_TAG, msg);
        }
    }

    /**
     * 输出log信息到sdcard根目录下，文件名称为download_log.txt
     * @param msg
     */
    public static void logToFile(String msg) {
        if (!KdingDownloadHelper.isDebug) {
            return;
        }

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
        String time = format.format(date);
        RandomAccessFile logFile = null;
        try {
            logFile = new RandomAccessFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + LOG_FILENAME, "rwd");
            logFile.seek(logFile.length());
            String log = time + "\t" + LOG_TAG + "\t" + msg + "\n";
            logFile.writeUTF(log);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.quietClose(logFile);
        }
    }

    /**
     * 输出exception信息到sdcard根目录下，文件名称为download_log.txt
     * @param exception
     */
    public static void exceptionToFile(Exception exception) {
        if (!KdingDownloadHelper.isDebug) {
            return;
        }

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
        String time = format.format(date);
        RandomAccessFile logFile = null;
        try {
            logFile = new RandomAccessFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + LOG_FILENAME, "rwd");
            logFile.seek(logFile.length());
            String log = time + "\t" + LOG_TAG + "\t" + Log.getStackTraceString(exception) + "\n";
            logFile.writeUTF(log);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.quietClose(logFile);
        }
    }
}
