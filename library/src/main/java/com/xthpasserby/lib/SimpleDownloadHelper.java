package com.xthpasserby.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.xthpasserby.lib.database.DownloadDataBaseManager;
import com.xthpasserby.lib.utils.CloseUtil;
import com.xthpasserby.lib.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * 下载工具类，通过okhttp实现，使用{@link IDownloadListener}传递事件
 */
public class SimpleDownloadHelper {
    private static final int PERCENTAGE = 100;
    private static final int PERMILLAGE = 1000;
    private static OkHttpClient mHttpClient = null;
    private static DownloadDataBaseManager dbManger;
    private static IDownloadListener mListener;
    /**
     * 调试模式是否开启
     */
    private static boolean isDebug = false;
    /**
     *  下载任务列表
     */
    private Map<DownloadTask, Call> downloadTaskCallMap = Collections.synchronizedMap(new HashMap<DownloadTask, Call>());
    // 进度条更新模式
    private int progressType = PERCENTAGE;

    SimpleDownloadHelper(Context context) {
        mHttpClient = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();
        dbManger = new DownloadDataBaseManager(context.getApplicationContext());
    }

    /**
     * 开启Debug打印
     */
    static void enableDebug() {
        isDebug = true;
    }

    public static boolean isDebugEnable() {
        return isDebug;
    }

    /**
     * 获取数据库中所有下载任务
     * @return 可能为null
     */
    @SuppressWarnings("unchecked")
    List<DownloadTask> getAllDownloadTask() {
        return dbManger.getAllDownloadTask();
    }

    /**
     * 注册监听
     * @param listener 下载监听
     */
    void setDownloadListener(IDownloadListener listener) {
        LogUtil.d("setDownloadListener called!");
        mListener = listener;
    }

    /**
     * 开始下载 注意该方法中item需要配置的参数有：fileName、filePath、downloadUrl
     * @param task 下载内容
     */
    void downloadStart(@NonNull final DownloadTask task) {
        if (DownloadStatus.DOWNLOADING == task.getDownloadStatus() && downloadTaskCallMap.get(task) != null) {
            return;
        }

        task.setCancel(false);
        mHttpClient.dispatcher().executorService().execute(new Runnable() {
            @Override
            public void run() {
                download(task, task.getFilePath() + task.getFileName(), false);
            }
        });
    }

    /**
     * 恢复下载 注意该方法中task需要配置的参数有：progressCount、currentProgress、fileName、filePath、downloadUrl
     * @param task 下载内容
     */
    void downloadResume(@NonNull final DownloadTask task) {
        if (DownloadStatus.DOWNLOADING == task.getDownloadStatus() && downloadTaskCallMap.get(task) != null) {
            return;
        }

        task.setCancel(false);
        mHttpClient.dispatcher().executorService().execute(new Runnable() {
            @Override
            public void run() {
                download(task, task.getFilePath() + task.getFileName(), true);
            }
        });
    }

    /**
     * 下载实现类
     * @param task 下载内容
     * @param fileName 保存文件
     * @param isResume 是否为断点续传
     */
    void download(@NonNull DownloadTask task, @NonNull String fileName, boolean isResume) {
        LogUtil.d(task.getDownloadUrl() + (isResume ? "---DOWNLOAD_RESUME" : "---DOWNLOAD_START"));
        task.setLastTime(System.currentTimeMillis());
        task.setDownloadStatus(isResume ? DownloadStatus.RESUME : DownloadStatus.START);
        onStatusChange(task);
        long startPos = isResume ? task.getCurrentProgress() : 0;
        long endPos = task.getProgressCount();
        if (isResume && startPos >= endPos) {
            isResume = false;
            startPos = 0;
        }

        if(TextUtils.isEmpty(task.getDownloadUrl())) {
            task.setDownloadStatus(DownloadStatus.FAILURE);
            onStatusChange(task);
            LogUtil.w("file_name = " + task.getFileName() + ", download_url is null!");
            return;
        }

        Request request;
        if (isResume) {
            request = new Request.Builder().url(task.getDownloadUrl())
                    .addHeader("Range", "bytes=" + startPos + "-" + endPos)
                    .build();
        } else {
            request = new Request.Builder().url(task.getDownloadUrl()).build();
        }

        File file = new File(task.getFilePath());
        if (null != file && !file.exists()) {
            file.mkdirs();
        }

        Response response;
        RandomAccessFile outFile = null;
        BufferedSource source = null;
        BufferedSink sink = null;
        Buffer buffer = null;
        FileOutputStream fileOutputStream = null;
        try {
            outFile = new RandomAccessFile(fileName, "rw");
            // 已下载文件出错，则重新下载
//            if (isRestart && (null == outFile || (outFile.length() != startPos && outFile.length() - startPos != 1024))) {
            if (isResume && (null == outFile || (outFile.length() < startPos))) {
                LogUtil.e("DOWNLOAD_RESUME_ERROR!!!");
                task.setDownloadStatus(DownloadStatus.RESUME_ERROR);
                onStatusChange(task);
                isResume = false;
                startPos = 0;
                request = new Request.Builder().url(task.getDownloadUrl()).build();
            }

            if (isResume) {
                outFile.seek(startPos);
            }
            fileOutputStream = new FileOutputStream(outFile.getFD());
            sink = Okio.buffer(Okio.sink(fileOutputStream));
            buffer = sink.buffer();
        } catch (IOException e) {
            if (task.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
                task.setDownloadStatus(DownloadStatus.FAILURE);
                onStatusChange(task);
            }
            LogUtil.w(Log.getStackTraceString(e));
            return;
        }

        Call call = mHttpClient.newCall(request);
        downloadTaskCallMap.put(task, call);
        try {
            response = call.execute();
            long totalLength;
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (isResume) {
                    totalLength = task.getProgressCount();
                } else {
                    totalLength = body.contentLength();
                    task.setProgressCount(totalLength);
                }
                source = body.source();
                long sum = 0;
                long len;
                final long bufferSize = 4096;
                int speed = 0;
                long time = 0;
                task.setDownloadStatus(DownloadStatus.DOWNLOADING);
                while (!task.isCancel() && (len = source.read(buffer, bufferSize)) > 0) {
                    sink.emit();
                    sum += len;
                    int lastProgress = (int) (((float) task.getCurrentProgress() / task.getProgressCount()) * progressType);
                    int curProgress = (int) (((float) (sum + startPos) / totalLength) * progressType);
                    task.setPercentage(curProgress);
                    task.setCurrentProgress(sum + startPos);
                    // 进度条变化时通知
                    if (curProgress == progressType || curProgress != lastProgress) {
                        time = System.currentTimeMillis();
                        if ((time - task.getLastTime()) > 500) {
                            speed = (int) (((sum - task.getLastCount()) / 1024) / ((float) (time - task.getLastTime()) / 1000));
                            task.setLastTime(time);
                            task.setLastCount(sum);
                            if (speed > 0) task.setSpeed(speed);
                        }
                        onProgress(task);
                        // 控制写入数据库次数
                        if (task.isNeedSaveIntoDataBase() && curProgress != lastProgress) {
                            if (-1 == task.getId()) {
                                dbManger.addDownloadTask(task);
                            } else {
                                dbManger.updateDownloadTask(task);
                            }
                        }
                    }
                }
                if (!task.isCancel()) {
                    task.setDownloadStatus(DownloadStatus.SUCCESS);
                }
                onStatusChange(task);
            } else {
                if (task.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
                    task.setDownloadStatus(DownloadStatus.FAILURE);
                    onStatusChange(task);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (task.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
                task.setDownloadStatus(DownloadStatus.FAILURE);
                onStatusChange(task);
                LogUtil.w(Log.getStackTraceString(e));
            }
            if (!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("ENOSPC")) {
                // write failed: ENOSPC (No space left on device)
                onStorageOverFlow();
            }
        } finally {
            CloseUtil.quietClose(source);
            CloseUtil.quietClose(buffer);
            CloseUtil.quietClose(sink);
            CloseUtil.quietClose(fileOutputStream);
            CloseUtil.quietClose(outFile);
        }
    }

    /**
     * 暂停下载
     * @param task
     */
    void downloadPause(@NonNull DownloadTask task) {
        LogUtil.d(task.getDownloadUrl() + "---DOWNLOAD_PAUSE");
        if (downloadTaskCallMap.containsKey(task)) {
            Call call = downloadTaskCallMap.get(task);
            if (!call.isCanceled()) {
                task.setCancel(true);
                call.cancel();
                task.setDownloadStatus(DownloadStatus.PAUSE);
                onStatusChange(task);
            }
        }
    }

    /**
     * 取消下载
     * @param task 下载内容
     * @param isDel 是否删除文件
     */
    void downloadCancel(@NonNull DownloadTask task, boolean isDel) {
        LogUtil.d(task.getDownloadUrl() + "---DOWNLOAD_CANCEL and isDel = " + isDel);
        if (downloadTaskCallMap.containsKey(task)) {
            Call call = downloadTaskCallMap.get(task);
            if (!call.isCanceled()) {
                task.setCancel(true);
                call.cancel();
                downloadTaskCallMap.remove(task);
            }
        }
        task.setDownloadStatus(DownloadStatus.CANCEL);
        onStatusChange(task);

        if (isDel) {
            File file = new File(task.getFilePath() + task.getFileName());
            if (null != file && file.exists()) {
                file.delete();
            }
        }
    }

    private void onStatusChange(DownloadTask task) {
        switch (task.getDownloadStatus()) {
            case SUCCESS:
            case FAILURE:
            case PAUSE:
                if (!task.isNeedSaveIntoDataBase()) {
                    break;
                }
                if (-1 == task.getId()) {
                    dbManger.addDownloadTask(task);
                } else {
                    dbManger.updateDownloadTask(task);
                }
                break;
            case CANCEL:
                dbManger.removeDownloadTask(task);
                break;
        }

        if (null != mListener) mListener.onStatusChange(task);
    }

    private void onProgress(DownloadTask task) {
        if (null != mListener) mListener.onProgress(task);
    }

    private void onStorageOverFlow() {
        if (null != mListener) mListener.onStorageOverFlow();
    }
}