package com.xthpasserby.lib.utils;

import android.os.Environment;
import android.util.Log;

import com.xthpasserby.lib.SimpleDownloadHelper;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * how to get line number, class name, method name
 * http://karimvarela.com/2012/06/12/android-how-to-include-line-numbers-class-names-and-method-names-in-your-log-statements/
 */
public class LogUtil {
    private static final String LOG_TAG = "simple_downloader_log";
    private static final String LOG_FILENAME = "simple_downloader_log.txt";
    private static final int STACK_TRACE_LEVELS_UP = 5;

    public static void e(String msg) {
        if (SimpleDownloadHelper.isDebugEnable()) {
            Log.e(LOG_TAG, getClassNameMethodNameAndLineNumber() + msg);
        }
    }

    public static void d(String msg) {
        if (SimpleDownloadHelper.isDebugEnable()) {
            Log.d(LOG_TAG, getClassNameMethodNameAndLineNumber() + msg);
        }
    }

    public static void w(String msg) {
        if (SimpleDownloadHelper.isDebugEnable()) {
            Log.w(LOG_TAG, getClassNameMethodNameAndLineNumber() + msg);
        }
    }

    public static void i(String msg) {
        if (SimpleDownloadHelper.isDebugEnable()) {
            Log.i(LOG_TAG, getClassNameMethodNameAndLineNumber() + msg);
        }
    }

    /**
     * 输出log信息到sdcard根目录下，文件名称为simple_downloader_log.txt
     *
     * @param msg
     */
    public static void logToFile(String msg) {
        if (!SimpleDownloadHelper.isDebugEnable()) {
            return;
        }

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
        String time = format.format(date);
        RandomAccessFile logFile = null;
        try {
            logFile = new RandomAccessFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + LOG_FILENAME, "rwd");
            logFile.seek(logFile.length());
            String log = time + "\t" + LOG_TAG + "\t" + getClassNameMethodNameAndLineNumber() + msg + "\n";
            logFile.writeUTF(log);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.quietClose(logFile);
        }
    }

    /**
     * 输出exception信息到sdcard根目录下，文件名称为simple_downloader_log.txt
     *
     * @param exception
     */
    public static void exceptionToFile(Exception exception) {
        if (!SimpleDownloadHelper.isDebugEnable()) {
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

    /**
     * Get the current line number. Note, this will only work as called from
     * this class as it has to go a predetermined number of steps up the stack
     * trace. In this case 5.
     *
     * @author kvarela
     * @return int - Current line number.
     */
    private static int getLineNumber()
    {
        return Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getLineNumber();
    }

    /**
     * Get the current class name. Note, this will only work as called from this
     * class as it has to go a predetermined number of steps up the stack trace.
     * In this case 5.
     *
     * @author kvarela
     * @return String - Current line number.
     */
    private static String getClassName()
    {
        String fileName = Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getFileName();

        // kvarela: Removing ".java" and returning class name
        return fileName.substring(0, fileName.length() - 5);
    }

    /**
     * Get the current method name. Note, this will only work as called from
     * this class as it has to go a predetermined number of steps up the stack
     * trace. In this case 5.
     *
     * @author kvarela
     * @return String - Current line number.
     */
    private static String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[STACK_TRACE_LEVELS_UP].getMethodName();
    }

    /**
     * Returns the class name, method name, and line number from the currently
     * executing log call in the form <class_name>.<method_name>()-<line_number>
     *
     * @author kvarela
     * @return String - String representing class name, method name, and line
     *         number.
     */
    private static String getClassNameMethodNameAndLineNumber()
    {
        return "[" + getClassName() + "." + getMethodName() + "()-" + getLineNumber() + "]: ";
    }
}
