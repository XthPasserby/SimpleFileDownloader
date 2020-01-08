package com.xthpasserby.simplefiledownloader

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.xthpasserby.lib.DownloadTask
import com.xthpasserby.lib.IDownloadListener
import com.xthpasserby.lib.SimpleDownloader
import kotlinx.android.synthetic.main.activity_download_manger.*

class DownloadMangerActivity : AppCompatActivity(), IDownloadListener {
    private var tasks : List<DownloadTask>? = null
    private var adapter : DownloadMangerAdapter? = null
    private val handle : Handler = Handler{
        adapter!!.notifyDataSetChanged()
        return@Handler true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_manger)

        btn_clear.setOnClickListener({
            Thread {
                kotlin.run {
                    SimpleDownloader.getInstance().clearAllTasks()
                    handle.sendEmptyMessage(0)
                }
            }.start()
        })
        rv_task.layoutManager = LinearLayoutManager(this)
        adapter = DownloadMangerAdapter()
        rv_task.adapter = adapter

        // add downloadListener for all tasks which will callback on mainThread
        SimpleDownloader.getInstance().addDownloadListenerOnMainThread(this)

        tasks = SimpleDownloader.getInstance().allTasks
        adapter!!.setData(tasks)
    }

    override fun onStatusChange(task: DownloadTask?) {
    }

    override fun onProgress(task: DownloadTask?) {
    }

    override fun onStorageOverFlow() {
        Toast.makeText(this, "存储已满", Toast.LENGTH_SHORT).show()
    }
}
