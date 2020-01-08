package com.xthpasserby.simplefiledownloader

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_normal.setOnClickListener(this)
        button_download_button.setOnClickListener(this)
        button_manger.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.button_normal -> startActivity(Intent(this, NormalActivity::class.java))
            R.id.button_download_button -> startActivity(Intent(this, DownloadButtonActivity::class.java))
            R.id.button_manger -> startActivity(Intent(this, DownloadMangerActivity::class.java))
        }
    }
}
