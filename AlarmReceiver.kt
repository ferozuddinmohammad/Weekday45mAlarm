package com.example.weekdayintervalalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmScheduler.scheduleNextAlarm(context)
        val serviceIntent = Intent(context, AlarmService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val isEnabled = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
                .getBoolean("is_enabled", false)
            if (isEnabled) {
                AlarmScheduler.scheduleNextAlarm(context)
            }
        }
    }
}
