package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var usedUrl: String? = null
    private var selectedRadioId = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var contentPendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val radioGroup: RadioGroup = findViewById(R.id.radio_group)
        val loadingButton: LoadingButton = findViewById(R.id.custom_button)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        notificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        // create a channel for your notification
        createChannel()

        // Check which radio button is clicked and save the url based on it
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.glide_radio -> usedUrl = GLIDE_URL
                R.id.load_app_radio -> usedUrl = UDACITY_URL
                R.id.retrofit_radio -> usedUrl = RETROFIT_URL
            }
        }

        loadingButton.setOnClickListener {
            selectedRadioId = radioGroup.checkedRadioButtonId
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            notificationManager.sendNotification(
                context.getString(R.string.notification_description),
                context
            )
            // Return the button state to Completed once it's done
            custom_button.changeState(ButtonState.Completed)
        }
    }

    private fun download() {
        // Check if no radio button clicked make a toast to select one then return from download function
        if (usedUrl == null) {
            Toast.makeText(this, "Please select the file to download.", Toast.LENGTH_SHORT).show()
            return
        }

        // Call changeState method so the loading animation starts
        custom_button.changeState(ButtonState.Loading)

        val request =
            DownloadManager.Request(Uri.parse(usedUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val GLIDE_URL = "https://github.com/bumptech/glide"
        private const val UDACITY_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val RETROFIT_URL = "https://github.com/square/retrofit"
    }

    private fun NotificationManager.sendNotification(
        messageBody: String,
        applicationContext: Context
    ) {
        val NOTIFICATION_ID = 0

        val contentIntent = Intent(applicationContext, DetailActivity::class.java).apply {
            putExtra("radio_id", selectedRadioId)
        }
        contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        action = NotificationCompat.Action.Builder(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_button),
            contentPendingIntent
        ).build()

        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.load_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(action)

        notify(NOTIFICATION_ID, builder.build())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.load_notification_channel_id)
            val channelName = getString(R.string.load_notification_channel_name)
            val notificationChannel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}
