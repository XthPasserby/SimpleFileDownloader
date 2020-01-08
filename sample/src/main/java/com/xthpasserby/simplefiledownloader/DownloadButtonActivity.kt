package com.xthpasserby.simplefiledownloader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xthpasserby.lib.SimpleDownloader
import kotlinx.android.synthetic.main.activity_download_button.*

class DownloadButtonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_button)

        // MultiDownloadButton
        val task1 = SimpleDownloader.getInstance()
                .url("http://qg-file-oss.haiheng178.com/qiguo/apk/game/1573784166_182.apk")
                .fileName("1573784166_182.apk")
                .setTaskStatusChangeListenerOnMainThread(multi_download_button)
                .buildTask()
        //ProgressDownloadButton
        val task2 = SimpleDownloader.getInstance()
                .url("http://qg-file-oss.haiheng178.com/qiguo/apk/game/1568964883_389.apk")
                .fileName("1568964883_389.apk")
                .setTaskStatusChangeListenerOnMainThread(progress_download_button)
                .buildTask()

        multi_download_button.bindData(task1)
        progress_download_button.bindData(task2)
    }
}
