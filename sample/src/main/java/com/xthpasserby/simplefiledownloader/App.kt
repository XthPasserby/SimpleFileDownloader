package com.xthpasserby.simplefiledownloader

import android.app.Application
import com.xthpasserby.lib.SimpleDownloader

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        SimpleDownloader.init(this, 1, 30, SimpleDownloader.PERCENTAGE)
        if (BuildConfig.DEBUG) SimpleDownloader.enableDebug()
    }
}
