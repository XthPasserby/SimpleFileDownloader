package com.xthpasserby.simplefiledownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.xthpasserby.lib.DownloadStatus
import com.xthpasserby.lib.DownloadTask
import com.xthpasserby.lib.IDownloadListener
import com.xthpasserby.lib.SimpleDownloader
import com.xthpasserby.lib.simpledownloadbutton.MultiDownloadButton
import com.xthpasserby.lib.simpledownloadbutton.ProgressDownloadButton

class MainActivity : AppCompatActivity(), IDownloadListener {
    private var button1: MultiDownloadButton? = null
    private var button2: ProgressDownloadButton? = null
    private var button3: Button? = null
    private var progressBar: ProgressBar? = null;
    private var button4: Button? = null;
    private var task1: DownloadTask? = null
    private var task2: DownloadTask? = null
    private var task3: DownloadTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1 = findViewById(R.id.button_1)
        button2 = findViewById(R.id.button_2)
        button3 = findViewById(R.id.button_3)
        progressBar = findViewById(R.id.progressBar)
        button4 = findViewById(R.id.button_4)

        task1 = SimpleDownloader.getInstance()
                .url("http://transmit.7guoyouxi.com/game/download/app_id/20170427-WLHDdewjhd/tune/qiguo_qgyx")
                .fileName("10051872_qiguo_qgyx.apk")
                .setTaskStatusChangeLisener(button1) // set downloadListener for single task
                .buildTask()
        task2 = SimpleDownloader.getInstance()
                .url("http://newstatic.7guoyouxi.com/apps/10051872/10051872.apk")
                .fileName("10051872.apk")
                .setTaskStatusChangeLisener(button2)
                .buildTask()
        task3 = SimpleDownloader.getInstance()
                .url("http://newstatic.7guoyouxi.com/apps/10053388/10053388.apk")
                .fileName("10053388.apk")
                .buildTask()

        // add downloadListener for all tasks which will callback on mainThread
        SimpleDownloader.getInstance().addDownloadListenerOnMainThread(this)

        button1!!.bindData(task1)
        button2!!.bindData(task2)
        button3!!.setOnClickListener {
            SimpleDownloader.getInstance().clearAllTasks()
        }
        button4!!.setOnClickListener {
            Log.e("***", "button4 onClick task3.status = " + task3?.downloadStatus)
            when(task3!!.downloadStatus) {
                DownloadStatus.UN_START,DownloadStatus.CANCEL -> task3!!.start()
                DownloadStatus.START,DownloadStatus.DOWNLOADING -> task3!!.pause()
                DownloadStatus.PAUSE -> task3!!.resume()
            }
        }

        updateButton4Status()
        progressBar!!.setProgress(task3!!.percentage)
    }

    private fun updateButton4Status() {
        when(task3?.downloadStatus) {
            DownloadStatus.START,DownloadStatus.DOWNLOADING,DownloadStatus.RESUME -> button4!!.setText("暂停")
            DownloadStatus.PAUSE -> button4!!.setText("继续")
            DownloadStatus.SUCCESS -> button4!!.setText("已完成")
            else -> {
                button4!!.setText("下载")
                progressBar!!.setProgress(0)
            }
        }
    }

    override fun onStatusChange(task: DownloadTask?) {
        Log.e("***", "onStatusChange task status = " + task?.downloadStatus + ", url = " + task?.downloadUrl)
        if (task!!.id == task3!!.id) {
            updateButton4Status()
        }
    }

    override fun onProgress(task: DownloadTask?) {
        Log.e("***", "onProgress task percentage = " + task?.percentage + ", url = " + task?.downloadUrl)
        if (task!!.id == task3!!.id) {
            progressBar!!.setProgress(task!!.percentage)
        }
    }

    override fun onStorageOverFlow() {
        Toast.makeText(this, "存储已满", Toast.LENGTH_SHORT).show()
    }
}
