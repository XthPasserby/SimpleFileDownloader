package com.xthpasserby.lib;

public interface IDownloadListener {
    /**
     * 下载状态变化{@link DownloadStatus}
     * @param task
     */
    void onStatusChange(DownloadTask task);

    /**
     * 下载进度变化
     * @param task
     */
    void onProgress(DownloadTask task);

    /**
     * 存储溢出
     */
    void onStorageOverFlow();
}