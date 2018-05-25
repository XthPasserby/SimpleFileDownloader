package com.xthpasserby.lib;

import android.text.TextUtils;

import com.xthpasserby.lib.utils.LogUtil;

/**
 * Created on 2018/5/23.
 */
public class DownloadTaskFactory {

    public static DownloadTask buildTask() {
        return new DownloadTask();
    }

    public static DownloadTask buildTask(String url, String fileName, boolean isNeedResume) {
        if (TextUtils.isEmpty(url)) {
            LogUtil.e("url is empty!");
            return null;
        }
        return new DownloadTask(url, fileName, isNeedResume);
    }
}
