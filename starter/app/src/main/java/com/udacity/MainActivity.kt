package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

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

        createChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name)
        )

        showToast()
    }

    private fun showToast() {
        Toast.makeText(this, getString(R.string.please_select), Toast.LENGTH_LONG).show()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            custom_button.loadingComplete()
            //TODO: NOTIFICATION
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val notificationManager = ContextCompat.getSystemService(
                    applicationContext,
                    NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendNotification("hoho", applicationContext)
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        // TODO: Step 1.6 START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.setShowBadge(false)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Time for breakfast"

            val notificationManager = getSystemService(
                    NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }

    private fun download() {
        val request = DownloadManager.Request(Uri.parse(selectedURL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "master.zip")

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
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
