package com.example.weekdayintervalalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {

    private const val REQUEST_CODE = 1001
    private val START_TIME = LocalTime.of(9, 0)
    private val END_TIME = LocalTime.of(18, 0)
    private const val INTERVAL_MINUTES = 45L

    fun scheduleNextAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val nextTriggerTime = calculateNextWeekdaySlot(LocalDateTime.now())
        val triggerMillis = nextTriggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

        context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_enabled", true)
            .putLong("next_trigger", triggerMillis)
            .apply()
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_enabled", false)
            .apply()
    }

    fun calculateNextWeekdaySlot(now: LocalDateTime): LocalDateTime {
        val candidateDate = now.toLocalDate()

        if (candidateDate.dayOfWeek == DayOfWeek.SATURDAY) {
            return candidateDate.plusDays(2).atTime(START_TIME)
        }
        if (candidateDate.dayOfWeek == DayOfWeek.SUNDAY) {
            return candidateDate.plusDays(1).atTime(START_TIME)
        }

        var slot = candidateDate.atTime(START_TIME)
        val cutoff = candidateDate.atTime(END_TIME)

        while (!slot.isAfter(cutoff)) {
            if (slot.isAfter(now)) {
                return slot
            }
            slot = slot.plusMinutes(INTERVAL_MINUTES)
        }

        var nextDay = candidateDate.plusDays(1)
        while (nextDay.dayOfWeek == DayOfWeek.SATURDAY || nextDay.dayOfWeek == DayOfWeek.SUNDAY) {
            nextDay = nextDay.plusDays(1)
        }
        return nextDay.atTime(START_TIME)
    }
}
