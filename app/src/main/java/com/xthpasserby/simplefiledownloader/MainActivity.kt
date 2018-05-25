package com.xthpasserby.simplefiledownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xthpasserby.lib.DownloadTask
import com.xthpasserby.lib.DownloadTaskFactory
import com.xthpasserby.lib.simpledownloadbutton.BaseDownloadButton
import com.xthpasserby.lib.simpledownloadbutton.MultiDownloadButton
import com.xthpasserby.lib.simpledownloadbutton.ProgressDownloadButton

class MainActivity : AppCompatActivity(), BaseDownloadButton.IOnDownloadButtonClickListener {
    var button1: MultiDownloadButton? = null
    var button2: ProgressDownloadButton? = null
    var downloadManager: DownloadManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1 = findViewById(R.id.button_1)
        button2 = findViewById(R.id.button_2)
        button1!!.setButtonClickListener(this)
        button2!!.setButtonClickListener(this)

        var task1 = DownloadTaskFactory.buildTask("http://transmit.7guoyouxi.com/game/download/app_id/20171116-LBmhxianyu/tune/qiguo_qgyx", "10054926_qiguo_qgyx.apk", true)
        var task2 = DownloadTaskFactory.buildTask("http://transmit.7guoyouxi.com/game/download/app_id/20160527-XXJQXZ/tune/qiguo_qgyx", "10055955_qiguo_qgyx.apk", true)
        button1!!.bindData(task1)
        button2!!.bindData(task2)

        downloadManager = DownloadManager.getInstance(this)
        downloadManager!!.addListener(button1)
        downloadManager!!.addListener(button2)
    }

    override fun onClickToStartDownload(task: DownloadTask?) {
        downloadManager!!.startTask(task)
    }

    override fun onClickToPauseDownload(task: DownloadTask?) {
        downloadManager!!.pauseTask(task)
    }

    override fun onClickToResumeDownload(task: DownloadTask?) {
        downloadManager!!.resumeTask(task)
    }

    override fun onClickAfterDownloadFinish(task: DownloadTask?) {
        downloadManager!!.cancelTask(task)
    }
}
