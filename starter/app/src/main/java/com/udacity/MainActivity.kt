package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private var handler = Handler(Looper.getMainLooper())
    private var isProgressCheckerRunning = false


    private var selectedURL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            if (selectedURL.isEmpty()) {
                showToast()
                return@setOnClickListener
            }


            custom_button.loading()
            download()
        }

        showToast()
    }

    override fun onDestroy() {
        stopProgressChecker()
        super.onDestroy()
    }

    private fun showToast() {
        Toast.makeText(this, getString(R.string.please_select), Toast.LENGTH_LONG).show()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stopProgressChecker()
            custom_button.loadingComplete()
            //TODO: NOTIFICATION
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(selectedURL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        startProgressChecker()
    }

    private fun checkDownloadProgress() {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))

        if (cursor.moveToFirst()) {
            val totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

            when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_RUNNING -> {
                    //get total bytes of the file
                    if (totalBytes >= 0) {

                        val bytesDownloadedSoFar : Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                        val percentProgress = ((bytesDownloadedSoFar * 100L) / totalBytes)

                        custom_button.loading(percentProgress.toInt())

//                        Timber.d("$percentProgress")
                    }
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                }
                DownloadManager.STATUS_FAILED -> {
                    val reason: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                }
            }
        }

        cursor.close()
    }

    private fun startProgressChecker() {
        if (!isProgressCheckerRunning) {
            isProgressCheckerRunning = true
            progressChecker.run()
        }
    }

    private fun stopProgressChecker() {
        handler.removeCallbacks(progressChecker)
        isProgressCheckerRunning = false
    }

    private val progressChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                checkDownloadProgress()
            } finally {
                handler.postDelayed(this, PROGRESS_DELAY.toLong())
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_glide ->
                    if (checked) {
                        selectedURL = GLIDE_URL
                    }
                R.id.radio_load_app ->
                    if (checked) {
                        selectedURL = UDACITY_URL
                    }

                R.id.radio_retrofit ->
                    if (checked) {
                        selectedURL = RETROFIT_URL
                    }
            }
        }
    }


    companion object {
        private const val UDACITY_URL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"

        private const val PROGRESS_DELAY = 500
    }

}
