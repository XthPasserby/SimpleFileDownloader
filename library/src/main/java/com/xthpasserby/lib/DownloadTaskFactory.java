package com.xthpasserby.lib;

import android.text.TextUtils;

/**
 * Created on 2018/5/23.
 */
public class DownloadTaskFactory {

    public static DownloadTask buildTask(long id, String downloadUrl, DownloadStatus downloadStatus, String filePath, String fileName, String fileSize, long progressCount, long currentProgress, int percentage) {
        return new DownloadTask(id, downloadUrl, downloadStatus, filePath, fileName, fileSize, progressCount, currentProgress, percentage);
    }

    static DownloadTask buildTask(SimpleDownloader simpleDownloader, String url, String filePath, String fileName, boolean isNeedResume) throws IllegalArgumentException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException();
        }
        return new DownloadTask(simpleDownloader, url, filePath, fileName, isNeedResume);
    }
}
