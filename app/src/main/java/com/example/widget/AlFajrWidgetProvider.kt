package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.batoulapps.adhan.Madhab
import com.example.MainActivity
import com.example.R
import com.example.data.WatchPreferences
import com.example.model.HijriCalendarHelper
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.service.PrayerCalculator
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlFajrWidgetProvider : AppWidgetProvider() {

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
            val calendar = Calendar.getInstance().apply { time = now }

            val calc = PrayerCalculator.calculate(settings, now)

            val is24H = settings.is24Hour
            val clockStr = if (is24H) {
                String.format(Locale.ENGLISH, "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            } else {
                val h = calendar.get(Calendar.HOUR)
                val h12 = if (h == 0) 12 else h
                String.format(Locale.ENGLISH, "%02d:%02d", h12, calendar.get(Calendar.MINUTE))
            }
            val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

            val dayFormat = SimpleDateFormat("EEE dd MMM", Locale.ENGLISH)
            val gregorianStr = dayFormat.format(now).uppercase()

            val hijriStr = HijriCalendarHelper.getHijriDate(
                localDate = LocalDate.now(),
                offsetDays = settings.hijriOffsetDays
            )

            val countdownMs = calc.countdownMs
            val totalSec = countdownMs / 1000
            val h = totalSec / 3600
            val m = (totalSec % 3600) / 60
            val s = totalSec % 60
            val countdownStr = String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)

            val nextPrayer = calc.nextPrayer

            val madhabTag = if (settings.madhab == Madhab.HANAFI) "HNF" else "SHF"
            val flagsStr = "ALM • PRAY • $madhabTag"

            val views = RemoteViews(context.packageName, R.layout.widget_al_fajr_full).apply {
                setTextViewText(R.id.tv_widget_location, settings.locationLabel.uppercase().take(12))
                setTextViewText(R.id.tv_widget_flags, flagsStr)
                setTextViewText(R.id.tv_widget_gregorian, gregorianStr)
                setTextViewText(R.id.tv_widget_hijri, hijriStr)

                // Configure live TextClock formats according to 12h / 24h setting
                if (is24H) {
                    setCharSequence(R.id.tc_widget_clock, "setFormat24Hour", "HH:mm")
                    setCharSequence(R.id.tc_widget_clock, "setFormat12Hour", "HH:mm")
                    setCharSequence(R.id.tc_widget_ampm, "setFormat24Hour", "")
                    setCharSequence(R.id.tc_widget_ampm, "setFormat12Hour", "")
                } else {
                    setCharSequence(R.id.tc_widget_clock, "setFormat12Hour", "hh:mm")
                    setCharSequence(R.id.tc_widget_clock, "setFormat24Hour", "hh:mm")
                    setCharSequence(R.id.tc_widget_ampm, "setFormat12Hour", "a")
                    setCharSequence(R.id.tc_widget_ampm, "setFormat24Hour", "a")
                }

                setTextViewText(
                    R.id.tv_widget_next_name,
                    "${nextPrayer?.type?.englishName ?: "PRAYER"} (${nextPrayer?.type?.arabicName ?: ""})"
                )
                setTextViewText(R.id.tv_widget_next_countdown, "IN $countdownStr")

                // Bind 6 prayers
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

            val fajr = prayers.find { it.type == PrayerType.FAJR }
            val shurooq = prayers.find { it.type == PrayerType.SHUROOQ }
            val dhuhr = prayers.find { it.type == PrayerType.DHUHR }
            val asr = prayers.find { it.type == PrayerType.ASR }
            val maghrib = prayers.find { it.type == PrayerType.MAGHRIB }
            val isha = prayers.find { it.type == PrayerType.ISHA }

            views.setTextViewText(R.id.tv_time_fajr, formatTime(fajr))
            views.setTextViewText(R.id.tv_time_shurooq, formatTime(shurooq))
            views.setTextViewText(R.id.tv_time_dhuhr, formatTime(dhuhr))
            views.setTextViewText(R.id.tv_time_asr, formatTime(asr))
            views.setTextViewText(R.id.tv_time_maghrib, formatTime(maghrib))
            views.setTextViewText(R.id.tv_time_isha, formatTime(isha))
        }
    }
}
