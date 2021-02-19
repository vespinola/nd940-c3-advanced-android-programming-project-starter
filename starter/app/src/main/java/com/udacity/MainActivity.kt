package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


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
            download()
        }

        startProgressChecker()
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
            //TODO: NOTIFICATION
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            downloadID = 0
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

        if (downloadID != 0L) {
            downloadManager.remove(downloadID)
        }

        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun checkDownloadProgress() {
        val query = DownloadManager.Query()
        query.setFilterByStatus((DownloadManager.STATUS_FAILED or DownloadManager.STATUS_SUCCESSFUL).inv())

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val cursor = downloadManager.query(query)

        if (!cursor.moveToFirst()) {
            cursor.close()
            return
        }

        do {
            val bytes_downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

            val dl_progress = (bytes_downloaded * 100L / bytes_total).toInt()

            Log.d(MainActivity.javaClass.name, "progress so far: $dl_progress")

        } while (cursor.moveToNext())
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
                // manager reference not found. Commenting the code for compilation
                //manager.refresh();
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

        private const val PROGRESS_DELAY = 1000
    }

}
