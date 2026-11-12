package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.WatchPreferences
import com.example.model.HijriCalendarHelper
import com.example.service.PrayerCalculator
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class AlFajrCompactWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = WatchPreferences(context)
            val settings = prefs.loadSettings()
            val now = Date()

            val calc = PrayerCalculator.calculate(settings, now)
            val nextPrayer = calc.nextPrayer

            val (day, month) = HijriCalendarHelper.getHijriDayAndMonth(
                localDate = LocalDate.now(),
                offsetDays = settings.hijriOffsetDays
            )
            val hijriShort = String.format("%02d %s", day, month)

            val countdownMs = calc.countdownMs
            val totalSec = countdownMs / 1000
            val h = totalSec / 3600
            val m = (totalSec % 3600) / 60
            val s = totalSec % 60
            val countdownStr = String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)

            val nextPrayerTimeStr = if (nextPrayer != null) {
                if (settings.is24Hour) nextPrayer.formatted24h else nextPrayer.formatted12h
            } else "--:--"

            val views = RemoteViews(context.packageName, R.layout.widget_al_fajr_compact).apply {
                setTextViewText(R.id.tv_compact_location, settings.locationLabel.uppercase().take(12))
                setTextViewText(R.id.tv_compact_hijri, hijriShort)
                setTextViewText(
                    R.id.tv_compact_next_name,
                    "NEXT: ${nextPrayer?.type?.englishName ?: "PRAYER"}"
                )
                setTextViewText(R.id.tv_compact_next_time, nextPrayerTimeStr)
                setTextViewText(R.id.tv_compact_countdown, countdownStr)

                // Launch App when widget tapped
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_compact_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
