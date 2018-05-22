package com.kding.downloadlib;

public enum KdingDownloadUtil {
    INSTANCE;
    private KdingDownloadHelper<?> downloadHelper;

    KdingDownloadUtil() {
        downloadHelper = KdingDownloadHelper.getInstance();
    }

    public KdingDownloadHelper getDownloadHelper() {
        return downloadHelper;
    }
}
