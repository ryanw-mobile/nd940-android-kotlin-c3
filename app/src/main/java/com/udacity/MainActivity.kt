package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        // Set up Notification - create channel
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.mainLayout.customButton.setOnClickListener {
            download(binding.mainLayout.mainRadiogroup.checkedRadioButtonId)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            // Only react to the download ID we know
            if (id == downloadID) {
                // Create a notification and display in the status bar
                val notificationManager = getSystemService(
                    NotificationManager::class.java
                )
                notificationManager.sendNotification(context!!)
            }
        }
    }

    private fun download(checkedRadioButtonId: Int) {
        if (checkedRadioButtonId == -1) {
            // If there is no selected option, display a Toast to let the user know to select one
            Toast.makeText(this, R.string.toast_no_select, Toast.LENGTH_LONG).show()
            return;
        }

        val URL = when (checkedRadioButtonId) {
            R.id.radio_glide -> URL_GLIDE
            R.id.radio_loadapp -> URL_UDACITY
            R.id.radio_retrofit -> URL_RETROFIT
            else -> return
        }

        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                //  disable badges for this channel
                setShowBadge(false)
            }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = R.color.colorAccent
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.app_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide"
        private const val URL_UDACITY =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "channelId"
    }

}
