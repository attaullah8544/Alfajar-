package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import java.util.Calendar

object WidgetUpdateScheduler {

    const val ACTION_REFRESH_WIDGETS = "com.example.widget.ACTION_REFRESH_WIDGETS"
    private const val REQUEST_CODE_REFRESH = 1001

    fun hasActiveWidgets(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        val fullIds = manager.getAppWidgetIds(ComponentName(context, AlFajrWidgetProvider::class.java))
        val compactIds = manager.getAppWidgetIds(ComponentName(context, AlFajrCompactWidgetProvider::class.java))
        return fullIds.isNotEmpty() || compactIds.isNotEmpty()
    }

    /**
     * Schedules an inexact/doze-aware refresh right after the next prayer transition
     * or midnight date rollover (whichever occurs sooner), without requiring restricted exact-alarm permissions.
     */
    fun scheduleNextUpdate(context: Context, nextPrayerTimeMs: Long?) {
        if (!hasActiveWidgets(context)) {
            cancelUpdates(context)
            return
        }

        val now = System.currentTimeMillis()
        val midnightCal = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 2)
            set(Calendar.MILLISECOND, 0)
        }
        val nextMidnightMs = midnightCal.timeInMillis

        val nextPrayerTrigger = if (nextPrayerTimeMs != null && nextPrayerTimeMs > now) {
            nextPrayerTimeMs + 1500L
        } else {
            now + 15 * 60 * 1000L
        }

        val triggerAt = minOf(nextPrayerTrigger, nextMidnightMs)
            .coerceAtLeast(now + 5000L)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = createRefreshPendingIntent(context)

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, triggerAt, pendingIntent)
        } catch (_: SecurityException) {
            try {
                alarmManager.set(AlarmManager.RTC, triggerAt, pendingIntent)
            } catch (_: Exception) {}
        }
    }

    fun cancelUpdates(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(createRefreshPendingIntent(context))
    }

    private fun createRefreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlFajrWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGETS
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REFRESH,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
