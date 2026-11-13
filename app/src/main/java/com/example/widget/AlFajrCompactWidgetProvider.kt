package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.WatchPreferences
import com.example.model.HijriCalendarHelper
import com.example.service.PrayerCalculator
import java.time.LocalDate
import java.util.Date

class AlFajrCompactWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            WidgetUpdateScheduler.ACTION_REFRESH_WIDGETS,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED -> {
                AlFajrWidgetProvider.updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        if (!WidgetUpdateScheduler.hasActiveWidgets(context)) {
            WidgetUpdateScheduler.cancelUpdates(context)
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

            val nextPrayerTimeStr = if (nextPrayer != null) {
                if (settings.is24Hour) nextPrayer.formatted24h else nextPrayer.formatted12h
            } else "--:--"

            val baseElapsed = SystemClock.elapsedRealtime() + calc.countdownMs

            val views = RemoteViews(context.packageName, R.layout.widget_al_fajr_compact).apply {
                setTextViewText(R.id.tv_compact_location, settings.locationLabel.uppercase().take(12))
                setTextViewText(R.id.tv_compact_hijri, hijriShort)

                if (settings.is24Hour) {
                    setCharSequence(R.id.tc_compact_live_clock, "setFormat24Hour", "HH:mm")
                    setCharSequence(R.id.tc_compact_live_clock, "setFormat12Hour", "HH:mm")
                } else {
                    setCharSequence(R.id.tc_compact_live_clock, "setFormat12Hour", "hh:mm a")
                    setCharSequence(R.id.tc_compact_live_clock, "setFormat24Hour", "hh:mm a")
                }

                setTextViewText(
                    R.id.tv_compact_next_name,
                    "NEXT: ${nextPrayer?.type?.englishName ?: "PRAYER"}"
                )
                setTextViewText(R.id.tv_compact_next_time, nextPrayerTimeStr)

                // Live counting Chronometer for IN countdown
                setChronometerCountDown(R.id.chrono_compact_countdown, true)
                setChronometer(R.id.chrono_compact_countdown, baseElapsed, "%s", true)

                // Apply background transparency
                val (bgRes, bannerRes) = when (settings.widgetTransparencyPercent) {
                    100 -> R.drawable.widget_bg_trans_100 to R.drawable.widget_banner_trans_100
                    75 -> R.drawable.widget_bg_trans_75 to R.drawable.widget_banner_trans_75
                    50 -> R.drawable.widget_bg_trans_50 to R.drawable.widget_banner_trans_50
                    25 -> R.drawable.widget_bg_trans_25 to R.drawable.widget_banner_trans_25
                    else -> R.drawable.widget_lcd_background to R.drawable.widget_inner_banner
                }
                setInt(R.id.widget_compact_root, "setBackgroundResource", bgRes)
                setInt(R.id.layout_compact_countdown_banner, "setBackgroundResource", bannerRes)

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
            WidgetUpdateScheduler.scheduleNextUpdate(context, nextPrayer?.time?.time)
        }
    }
}
