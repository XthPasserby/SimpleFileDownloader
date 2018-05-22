package com.xthpasserby.lib;

import android.os.Environment;

/**
 * 下载项
 */
public class DownloadItem {
    /**
     * 下载未开始
     */
    public final static int DOWNLOAD_UNSTART = 0xf000;
    /**
     * 下载成功
     */
    public final static int DOWNLOAD_SUCCESS = 0xf001;
    /**
     * 下载失败
     */
    public final static int DOWNLOAD_FAILURE = 0xf002;
    /**
     * 下载中
     */
    public final static int DOWNLOADING = 0xf003;
    /**
     * 下载暂停
     */
    public final static int DOWNLOAD_PAUSE = 0xf004;
    /**
     * 下载取消
     */
    public final static int DOWNLOAD_CANCEL = 0xf005;
    /**
     * 下载继续
     */
    public final static int DOWNLOAD_RESTART = 0xf006;
    /**
     * 继续下载出错
     */
    public final static int DOWNLOAD_RESTART_ERROR = 0xf007;
    /**
     * 等待下载
     */
    public final static int DOWNLOAD_WAIT = 0xf008;
    /**
     * 开始下载
     */
    public final static int DOWNLOAD_START = 0xf009;

    public static final String ID = "_id";
    protected long id = -1L;

    public static final String DOWNLOAD_URL = "downloadUrl";
    protected String downloadUrl;

    public static final String DOWNLOAD_STATUS = "downloadStatus";
    protected int downloadStatus = DOWNLOAD_UNSTART;

    public static final String FILE_PATH = "filePath";
    protected String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/simple/download/"; // 存储路径

    public static final String FILE_NAME = "fileName";
    protected String fileName;

    public static final String FILE_SIZE = "fileSize";
    protected String fileSize;// 文件大小

    public static final String PROGRESS_COUNT = "progressCount";
    protected long progressCount = 0L; // 总大小

    public static final String CURRENT_PROGRESS = "currentProgress";
    protected long currentProgress = 0L;// 当前进度

    public static final String PERCENTAGE = "percentage";
    protected int percentage = 0; // 下载百分比0到1000

    /**
     * 这两项项无需存储到数据库中
     */
    protected boolean isCancel = false; // 下载是否主动取消或者暂停
    protected boolean isNeedSaveIntoDataBase = true; // 是否需要将进度保存至数据库(不保存就无法断点续传)

    /**
     * 计算下载速度相关项
     *
     */
    protected long mLastTime = 0;
    protected long mLastCount = 0;
    protected int mSpeed = 0; // 单位KB/S，显示时可自行转换

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public long getProgressCount() {
        return progressCount;
    }

    public void setProgressCount(long progressCount) {
        this.progressCount = progressCount;
    }

    public long getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(long currentProgress) {
        this.currentProgress = currentProgress;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public boolean isNeedSaveIntoDataBase() {
        return isNeedSaveIntoDataBase;
    }

    public void setNeedSaveIntoDataBase(boolean needSaveIntoDataBase) {
        this.isNeedSaveIntoDataBase = needSaveIntoDataBase;
    }

    public long getmLastTime() {
        return mLastTime;
    }

    public void setmLastTime(long mLastTime) {
        this.mLastTime = mLastTime;
    }

    public long getmLastCount() {
        return mLastCount;
    }

    public void setmLastCount(long mLastCount) {
        this.mLastCount = mLastCount;
    }

    public int getmSpeed() {
        return mSpeed;
    }

    public void setmSpeed(int mSpeed) {
        this.mSpeed = mSpeed;
    }

    @Override
    public String toString() {
        return "DownloadItem{" +
                "id=" + id +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", downloadStatus=" + downloadStatus +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", progressCount=" + progressCount +
                ", currentProgress=" + currentProgress +
                ", percentage=" + percentage +
                ", isCancel=" + isCancel +
                ", isNeedSaveIntoDataBase=" + isNeedSaveIntoDataBase +
                ", mLastTime=" + mLastTime +
                ", mLastCount=" + mLastCount +
                ", mSpeed=" + mSpeed +
                '}';
    }
}