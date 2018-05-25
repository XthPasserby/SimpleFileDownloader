package com.xthpasserby.lib;

import android.os.Environment;

/**
 * 下载任务
 */
public class DownloadTask {
    public static final String ID = "_id";
    protected long id = -1L;

    public static final String DOWNLOAD_URL = "downloadUrl";
    protected String downloadUrl;

    public static final String DOWNLOAD_STATUS = "downloadStatus";
    protected DownloadStatus downloadStatus = DownloadStatus.UN_START;

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
    protected long lastTime = 0;
    protected long lastCount = 0;
    protected int speed = 0; // 单位KB/S，显示时可自行转换

    DownloadTask() {
    }

    DownloadTask(String downloadUrl, String fileName, boolean isNeedSaveIntoDataBase) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.isNeedSaveIntoDataBase = isNeedSaveIntoDataBase;
    }

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

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
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

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public long getLastCount() {
        return lastCount;
    }

    public void setLastCount(long lastCount) {
        this.lastCount = lastCount;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "DownloadTask{" +
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
                ", lastTime=" + lastTime +
                ", lastCount=" + lastCount +
                ", speed=" + speed +
                '}';
    }
}