package com.xthpasserby.simplefiledownloader;

import android.app.Application;

/**
 * Created on 2018/5/25.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DownloadManager.getInstance(this);
    }
}
