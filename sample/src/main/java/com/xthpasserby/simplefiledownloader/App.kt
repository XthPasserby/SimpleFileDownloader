package com.xthpasserby.simplefiledownloader

import android.app.Application
import com.xthpasserby.lib.SimpleDownloader

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        SimpleDownloader.init(this)
        if (BuildConfig.DEBUG) SimpleDownloader.enableDebug()
    }
}
