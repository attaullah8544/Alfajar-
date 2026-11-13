package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.batoulapps.adhan.Madhab
import com.example.MainActivity
import com.example.R
import com.example.data.WatchPreferences
import com.example.model.HijriCalendarHelper
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.service.PrayerCalculator
import java.time.LocalDate
import java.util.Date

class AlFajrWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            WidgetUpdateScheduler.ACTION_REFRESH_WIDGETS,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED -> {
                updateAllWidgets(context)
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
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val fullWidgetComponent = ComponentName(context, AlFajrWidgetProvider::class.java)
            val fullWidgetIds = appWidgetManager.getAppWidgetIds(fullWidgetComponent)
            for (id in fullWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }

            val compactComponent = ComponentName(context, AlFajrCompactWidgetProvider::class.java)
            val compactWidgetIds = appWidgetManager.getAppWidgetIds(compactComponent)
            for (id in compactWidgetIds) {
                AlFajrCompactWidgetProvider.updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = WatchPreferences(context)
            val settings = prefs.loadSettings()
            val now = Date()

            val calc = PrayerCalculator.calculate(settings, now)
            val is24H = settings.is24Hour

            val hijriStr = HijriCalendarHelper.getHijriDate(
                localDate = LocalDate.now(),
                offsetDays = settings.hijriOffsetDays
            )

            val nextPrayer = calc.nextPrayer

            // Dynamic status flags reflecting all active watch settings
            val statusFlags = mutableListOf<String>()
            if (settings.fajrAlarmEnabled) statusFlags.add("ALM")
            if (settings.prayerAlertEnabled) statusFlags.add("PRAY")
            statusFlags.add(if (settings.madhab == Madhab.HANAFI) "HNF" else "SHF")
            statusFlags.add(if (is24H) "24H" else "12H")
            if (!settings.soundEnabled) statusFlags.add("MUTE")
            val flagsStr = statusFlags.joinToString(" • ")

            val baseElapsed = SystemClock.elapsedRealtime() + calc.countdownMs

            val views = RemoteViews(context.packageName, R.layout.widget_al_fajr_full).apply {
                setTextViewText(R.id.tv_widget_location, settings.locationLabel.uppercase().take(12))
                setTextViewText(R.id.tv_widget_flags, flagsStr)
                setTextViewText(R.id.tv_widget_hijri, hijriStr)

                // Live Gregorian Date TextClock (auto-updates at midnight)
                setCharSequence(R.id.tc_widget_gregorian, "setFormat12Hour", "EEE dd MMM")
                setCharSequence(R.id.tc_widget_gregorian, "setFormat24Hour", "EEE dd MMM")

                // Live Clock + Seconds + AM/PM TextClocks
                if (is24H) {
                    setCharSequence(R.id.tc_widget_clock, "setFormat24Hour", "HH:mm")
                    setCharSequence(R.id.tc_widget_clock, "setFormat12Hour", "HH:mm")
                    setViewVisibility(R.id.tc_widget_ampm, View.GONE)
                } else {
                    setCharSequence(R.id.tc_widget_clock, "setFormat12Hour", "hh:mm")
                    setCharSequence(R.id.tc_widget_clock, "setFormat24Hour", "hh:mm")
                    setCharSequence(R.id.tc_widget_ampm, "setFormat12Hour", "a")
                    setCharSequence(R.id.tc_widget_ampm, "setFormat24Hour", "a")
                    setViewVisibility(R.id.tc_widget_ampm, View.VISIBLE)
                }
                setCharSequence(R.id.tc_widget_seconds, "setFormat12Hour", "ss")
                setCharSequence(R.id.tc_widget_seconds, "setFormat24Hour", "ss")

                // Next Prayer Name & Live Counting Chronometer ("IN HH:MM:SS")
                setTextViewText(
                    R.id.tv_widget_next_name,
                    "${nextPrayer?.type?.englishName ?: "PRAYER"} (${nextPrayer?.type?.arabicName ?: ""})"
                )
                setChronometerCountDown(R.id.chrono_widget_countdown, true)
                setChronometer(R.id.chrono_widget_countdown, baseElapsed, "IN %s", true)

                // Bind 6 prayers with active/passed highlighting
                bindPrayerTimes(this, calc.prayers, is24H)

                // Apply background transparency
                val (bgRes, bannerRes) = when (settings.widgetTransparencyPercent) {
                    100 -> R.drawable.widget_bg_trans_100 to R.drawable.widget_banner_trans_100
                    75 -> R.drawable.widget_bg_trans_75 to R.drawable.widget_banner_trans_75
                    50 -> R.drawable.widget_bg_trans_50 to R.drawable.widget_banner_trans_50
                    25 -> R.drawable.widget_bg_trans_25 to R.drawable.widget_banner_trans_25
                    else -> R.drawable.widget_lcd_background to R.drawable.widget_inner_banner
                }
                setInt(R.id.widget_root, "setBackgroundResource", bgRes)
                setInt(R.id.layout_next_banner, "setBackgroundResource", bannerRes)

                // Launch App when widget tapped
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
            WidgetUpdateScheduler.scheduleNextUpdate(context, nextPrayer?.time?.time)
        }

        private fun bindPrayerTimes(
            views: RemoteViews,
            prayers: List<PrayerTimeItem>,
            is24Hour: Boolean
        ) {
            val formatTime = { item: PrayerTimeItem? ->
                if (item == null) "--:--"
                else if (is24Hour) item.formatted24h
                else item.formatted12h.replace(" AM", "").replace(" PM", "")
            }

            data class PrayerSlot(
                val type: PrayerType,
                val layoutId: Int,
                val labelId: Int,
                val timeId: Int
            )

            val slots = listOf(
                PrayerSlot(PrayerType.FAJR, R.id.layout_fajr, R.id.tv_label_fajr, R.id.tv_time_fajr),
                PrayerSlot(PrayerType.SHUROOQ, R.id.layout_shurooq, R.id.tv_label_shurooq, R.id.tv_time_shurooq),
                PrayerSlot(PrayerType.DHUHR, R.id.layout_dhuhr, R.id.tv_label_dhuhr, R.id.tv_time_dhuhr),
                PrayerSlot(PrayerType.ASR, R.id.layout_asr, R.id.tv_label_asr, R.id.tv_time_asr),
                PrayerSlot(PrayerType.MAGHRIB, R.id.layout_maghrib, R.id.tv_label_maghrib, R.id.tv_time_maghrib),
                PrayerSlot(PrayerType.ISHA, R.id.layout_isha, R.id.tv_label_isha, R.id.tv_time_isha)
            )

            val goldColor = 0xFFE5BA69.toInt()
            val normalLabelColor = 0xFF8FB5A2.toInt()
            val normalTimeColor = 0xFFF4FAF6.toInt()
            val passedLabelColor = 0x778FB5A2.toInt()
            val passedTimeColor = 0x88F4FAF6.toInt()

            slots.forEach { slot ->
                val item = prayers.find { it.type == slot.type }
                views.setTextViewText(slot.timeId, formatTime(item))

                when {
                    item?.isNext == true -> {
                        views.setInt(slot.layoutId, "setBackgroundResource", R.drawable.widget_prayer_pill_active)
                        views.setTextColor(slot.labelId, goldColor)
                        views.setTextColor(slot.timeId, goldColor)
                    }
                    item?.isPassed == true -> {
                        views.setInt(slot.layoutId, "setBackgroundResource", 0)
                        views.setTextColor(slot.labelId, passedLabelColor)
                        views.setTextColor(slot.timeId, passedTimeColor)
                    }
                    else -> {
                        views.setInt(slot.layoutId, "setBackgroundResource", 0)
                        views.setTextColor(slot.labelId, normalLabelColor)
                        views.setTextColor(slot.timeId, normalTimeColor)
                    }
                }
            }
        }
    }
}
