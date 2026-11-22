package com.example.weekdayintervalalarm

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val statusText = TextView(this).apply { textSize = 18f; setPadding(48, 80, 48, 40) }
        val startBtn = Button(this).apply { text = "Start Weekday 45m Alarms" }
        val stopBtn = Button(this).apply { text = "Stop Alarms" }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            addView(statusText)
            addView(startBtn)
            addView(stopBtn)
        }
        setContentView(layout)

        requestRequiredPermissions()
        updateStatusUI(statusText)

        startBtn.setOnClickListener {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            } else {
                AlarmScheduler.scheduleNextAlarm(this)
                updateStatusUI(statusText)
            }
        }

        stopBtn.setOnClickListener {
            AlarmScheduler.cancelAlarm(this)
            stopService(Intent(this, AlarmService::class.java))
            updateStatusUI(statusText)
        }
    }

    private fun updateStatusUI(statusText: TextView) {
        val prefs = getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("is_enabled", false)
        val nextMillis = prefs.getLong("next_trigger", 0L)

        statusText.text = if (enabled && nextMillis > 0) {
            val formatted = SimpleDateFormat("EEE, MMM d 'at' hh:mm a", Locale.getDefault()).format(Date(nextMillis))
            "Status: ACTIVE\nNext Alarm: $formatted"
        } else {
            "Status: INACTIVE"
        }
    }

    private fun requestRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }
}

class AlarmRingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dismissButton = Button(this).apply {
            text = "DISMISS ALARM"
            textSize = 24f
            setOnClickListener {
                stopService(Intent(this@AlarmRingActivity, AlarmService::class.java))
                finish()
            }
        }
        setContentView(dismissButton)
    }
}
