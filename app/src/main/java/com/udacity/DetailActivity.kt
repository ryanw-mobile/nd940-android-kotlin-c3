package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.appbar.AppBarLayout
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private var downloadStatus = DownloadManager.STATUS_FAILED
    private var downloadDescription = ""

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dismiss the notification message
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.cancelAll()

        binding = ActivityDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.contentDetail.buttonBack.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        // Try to get the downloadId
        if (intent != null) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            getDownloadResult(downloadId)
        }

        coordinateMotion()
    }

    override fun onResume() {
        super.onResume()

        // Show the download result on screen
        binding.contentDetail.textViewFilename.text = downloadDescription
        binding.contentDetail.textViewStatus.text = when (downloadStatus) {
            DownloadManager.STATUS_FAILED -> getString(R.string.status_failed)
            DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.status_success)
            else -> ""
        }
    }

    private fun getDownloadResult(downloadId: Long) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            if (cursor.count > 0) {
                downloadStatus =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                downloadDescription =
                    cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION))
            }
        }
        cursor.close()
    }

    // This function was from the motionlayout codelab
    // To synchronise the scrolling progress with the motionlayout
    private fun coordinateMotion() {
        val appBarLayout: AppBarLayout = binding.appbarLayout
        val motionLayout: MotionLayout = binding.motionLayout

        val listener = AppBarLayout.OnOffsetChangedListener { unused, verticalOffset ->
            val seekPosition = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
            motionLayout.progress = seekPosition
        }

        appBarLayout.addOnOffsetChangedListener(listener)
    }
}
