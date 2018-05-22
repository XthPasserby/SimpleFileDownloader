package com.kding.downloadlib;

public interface IDownloadListener<T extends DownloadItem> {
    /**
     * 下载状态变化{@link DownloadItem}
     * @param item
     */
    void onStatusChange(T item);

    /**
     * 下载进度变化
     * @param item
     */
    void onProgress(T item);

    /**
     * 存储溢出
     */
    void onStorageOverFlow();
}