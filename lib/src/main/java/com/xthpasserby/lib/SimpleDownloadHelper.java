package com.xthpasserby.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.xthpasserby.lib.database.DownloadDataBaseManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.HashMap;
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
 * 快定下载工具类，通过okhttp实现，使用{@link IDownloadListener}传递事件
 */
public class SimpleDownloadHelper<T extends DownloadItem> {
    private static SimpleDownloadHelper instance = null;
    private static OkHttpClient mHttpClient = null;
    private static DownloadDataBaseManager dbManger;
    private static IDownloadListener mListener;
    /**
     * 调试模式是否开启
     */
    static boolean isDebug = false;
    /**
     *  下载任务列表
     */
    private Map<T, Call> downloadItemCallMap = Collections.synchronizedMap(new HashMap<T, Call>());

    /**
     * 私有构造
     */
    private SimpleDownloadHelper(Context context) {
        mHttpClient = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();
        dbManger = new DownloadDataBaseManager(context);
    }

    /**
     * 获取实例
     *
     * @return 返回KDingDownloadHelper实例
     */
    static SimpleDownloadHelper getInstance(Context context) {
        if (null == instance) {
            synchronized (SimpleDownloadHelper.class) {
                if (null == instance) {
                    instance = new SimpleDownloadHelper<>(context);
                }
            }
        }

        return instance;
    }

    /**
     * 开启Debug打印
     */
    public void enableDebug() {
        isDebug = true;
    }

    /**
     * 注册监听
     * @param listener 下载监听
     */
    public void setDownloadListener(IDownloadListener listener) {
        LogUtil.d("setDownloadListener called!");
        mListener = listener;
    }

    /**
     * 开始下载 注意该方法中item需要配置的参数有：fileName、filePath、downloadUrl
     * @param item 下载内容
     */
    public void downloadStart(@NonNull final T item) {
        if (DownloadItem.DOWNLOADING == item.getDownloadStatus() && downloadItemCallMap.get(item) != null) {
            return;
        }

        item.setCancel(false);
        mHttpClient.dispatcher().executorService().execute(new Runnable() {
            @Override
            public void run() {
                download(item, item.getFilePath() + item.getFileName(), false);
            }
        });
    }

    /**
     * 继续下载 注意该方法中item需要配置的参数有：progressCount、currentProgress、fileName、filePath、downloadUrl
     * @param item 下载内容
     */
    public void downloadRestart(@NonNull final T item) {
        if (DownloadItem.DOWNLOADING == item.getDownloadStatus() && downloadItemCallMap.get(item) != null) {
            return;
        }

        item.setCancel(false);
        mHttpClient.dispatcher().executorService().execute(new Runnable() {
            @Override
            public void run() {
                download(item, item.getFilePath() + item.getFileName(), true);
            }
        });
    }

    /**
     * 下载实现类
     * @param item 下载内容
     * @param fileName 保存文件
     * @param isRestart 是否为断点续传
     */
    private void download(@NonNull T item, @NonNull String fileName, boolean isRestart) {
        LogUtil.d(item.getDownloadUrl() + (isRestart ? "---DOWNLOAD_RESTART" : "---DOWNLOAD_START"));
        item.setmLastTime(System.currentTimeMillis());
        item.setDownloadStatus(isRestart ? DownloadItem.DOWNLOAD_RESTART : DownloadItem.DOWNLOAD_START);
        onStatusChange(item);
        long startPos = isRestart ? item.getCurrentProgress() : 0;
        long endPos = item.getProgressCount();
        if (isRestart && startPos >= endPos) {
            isRestart = false;
            startPos = 0;
        }

        if(TextUtils.isEmpty(item.getDownloadUrl())) {
            item.setDownloadStatus(DownloadItem.DOWNLOAD_FAILURE);
            onStatusChange(item);
            LogUtil.w("file_name = " + item.getFileName() + ", download_url is null!");
            return;
        }

        Request request;
        if (isRestart) {
            request = new Request.Builder().url(item.getDownloadUrl())
                    .addHeader("Range", "bytes=" + startPos + "-" + endPos)
                    .build();
        } else {
            request = new Request.Builder().url(item.getDownloadUrl()).build();
        }

        File file = new File(item.getFilePath());
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
            if (isRestart && (null == outFile || (outFile.length() < startPos))) {
                LogUtil.e("DOWNLOAD_RESTART_ERROR!!!");
                item.setDownloadStatus(DownloadItem.DOWNLOAD_RESTART_ERROR);
                onStatusChange(item);
                isRestart = false;
                startPos = 0;
                request = new Request.Builder().url(item.getDownloadUrl()).build();
            }

            if (isRestart) {
                outFile.seek(startPos);
            }
            fileOutputStream = new FileOutputStream(outFile.getFD());
            sink = Okio.buffer(Okio.sink(fileOutputStream));
            buffer = sink.buffer();
        } catch (IOException e) {
            if (item.getDownloadStatus() == DownloadItem.DOWNLOADING) {
                item.setDownloadStatus(DownloadItem.DOWNLOAD_FAILURE);
                onStatusChange(item);
            }
            LogUtil.w(Log.getStackTraceString(e));
            return;
        }

        Call call = mHttpClient.newCall(request);
        downloadItemCallMap.put(item, call);
        try {
            response = call.execute();
            long totalLength;
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (isRestart) {
                    totalLength = item.getProgressCount();
                } else {
                    totalLength = body.contentLength();
                    item.setProgressCount(totalLength);
                }
                source = body.source();
                long sum = 0;
                long len;
                final long bufferSize = 4096;
                int speed = 0;
                long time = 0;
                item.setDownloadStatus(DownloadItem.DOWNLOADING);
//                final long t = System.currentTimeMillis();
                while (!item.isCancel() && (len = source.read(buffer, bufferSize)) > 0) {
                    sink.emit();
                    sum += len;
                    int lastProgress = (int) (((float) item.getCurrentProgress() / item.getProgressCount()) * 1000);
                    int curProgress = (int) (((float) (sum + startPos) / totalLength) * 1000);
                    item.setPercentage(curProgress);
                    item.setCurrentProgress(sum + startPos);
                    // 进度条变化时通知
                    if (curProgress == 1000 || curProgress != lastProgress) {
                        time = System.currentTimeMillis();
                        if ((time - item.getmLastTime()) > 500) {
                            speed = (int) (((sum - item.getmLastCount()) / 1024) / ((float) (time - item.getmLastTime()) / 1000));
                            item.setmLastTime(time);
                            item.setmLastCount(sum);
                            if (speed > 0) item.setmSpeed(speed);
                        }
                        onProgress(item);
                        if (item.isNeedSaveIntoDataBase() && (curProgress / 10) != (lastProgress / 10)) {
                            if (-1 == item.getId()) {
                                dbManger.addDownloadItem(item);
                            } else {
                                dbManger.updateDownloadItem(item);
                            }
                        }
                    }
                }
//                Log.e("===", "time = " + (System.currentTimeMillis() - t));
                if (!item.isCancel()) {
                    item.setDownloadStatus(DownloadItem.DOWNLOAD_SUCCESS);
                }
                onStatusChange(item);
            } else {
                if (item.getDownloadStatus() == DownloadItem.DOWNLOADING) {
                    item.setDownloadStatus(DownloadItem.DOWNLOAD_FAILURE);
                    onStatusChange(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (item.getDownloadStatus() == DownloadItem.DOWNLOADING) {
                item.setDownloadStatus(DownloadItem.DOWNLOAD_FAILURE);
                onStatusChange(item);
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
     * @param item 下载内容
     */
    public void downloadPause(@NonNull T item) {
        LogUtil.d(item.getDownloadUrl() + "---DOWNLOAD_PAUSE");
        if (downloadItemCallMap.containsKey(item)) {
            Call call = downloadItemCallMap.get(item);
            if (!call.isCanceled()) {
                item.setCancel(true);
                call.cancel();
                item.setDownloadStatus(DownloadItem.DOWNLOAD_PAUSE);
                onStatusChange(item);
            }
        }
    }

    /**
     * 取消下载
     * @param item 下载内容
     * @param isDel 是否删除文件
     */
    public void downloadCancel(@NonNull T item, boolean isDel) {
        LogUtil.d(item.getDownloadUrl() + "---DOWNLOAD_CANCEL and isDel = " + isDel);
        if (downloadItemCallMap.containsKey(item)) {
            Call call = downloadItemCallMap.get(item);
            if (!call.isCanceled()) {
                item.setCancel(true);
                call.cancel();
                downloadItemCallMap.remove(item);
            }
        }
        item.setDownloadStatus(DownloadItem.DOWNLOAD_CANCEL);
        onStatusChange(item);

        if (isDel) {
            File file = new File(item.getFilePath() + item.getFileName());
            if (null != file && file.exists()) {
                file.delete();
            }
        }
    }

    private void onStatusChange(T item) {
        switch (item.getDownloadStatus()) {
            case DownloadItem.DOWNLOAD_SUCCESS:
            case DownloadItem.DOWNLOAD_FAILURE:
            case DownloadItem.DOWNLOAD_PAUSE:
                if (!item.isNeedSaveIntoDataBase()) {
                    break; // 自动更新的下载不需要保存到数据库中
                }
                if (-1 == item.getId()) {
                    dbManger.addDownloadItem(item);
                } else {
                    dbManger.updateDownloadItem(item);
                }
                break;
            case DownloadItem.DOWNLOAD_CANCEL:
                dbManger.removeDownloadItem(item);
                break;
        }

        if (null != mListener) mListener.onStatusChange(item);
    }

    private void onProgress(T item) {
        if (null != mListener) mListener.onProgress(item);
    }

    private void onStorageOverFlow() {
        if (null != mListener) mListener.onStorageOverFlow();
    }
}