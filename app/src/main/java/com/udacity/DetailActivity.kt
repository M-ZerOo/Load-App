package com.udacity

import android.app.NotificationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        title = getString(R.string.title_activity_detail)

        // Remove the notification from the status bar
        val notificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.cancelAll()

        // Get which button is clicked from the MainActivity
        val selectedRadioId = intent?.extras?.get("radio_id")

        val fileName = findViewById<TextView>(R.id.file_name_value)
        val okButton = findViewById<Button>(R.id.ok_button)

        fileName.text = when (selectedRadioId) {
            R.id.glide_radio -> getString(R.string.glide_button_description)
            R.id.load_app_radio -> getString(R.string.load_app_button_description)
            else -> getString(R.string.retrofit_button_description)
        }

        okButton.setOnClickListener {
            finish()
        }

    }

}
