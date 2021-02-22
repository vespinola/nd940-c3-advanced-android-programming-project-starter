package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.database.Cursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import kotlinx.android.synthetic.main.content_main.*

class DetailActivity : AppCompatActivity() {

    private var downloadID: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        downloadID = intent?.extras?.getLong(ARG_DOWNLOAD_ID) ?: -1

        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
        ) as NotificationManager

        notificationManager.cancelAll()

        checkLastDownloadStatus()

        ok_button.setOnClickListener {
            finish()
        }
    }

    private fun checkLastDownloadStatus() {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))

        if (cursor.moveToFirst()) {
            when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    status_value.setText(R.string.success)
                }
                DownloadManager.STATUS_FAILED -> {
                    status_value.setText(R.string.fail)
                }
                else -> {
                    status_value.setText(R.string.fail)
                }
            }

            file_name_value.text = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
        }

        cursor.close()
    }

}
