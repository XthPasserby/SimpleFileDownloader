package com.xthpasserby.simplefiledownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xthpasserby.lib.DownloadTask
import com.xthpasserby.lib.SimpleDownloader
import com.xthpasserby.lib.simpledownloadbutton.MultiDownloadButton
import com.xthpasserby.lib.simpledownloadbutton.ProgressDownloadButton

class MainActivity : AppCompatActivity() {
    private var button1: MultiDownloadButton? = null
    private var button2: ProgressDownloadButton? = null
    private var task1: DownloadTask? = null
    private var task2: DownloadTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1 = findViewById(R.id.button_1)
        button2 = findViewById(R.id.button_2)

        task1 = SimpleDownloader.with()
                .url("http://transmit.7guoyouxi.com/game/download/app_id/20171116-LBmhxianyu/tune/qiguo_qgyx")
                .fileName("10054926_qiguo_qgyx.apk")
                .setTaskStatusChangeLisener(button1)
                .buildTask();
        task2 = SimpleDownloader.with()
                .url("http://transmit.7guoyouxi.com/game/download/app_id/20160527-XXJQXZ/tune/qiguo_qgyx")
                .fileName("10055955_qiguo_qgyx.apk")
                .setTaskStatusChangeLisener(button2)
                .buildTask()
        button1!!.bindData(task1)
        button2!!.bindData(task2)
    }

    override fun onDestroy() {
        task1!!.recycle()
        task2!!.recycle()
        super.onDestroy()
    }
}
