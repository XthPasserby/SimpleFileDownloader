package com.xthpasserby.simplefiledownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.xthpasserby.lib.DownloadTask
import com.xthpasserby.lib.IDownloadListener
import com.xthpasserby.lib.SimpleDownloader
import com.xthpasserby.lib.simpledownloadbutton.MultiDownloadButton
import com.xthpasserby.lib.simpledownloadbutton.ProgressDownloadButton

class MainActivity : AppCompatActivity(), IDownloadListener {
    private var button1: MultiDownloadButton? = null
    private var button2: ProgressDownloadButton? = null
    private var button3: Button? = null
    private var task1: DownloadTask? = null
    private var task2: DownloadTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1 = findViewById(R.id.button_1)
        button2 = findViewById(R.id.button_2)
        button3 = findViewById(R.id.button_3)

        task1 = SimpleDownloader.getInstance()
                .url("http://transmit.7guoyouxi.com/game/download/app_id/20170427-WLHDdewjhd/tune/qiguo_qgyx")
                .fileName("10051872_qiguo_qgyx.apk")
                .setTaskStatusChangeLisener(button1)
                .buildTask()
        task2 = SimpleDownloader.getInstance()
                .url("http://newstatic.7guoyouxi.com/apps/10051872/10051872.apk")
                .fileName("10051872.apk")
                .setTaskStatusChangeLisener(button2)
                .buildTask()

        SimpleDownloader.getInstance().addDownloadListenerOnMainThread(this)

        button1!!.bindData(task1)
        button2!!.bindData(task2)
        button3!!.setOnClickListener {
            SimpleDownloader.getInstance().clearAllTasks()
        }
    }

    override fun onStatusChange(task: DownloadTask?) {
        Log.e("***", "onStatusChange task status = " + task?.downloadStatus + ", url = " + task?.downloadUrl)
    }

    override fun onProgress(task: DownloadTask?) {
        Log.e("***", "onProgress task percentage = " + task?.percentage + ", url = " + task?.downloadUrl)
    }

    override fun onStorageOverFlow() {
        Toast.makeText(this, "存储已满", Toast.LENGTH_SHORT).show()
    }
}
