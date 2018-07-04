package com.xthpasserby.simplefiledownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xthpasserby.lib.SimpleDownloader
import kotlinx.android.synthetic.main.activity_download_button.*

class DownloadButtonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_button)

        // MultiDownloadButton
        val task1 = SimpleDownloader.getInstance()
                .url("http://newstatic.7guoyouxi.com/apps/10051872/10051872.apk")
                .fileName("10051872.apk")
                .setTaskStatusChangeLisener(multi_download_button)
                .buildTask()
        //ProgressDownloadButton
        val task2 = SimpleDownloader.getInstance()
                .url("http://newstatic.7guoyouxi.com/apps/10053388/10053388.apk")
                .fileName("10053388.apk")
                .setTaskStatusChangeLisener(progress_download_button)
                .buildTask()

        multi_download_button.bindData(task1)
        progress_download_button.bindData(task2)
    }
}
