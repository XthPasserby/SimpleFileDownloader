package com.xthpasserby.simplefiledownloader;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.xthpasserby.lib.DownloadTask;
import com.xthpasserby.lib.IDownloadListener;
import com.xthpasserby.lib.SimpleDownloadHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DownloadManager implements IDownloadListener {
    static DownloadManager sInstance;
    private final Context appContext;
    private final SimpleDownloadHelper downloadHelper;
    private final List<WeakReference<IDownloadListener>> listeners = new ArrayList<>();
    private final List<DownloadTask> tasks = new Vector<>();
    private final String DOWNLOAD_FILE_PATH;

    public static DownloadManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager(context.getApplicationContext());
                }
            }
        }

        return sInstance;
    }

    private DownloadManager(Context context) {
        appContext = context;
        downloadHelper = SimpleDownloadHelper.getInstance(context);
        if (BuildConfig.DEBUG) SimpleDownloadHelper.enableDebug();
        List<DownloadTask> list = downloadHelper.getAllDownloadTask();
        if (null != list && !list.isEmpty()) {
            tasks.addAll(list);
        }
        File downloadFile = context.getExternalFilesDir("download/games");
        if (null != downloadFile) {
            DOWNLOAD_FILE_PATH = downloadFile.getAbsolutePath() + File.separator;
        } else {
            DOWNLOAD_FILE_PATH = null;
        }
    }

    public void addListener(IDownloadListener listener) {
        if (null == listener) return;

        WeakReference<IDownloadListener> weakReference = new WeakReference<>(listener);
        listeners.add(weakReference);
    }

    public void startTask(DownloadTask task) {
        if (null == task || tasks.contains(task)) {
            return;
        }
        if (!TextUtils.isEmpty(DOWNLOAD_FILE_PATH)) {
            task.setFilePath(DOWNLOAD_FILE_PATH);
        }
        tasks.add(task);
        downloadHelper.downloadStart(task);
    }

    public void pauseTask(DownloadTask task) {
        if (null == task) return;
        downloadHelper.downloadPause(task);
    }

    public void resumeTask(DownloadTask task) {
        if (null == task) return;
        downloadHelper.downloadResume(task);
    }

    public void cancelTask(DownloadTask task) {
        if (null == task) return;

        tasks.remove(task);
        downloadHelper.downloadCancel(task, true);
    }

    @Override
    public void onStatusChange(DownloadTask task) {
        for (int i = 0; i < listeners.size(); i++) {
            WeakReference<IDownloadListener> weakReference = listeners.get(i);
            if (weakReference.get() == null) {
                listeners.remove(i);
                i--;
            } else {
                weakReference.get().onStatusChange(task);
            }
        }
    }

    @Override
    public void onProgress(DownloadTask task) {
        for (int i = 0; i < listeners.size(); i++) {
            WeakReference<IDownloadListener> weakReference = listeners.get(i);
            if (weakReference.get() == null) {
                listeners.remove(i);
                i--;
            } else {
                weakReference.get().onProgress(task);
            }
        }
    }

    @Override
    public void onStorageOverFlow() {
        Toast.makeText(appContext, "存储已满！", Toast.LENGTH_SHORT).show();
    }
}
