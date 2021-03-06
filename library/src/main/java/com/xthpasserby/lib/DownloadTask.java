package com.xthpasserby.lib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载任务
 */
public class DownloadTask {
    public static final String ID = "_id";
    private long id = -1L;

    public static final String DOWNLOAD_URL = "downloadUrl";
    private String downloadUrl;

    public static final String DOWNLOAD_STATUS = "downloadStatus";
    private DownloadStatus downloadStatus = DownloadStatus.UN_START;

    public static final String FILE_PATH = "filePath";
    private String filePath; // 存储路径

    public static final String FILE_NAME = "fileName";
    private String fileName;

    public static final String FILE_SIZE = "fileSize";
    private String fileSize;// 文件大小

    public static final String PROGRESS_COUNT = "progressCount";
    private long progressCount = 0L; // 总大小

    public static final String CURRENT_PROGRESS = "currentProgress";
    private long currentProgress = 0L;// 当前进度

    public static final String PERCENTAGE = "percentage";
    private int percentage = 0; // 下载百分比0到1000

    /**
     * 这两项项无需存储到数据库中
     */
    private boolean isCancel = false; // 下载是否主动取消或者暂停
    private boolean isNeedSaveIntoDataBase = true; // 是否需要将进度保存至数据库(不保存就无法断点续传)

    /**
     * 计算下载速度相关项
     *
     */
    private long lastTime = 0;
    private long lastCount = 0;
    private int speed = 0; // 单位KB/S，显示时可自行转换

    private SimpleDownloader simpleDownloader;
    private final List<WeakReference<ITaskStatusListener>> statusListenersOnMainThread = new ArrayList<>();
    private final List<WeakReference<ITaskStatusListener>> statusListeners = new ArrayList<>();

    DownloadTask(SimpleDownloader simpleDownloader, String downloadUrl, String filePath, String fileName, boolean isNeedSaveIntoDataBase) {
        this.simpleDownloader = simpleDownloader;
        this.downloadUrl = downloadUrl;
        this.filePath = filePath;
        this.fileName = fileName;
        this.isNeedSaveIntoDataBase = isNeedSaveIntoDataBase;
    }

    public DownloadTask(long id, String downloadUrl, DownloadStatus downloadStatus, String filePath, String fileName, String fileSize, long progressCount, long currentProgress, int percentage) {
        this.id = id;
        this.downloadUrl = downloadUrl;
        this.downloadStatus = downloadStatus;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.progressCount = progressCount;
        this.currentProgress = currentProgress;
        this.percentage = percentage;
    }

    void setSimpleDownloader(SimpleDownloader simpleDownloader) {
        this.simpleDownloader = simpleDownloader;
    }

    public void addTaskStatusListenerOnMainThread(ITaskStatusListener listener) {
        if (null == listener) return;
        synchronized (statusListenersOnMainThread) {
            WeakReference<ITaskStatusListener> weakReference = new WeakReference(listener);
            statusListenersOnMainThread.add(weakReference);
        }
    }

    public void addTaskStatusListener(ITaskStatusListener listener) {
        if (null == listener) return;
        synchronized (statusListeners) {
            WeakReference<ITaskStatusListener> weakReference = new WeakReference(listener);
            statusListeners.add(weakReference);
        }
    }

    void onStatusChangeOnMainThread(final DownloadStatus status) {
        synchronized (statusListenersOnMainThread) {
            for (int i = 0; i < statusListenersOnMainThread.size(); i++) {
                WeakReference<ITaskStatusListener> weakReference = statusListenersOnMainThread.get(i);
                if (weakReference.get() == null) {
                    statusListenersOnMainThread.remove(i);
                    weakReference = null;
                    i--;
                } else {
                    weakReference.get().onStatusChange(status);
                }
            }
        }
    }

    void onProgressOnMainThread(final int percentage) {
        synchronized (statusListenersOnMainThread) {
            for (int i = 0; i < statusListenersOnMainThread.size(); i++) {
                WeakReference<ITaskStatusListener> weakReference = statusListenersOnMainThread.get(i);
                if (weakReference.get() == null) {
                    statusListenersOnMainThread.remove(i);
                    weakReference = null;
                    i--;
                } else {
                    weakReference.get().onProgress(percentage);
                }
            }
        }
    }

    void onStatusChange(final DownloadStatus status) {
        synchronized (statusListeners) {
            for (int i = 0; i < statusListeners.size(); i++) {
                WeakReference<ITaskStatusListener> weakReference = statusListeners.get(i);
                if (weakReference.get() == null) {
                    statusListeners.remove(i);
                    weakReference = null;
                    i--;
                } else {
                    weakReference.get().onStatusChange(status);
                }
            }
        }
    }

    void onProgress(final int percentage) {
        synchronized (statusListeners) {
            for (int i = 0; i < statusListeners.size(); i++) {
                WeakReference<ITaskStatusListener> weakReference = statusListeners.get(i);
                if (weakReference.get() == null) {
                    statusListeners.remove(i);
                    weakReference = null;
                    i--;
                } else {
                    weakReference.get().onProgress(percentage);
                }
            }
        }
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
        if (downloadStatus != this.downloadStatus) {
            this.downloadStatus = downloadStatus;
            simpleDownloader.onTaskStatusChange(this);
        }
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
        if (this.percentage != percentage) {
            this.percentage = percentage;
            simpleDownloader.onTaskProgress(this);
        }
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

    public void start() {
        simpleDownloader.enqueue(this);
    }

    public void pause() {
        simpleDownloader.pauseTask(this);
    }

    public void resume() {
        simpleDownloader.enqueue(this);
    }

    public void cancel() {
        cancel(true);
    }

    public void cancel(boolean deleteFile) {
        simpleDownloader.cancelTask(this, deleteFile);
    }

    public interface ITaskStatusListener {
        void onStatusChange(DownloadStatus status);
        void onProgress(int percentage);
    }
}