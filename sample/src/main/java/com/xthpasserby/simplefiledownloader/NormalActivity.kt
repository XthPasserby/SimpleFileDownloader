package com.xthpasserby.simplefiledownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xthpasserby.lib.DownloadStatus
import com.xthpasserby.lib.DownloadTask
import com.xthpasserby.lib.SimpleDownloader
import kotlinx.android.synthetic.main.activity_normal.*

class NormalActivity : AppCompatActivity(), DownloadTask.ITaskStatusListener {
    private var task: DownloadTask? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)

        task = SimpleDownloader.getInstance()
                .url("http://newstatic.7guoyouxi.com/apps/10053722/10053722.apk")
                .fileName("10053722.apk")
                .setTaskStatusChangeLisener(this)
                .buildTask()

        normal_download_button.setOnClickListener {
            when(task!!.downloadStatus) {
                DownloadStatus.UN_START,DownloadStatus.CANCEL -> task!!.start()
                DownloadStatus.START,DownloadStatus.DOWNLOADING -> task!!.pause()
                DownloadStatus.PAUSE -> task!!.resume()
            }
        }

        updateButtonStatus()
        progressBar.setProgress(task!!.percentage)
    }

    private fun updateButtonStatus() {
        when(task?.downloadStatus) {
            DownloadStatus.WAIT -> normal_download_button!!.setText("等待")
            DownloadStatus.START,DownloadStatus.DOWNLOADING,DownloadStatus.RESUME -> normal_download_button!!.setText("暂停")
            DownloadStatus.PAUSE -> normal_download_button!!.setText("继续")
            DownloadStatus.SUCCESS -> normal_download_button!!.setText("已完成")
            else -> {
                normal_download_button!!.setText("下载")
                progressBar!!.setProgress(0)
            }
        }
    }

    override fun onStatusChange(status: DownloadStatus?) {
        updateButtonStatus()
    }

    override fun onProgress(percentage: Int) {
        progressBar!!.setProgress(task!!.percentage)
    }
}
