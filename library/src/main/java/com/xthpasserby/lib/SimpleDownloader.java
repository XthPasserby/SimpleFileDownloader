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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleDownloader extends Handler implements IDownloadListener, Runnable {
    private static final int MAX_RUNNING_SIZE = 5;
    private static final int MESSAGE_ON_STATE_CHANGE = 0xBA0;
    private static final int MESSAGE_ON_PROGRESS = 0xBA1;
    private static final int MESSAGE_ON_OVER_FLOW = 0xBA2;
    private static final int MESSAGE_ON_TASK_STATUS_CHANGE = 0xBA3;
    private static final int MESSAGE_ON_TASK_PROGRESS = 0xBA4;
    private static final int DEFAULT_TIME_OUT = 30;
    public static final int PERCENTAGE = 100;
    public static final int PERMILLAGE = 1000;
    private static SimpleDownloader sInstance;
    private SimpleDownloadHelper downloadHelper;
    private final String DEFAULT_DOWNLOAD_FILE_PATH;
    private final List<WeakReference<IDownloadListener>> listeners = new ArrayList<>();
    private final List<WeakReference<IDownloadListener>> mainThreadListeners = new ArrayList<>();
    private final List<DownloadTask> tasks = new ArrayList<>();
    private final PendingTaskQueue taskQueue = new PendingTaskQueue();
    private volatile boolean executorRunning;
    private ExecutorService queueExecutor = Executors.newSingleThreadExecutor();
    private final List<DownloadTask> runningTasks;
    private final int maxRunningSize;

    private String taskUrl;
    private String taskFilePath;
    private String taskFileName;
    private boolean taskNeedResume = true;
    private DownloadTask.ITaskStatusListener statusListener;

    public static void init(Context context) {
        init(context, MAX_RUNNING_SIZE, DEFAULT_TIME_OUT, PERCENTAGE);
    }

    /**
     * 设置
     * @param context
     * @param timeOut
     * @param progressType
     */
    public static void init(Context context, int maxRunningSize, int timeOut, int progressType) {
        if (null == sInstance) {
            synchronized (SimpleDownloader.class) {
                if (null == sInstance) {
                    if (progressType != PERCENTAGE && progressType != PERMILLAGE) {
                        LogUtil.e("progressType must been PERCENTAGE or PERMILLAGE!");
                        progressType = PERCENTAGE;
                    }
                    sInstance = new SimpleDownloader(context, maxRunningSize, timeOut, progressType);
                }
            }
        }
    }

    public static SimpleDownloader getInstance() {
        if (null == sInstance) {
            LogUtil.e("you should init first!");
        }
        return sInstance;
    }

    private SimpleDownloader(Context context, int maxRunningSize, int timeOut, int progressType) throws IllegalArgumentException {
        super(Looper.getMainLooper());
        if (null == context) {
            DEFAULT_DOWNLOAD_FILE_PATH = null;
            LogUtil.e("context is null!");
            throw new IllegalArgumentException();
        }
        this.maxRunningSize = maxRunningSize;
        runningTasks = new ArrayList<>(maxRunningSize);
        downloadHelper = new SimpleDownloadHelper(context.getApplicationContext(), timeOut, progressType);
        downloadHelper.setDownloadListener(this);
        List<DownloadTask> list = downloadHelper.getAllDownloadTask();
        if (null != list) {
            for (DownloadTask task : list) {
                task.setSimpleDownloader(this);
                tasks.add(task);
                resumeQueue(task);
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

    public void clearAllTasks() {
        synchronized (tasks) {
            for (int i = 0; i < tasks.size(); i++) {
                DownloadTask task = tasks.get(i);
                cancelTask(task, true);
                i--;
            }
        }
    }

    public SimpleDownloader url(String url) throws IllegalArgumentException {
        if (TextUtils.isEmpty(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            LogUtil.e("url is null!");
            throw new IllegalArgumentException();
        }
        taskUrl = url;
        return this;
    }

    public SimpleDownloader filePath(String filePath) throws IllegalArgumentException {
        if (TextUtils.isEmpty(filePath)) {
            LogUtil.e("filePath is null!");
            throw new IllegalArgumentException();
        }
        taskFilePath = filePath;
        return this;
    }

    public SimpleDownloader fileName(String fileName) throws IllegalArgumentException {
        if (TextUtils.isEmpty(fileName)) {
            LogUtil.e("fileName is null!");
            throw new IllegalArgumentException();
        }
        taskFileName = fileName;
        return this;
    }

    public SimpleDownloader needResume(boolean needResume) {
        taskNeedResume = needResume;
        return this;
    }

    /**
     * 添加对所有下载任务状态监听(在下载线程回调)，可以设置多个监听
     * @param listener {@link IDownloadListener}
     * @return
     */
    public SimpleDownloader addDownloadListener(IDownloadListener listener) throws IllegalArgumentException {
        if (null == listener) {
            LogUtil.e("listener is null!");
            throw new IllegalArgumentException();
        }
        synchronized (listeners) {
            WeakReference<IDownloadListener> weakReference = new WeakReference(listener);
            listeners.add(weakReference);
        }
        return this;
    }

    /**
     * 添加对所有下载任务状态监听(在主线程回调)，可以设置多个监听
     * @param listener {@link IDownloadListener}
     * @return
     */
    public SimpleDownloader addDownloadListenerOnMainThread(IDownloadListener listener) throws IllegalArgumentException {
        if (null == listener) {
            LogUtil.e("listener is null!");
            throw new IllegalArgumentException();
        }
        synchronized (mainThreadListeners) {
            WeakReference<IDownloadListener> weakReference = new WeakReference(listener);
            mainThreadListeners.add(weakReference);
        }
        return this;
    }

    /**
     * 设置单个task状态监听(在主线程回调)，一个task只能设置一个监听
     * @param listener {@link DownloadTask.ITaskStatusListener}
     * @return
     */
    public SimpleDownloader setTaskStatusChangeLisener(DownloadTask.ITaskStatusListener listener) throws IllegalArgumentException {
        if (null == listener) {
            LogUtil.e("listener is null!");
            throw new IllegalArgumentException();
        }
        statusListener = listener;
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
        DownloadTask task = isTasksContains(taskUrl, taskFilePath, taskFileName);
        if (null == task) {
            task = DownloadTaskFactory.buildTask(this, taskUrl, taskFilePath, taskFileName, taskNeedResume);
        }
        if (null != statusListener) task.setTaskStatusListener(statusListener);
        return task;
    }

    private void cleanTask() {
        taskUrl = null;
        taskFilePath = null;
        taskFileName = null;
        taskNeedResume = true;
        statusListener = null;
    }

    private DownloadTask isTasksContains(final String url, final String filePath, final String fileName) {
        synchronized (tasks) {
            DownloadTask resTask = null;
            for (DownloadTask task : tasks) {
                if (TextUtils.equals(url, task.getDownloadUrl())
                        && TextUtils.equals(filePath, task.getFilePath())
                        && TextUtils.equals(fileName, task.getFileName())) {
                    resTask = task;
                    break;
                }
            }
            return resTask;
        }
    }

    @Override
    public void onStatusChange(DownloadTask task) {
        checkTaskStatus(task);
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
        DownloadTask.ITaskStatusListener listener;
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
            case MESSAGE_ON_TASK_STATUS_CHANGE:
                task = (DownloadTask) msg.obj;
                listener = task.getStatusListener();
                if (null != listener) {
                    listener.onStatusChange(task.getDownloadStatus());
                }
                break;
            case MESSAGE_ON_TASK_PROGRESS:
                task = (DownloadTask) msg.obj;
                listener = task.getStatusListener();
                if (null != listener) {
                    listener.onProgress(task.getPercentage());
                }
                break;
        }
    }

    void onTaskStatusChange(DownloadTask task) {
        Message message = obtainMessage();
        message.what = MESSAGE_ON_TASK_STATUS_CHANGE;
        message.obj = task;
        sendMessage(message);
    }

    void onTaskProgress(DownloadTask task) {
        Message message = obtainMessage();
        message.what = MESSAGE_ON_TASK_PROGRESS;
        message.obj = task;
        sendMessage(message);
    }

    private void startTask(DownloadTask task) {
        downloadHelper.downloadStart(task);
    }

    void pauseTask(DownloadTask task) {
        downloadHelper.downloadPause(task);
    }

    private void resumeTask(DownloadTask task) {
        downloadHelper.downloadResume(task);
    }

    void cancelTask(DownloadTask task, boolean deleteFile) {
        synchronized (tasks) {
            tasks.remove(task);
        }
        downloadHelper.downloadCancel(task, deleteFile);
    }

    void enqueue(DownloadTask task) {
        synchronized (tasks) {
            if (!tasks.contains(task)) {
                tasks.add(task);
            }
        }
        if (task.getDownloadStatus() != DownloadStatus.WAIT) downloadHelper.downloadWait(task);
        PendingTask pendingTask = PendingTask.obtainPendingTask(task);
        taskQueue.enqueue(pendingTask);
        if (!executorRunning) {
            executorRunning = true;
            queueExecutor.execute(this);
        }
    }

    private void resumeQueue(DownloadTask task) {
        if (task.getDownloadStatus() == DownloadStatus.WAIT
                || task.getDownloadStatus() == DownloadStatus.START
                || task.getDownloadStatus() == DownloadStatus.RESUME
                || task.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
            if (task.getDownloadStatus() != DownloadStatus.WAIT) downloadHelper.downloadWait(task);
            PendingTask pendingTask = PendingTask.obtainPendingTask(task);
            taskQueue.enqueue(pendingTask);
            if (!executorRunning) {
                executorRunning = true;
                queueExecutor.execute(this);
            }
        }
    }

    private void checkTaskStatus(DownloadTask task) {
        switch (task.getDownloadStatus()) {
            case FAILURE:
            case PAUSE:
            case SUCCESS:
            case CANCEL:
                synchronized (runningTasks) {
                    runningTasks.remove(task);
                }
                if (!executorRunning) {
                    executorRunning = true;
                    queueExecutor.execute(this);
                }
                break;
            default:
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (runningTasks) {
                    if (runningTasks.size() >= maxRunningSize) break;
                }
                PendingTask pendingTask = taskQueue.poll(1000);
                if (null == pendingTask) {
                    pendingTask = taskQueue.poll();
                    if (null == pendingTask) {
                        executorRunning = false;
                        return;
                    }
                }
                final DownloadTask task = pendingTask.downloadTask;
                PendingTask.releasePendingTask(pendingTask);
                synchronized (runningTasks) {
                    // if task has already started ignore this task
                    if (runningTasks.contains(task)) {
                        LogUtil.w("task has been already started!");
                        continue;
                    } else {
                        runningTasks.add(task);
                    }
                }
                if (task.getCurrentProgress() > 0) {
                    resumeTask(task);
                } else {
                    startTask(task);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorRunning = false;
        }
    }
}
