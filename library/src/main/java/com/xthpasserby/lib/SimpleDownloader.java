package com.xthpasserby.lib;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.xthpasserby.lib.utils.LogUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018/5/25.
 */
public class SimpleDownloader extends Handler implements IDownloadListener {
    private static final int MESSAGE_ON_STATE_CHANGE = 0xBA0;
    private static final int MESSAGE_ON_PROGRESS = 0xBA1;
    private static final int MESSAGE_ON_OVER_FLOW = 0xBA2;
    private static final int MAX_POOL_SIZE = 100;
    private static final List<DownloadTask> taskPool = new ArrayList<>();
    private static SimpleDownloader sInstance;
    private SimpleDownloadHelper downloadHelper;
    private final String DEFAULT_DOWNLOAD_FILE_PATH;
    private final List<WeakReference<IDownloadListener>> listeners = new ArrayList<>();
    private final List<WeakReference<IDownloadListener>> mainThreadListeners = new ArrayList<>();
    private final List<DownloadTask> tasks = new ArrayList<>();

    private String taskUrl;
    private String taskFilePath;
    private String taskFileName;
    private boolean taskNeedResume = true;

    public static SimpleDownloader init(Context context) {
        if (null == sInstance) {
            synchronized (SimpleDownloader.class) {
                if (null == sInstance) {
                    sInstance = new SimpleDownloader(context);
                }
            }
        }
        return sInstance;
    }

    public static SimpleDownloader with() {
        if (null == sInstance) {
            LogUtil.e("you should init first!");
        }

        return sInstance;
    }

    private SimpleDownloader(Context context) {
        super(Looper.getMainLooper());
        if (null == context) {
            DEFAULT_DOWNLOAD_FILE_PATH = null;
            LogUtil.e("context is null!");
            return;
        }
        downloadHelper = new SimpleDownloadHelper(context.getApplicationContext());
        downloadHelper.setDownloadListener(this);
        List<DownloadTask> list = downloadHelper.getAllDownloadTask();
        if (null != list) {
            for (DownloadTask task : list) {
                task.setSimpleDownloader(this);
                tasks.add(task);
            }
        }

        File downloadFile = context.getExternalFilesDir("simple/download/files");
        if (null != downloadFile) {
            DEFAULT_DOWNLOAD_FILE_PATH = downloadFile.getAbsolutePath() + File.separator;
        } else {
            DEFAULT_DOWNLOAD_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/simple/download/files" + File.separator;
        }
    }

    public static void enableDebug() {
        SimpleDownloadHelper.enableDebug();
    }

    public List<DownloadTask> getAllTasks() {
        return tasks;
    }

    public SimpleDownloader url(String url) {
        if (TextUtils.isEmpty(url)) {
            LogUtil.e("url is null!");
            return null;
        }
        taskUrl = url;
        return this;
    }

    public SimpleDownloader filePath(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            LogUtil.e("filePath is null!");
            return null;
        }
        taskFilePath = filePath;
        return this;
    }

    public SimpleDownloader fileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            LogUtil.e("fileName is null!");
            return null;
        }
        taskFileName = fileName;
        return this;
    }

    public SimpleDownloader needResume(boolean needResume) {
        taskNeedResume = needResume;
        return this;
    }

    public SimpleDownloader addDownloadListener(IDownloadListener listener) {
        if (null == listener) {
            LogUtil.e("listener is null!");
            return null;
        }
        synchronized (listeners) {
            WeakReference<IDownloadListener> weakReference = new WeakReference(listener);
            listeners.add(weakReference);
        }
        return this;
    }

    public SimpleDownloader addDownloadListenerOnMainThread(IDownloadListener listener) {
        if (null == listener) {
            LogUtil.e("listener is null!");
            return null;
        }
        synchronized (mainThreadListeners) {
            WeakReference<IDownloadListener> weakReference = new WeakReference(listener);
            mainThreadListeners.add(weakReference);
        }
        return this;
    }

    public DownloadTask buildTask() {
        if (TextUtils.isEmpty(taskFilePath)) {
            taskFilePath = DEFAULT_DOWNLOAD_FILE_PATH;
        }
        DownloadTask task = obtainTask();
        cleanTask();
        return task;
    }

    private DownloadTask obtainTask() {
        DownloadTask task;
        synchronized (taskPool) {
            int size = taskPool.size();
            if (size < 1) {
                task = DownloadTaskFactory.buildTask(this, taskUrl, taskFilePath, taskFileName, taskNeedResume);
            } else {
                task = taskPool.remove(size - 1);
            }
        }
        return task;
    }

    private void cleanTask() {
        taskUrl = null;
        taskFilePath = null;
        taskFileName = null;
        taskNeedResume = true;
    }

    @Override
    public void onStatusChange(DownloadTask task) {
        synchronized (listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                WeakReference<IDownloadListener> weakReference = listeners.get(i);
                if (weakReference.get() == null) {
                    listeners.remove(i);
                    weakReference = null;
                    i--;
                } else {
                    weakReference.get().onStatusChange(task);
                }
            }
        }

        if (!mainThreadListeners.isEmpty()) {
            Message message = obtainMessage();
            message.what = MESSAGE_ON_STATE_CHANGE;
            message.obj = task;
            sendMessage(message);
        }
    }

    @Override
    public void onProgress(DownloadTask task) {
        synchronized (listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                WeakReference<IDownloadListener> weakReference = listeners.get(i);
                if (weakReference.get() == null) {
                    listeners.remove(i);
                    weakReference = null;
                    i--;
                } else {
                    weakReference.get().onProgress(task);
                }
            }
        }
        if (!mainThreadListeners.isEmpty()) {
            Message message = obtainMessage();
            message.what = MESSAGE_ON_PROGRESS;
            message.obj = task;
            sendMessage(message);
        }
    }

    @Override
    public void onStorageOverFlow() {
        synchronized (listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                WeakReference<IDownloadListener> weakReference = listeners.get(i);
                if (weakReference.get() == null) {
                    listeners.remove(i);
                    weakReference = null;
                    i--;
                } else {
                    weakReference.get().onStorageOverFlow();
                }
            }
        }
        if (!mainThreadListeners.isEmpty()) {
            sendEmptyMessage(MESSAGE_ON_OVER_FLOW);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        DownloadTask task;
        switch (msg.what) {
            case MESSAGE_ON_STATE_CHANGE:
                task = (DownloadTask) msg.obj;
                synchronized (mainThreadListeners) {
                    for (int i = 0; i < mainThreadListeners.size(); i++) {
                        WeakReference<IDownloadListener> weakReference = mainThreadListeners.get(i);
                        if (weakReference.get() == null) {
                            mainThreadListeners.remove(i);
                            weakReference = null;
                            i--;
                        } else {
                            weakReference.get().onStatusChange(task);
                        }
                    }
                }
                break;
            case MESSAGE_ON_PROGRESS:
                task = (DownloadTask) msg.obj;
                synchronized (mainThreadListeners) {
                    for (int i = 0; i < mainThreadListeners.size(); i++) {
                        WeakReference<IDownloadListener> weakReference = mainThreadListeners.get(i);
                        if (weakReference.get() == null) {
                            mainThreadListeners.remove(i);
                            weakReference = null;
                            i--;
                        } else {
                            weakReference.get().onProgress(task);
                        }
                    }
                }
                break;
            case MESSAGE_ON_OVER_FLOW:
                synchronized (mainThreadListeners) {
                    for (int i = 0; i < mainThreadListeners.size(); i++) {
                        WeakReference<IDownloadListener> weakReference = mainThreadListeners.get(i);
                        if (weakReference.get() == null) {
                            mainThreadListeners.remove(i);
                            weakReference = null;
                            i--;
                        } else {
                            weakReference.get().onStorageOverFlow();
                        }
                    }
                }
                break;
        }
    }

    void startTask(DownloadTask task) {
        if (!tasks.contains(task)) {
            synchronized (tasks) {
                tasks.add(task);
            }
        } else if (task.getDownloadStatus() != DownloadStatus.SUCCESS
                || task.getDownloadStatus() != DownloadStatus.CANCEL
                || task.getDownloadStatus() != DownloadStatus.FAILURE) {
            // 防止重复下载
            return;
        }

        downloadHelper.downloadStart(task);
    }

    void pauseTask(DownloadTask task) {
        downloadHelper.downloadPause(task);
    }

    void resumeTask(DownloadTask task) {
        downloadHelper.downloadResume(task);
    }

    void cancelTask(DownloadTask task, boolean deleteFile) {
        synchronized (tasks) {
            tasks.remove(task);
        }
        downloadHelper.downloadCancel(task, deleteFile);
    }

    void recycleTask(DownloadTask task) {
        synchronized (taskPool) {
            if (taskPool.size() < MAX_POOL_SIZE) {
                taskPool.add(task);
            }
        }
    }
}
