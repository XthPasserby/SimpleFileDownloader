package com.xthpasserby.simplefiledownloader

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.xthpasserby.lib.DownloadTask
import com.xthpasserby.lib.simpledownloadbutton.ProgressDownloadButton

class DownloadMangerAdapter : RecyclerView.Adapter<DownloadMangerAdapter.ItemHolder>() {
    private var tasks : List<DownloadTask>? = null

    fun setData(data: List<DownloadTask>?) {
        if (null == data) return
        tasks = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val task = tasks!![position]
        holder.tvUrl.text = task.downloadUrl
        holder.button.bindData(task)
        task!!.addTaskStatusListenerOnMainThread(holder.button)
    }

    override fun getItemCount(): Int {
        return if(tasks == null)  0 else tasks!!.size
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUrl: TextView
        val button: ProgressDownloadButton

        init {
            tvUrl = itemView.findViewById(R.id.tv_url)
            button = itemView.findViewById(R.id.progress_download_button)
        }
    }
}
