package com.example.service

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.PrayerTimes
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.model.WatchSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PrayerCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    data class CalculationResult(
        val prayers: List<PrayerTimeItem>,
        val nextPrayer: PrayerTimeItem?,
        val countdownMs: Long,
        val fajrAlarmTime: Date,
        val formattedAlarmTime12h: String,
        val formattedAlarmTime24h: String,
        val qiblaDirection: Double,
        val kaabaDistanceKm: Int
    )

    fun calculate(
        settings: WatchSettings,
        currentDate: Date = Date()
    ): CalculationResult {
        val lat = settings.customLat ?: settings.selectedCity.latitude
        val lng = settings.customLng ?: settings.selectedCity.longitude
        val coordinates = Coordinates(lat, lng)

        val params = settings.calculationMethod.parameters
        params.madhab = settings.madhab

        val todayCalendar = Calendar.getInstance().apply { time = currentDate }
        val todayComponents = DateComponents.from(currentDate)
        val todayPrayers = PrayerTimes(coordinates, todayComponents, params)

        // Tomorrow's prayer times (for countdown after Isha)
        val tomorrowCalendar = (todayCalendar.clone() as Calendar).apply { add(Calendar.DATE, 1) }
        val tomorrowComponents = DateComponents.from(tomorrowCalendar.time)
        val tomorrowPrayers = PrayerTimes(coordinates, tomorrowComponents, params)

        val formatter12h = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val formatter24h = SimpleDateFormat("HH:mm", Locale.ENGLISH)

        val nowMs = currentDate.time

        // 6 prayers of today
        val rawList = listOf(
            PrayerTimeItem(PrayerType.FAJR, todayPrayers.fajr, formatter12h.format(todayPrayers.fajr), formatter24h.format(todayPrayers.fajr)),
            PrayerTimeItem(PrayerType.SHUROOQ, todayPrayers.sunrise, formatter12h.format(todayPrayers.sunrise), formatter24h.format(todayPrayers.sunrise)),
            PrayerTimeItem(PrayerType.DHUHR, todayPrayers.dhuhr, formatter12h.format(todayPrayers.dhuhr), formatter24h.format(todayPrayers.dhuhr)),
            PrayerTimeItem(PrayerType.ASR, todayPrayers.asr, formatter12h.format(todayPrayers.asr), formatter24h.format(todayPrayers.asr)),
            PrayerTimeItem(PrayerType.MAGHRIB, todayPrayers.maghrib, formatter12h.format(todayPrayers.maghrib), formatter24h.format(todayPrayers.maghrib)),
            PrayerTimeItem(PrayerType.ISHA, todayPrayers.isha, formatter12h.format(todayPrayers.isha), formatter24h.format(todayPrayers.isha))
        )

        // Determine next prayer and countdown
        var nextItem: PrayerTimeItem? = null
        var minDiff = Long.MAX_VALUE

        for (item in rawList) {
            val diff = item.time.time - nowMs
            if (diff > 0 && diff < minDiff) {
                minDiff = diff
                nextItem = item
            }
        }

        // If all prayers today passed, next prayer is Tomorrow's Fajr
        val isAllPassedToday = (nextItem == null)
        val nextPrayerResult: PrayerTimeItem
        val countdownMs: Long

        if (isAllPassedToday) {
            val tomorrowFajr = tomorrowPrayers.fajr
            nextPrayerResult = PrayerTimeItem(
                PrayerType.FAJR,
                tomorrowFajr,
                formatter12h.format(tomorrowFajr),
                formatter24h.format(tomorrowFajr),
                isNext = true
            )
            countdownMs = maxOf(0L, tomorrowFajr.time - nowMs)
        } else {
            nextPrayerResult = nextItem!!
            countdownMs = maxOf(0L, minDiff)
        }

        // Update list with isNext & isPassed
        val prayerListWithStatus = rawList.map { item ->
            item.copy(
                isNext = (item.type == nextPrayerResult.type && !isAllPassedToday),
                isPassed = item.time.time < nowMs
            )
        }

        // Fajr relative alarm calculation
        // Offset: e.g. 15 minutes before Fajr
        val offsetMs = settings.fajrAlarmOffsetMinutes * 60 * 1000L
        val todayFajrAlarmMs = todayPrayers.fajr.time - offsetMs
        val tomorrowFajrAlarmMs = tomorrowPrayers.fajr.time - offsetMs

        val alarmTime = if (nowMs < todayFajrAlarmMs) {
            Date(todayFajrAlarmMs)
        } else {
            Date(tomorrowFajrAlarmMs)
        }

        // Qibla direction & distance
        val (bearing, distanceKm) = calculateQibla(lat, lng)

        return CalculationResult(
            prayers = prayerListWithStatus,
            nextPrayer = nextPrayerResult,
            countdownMs = countdownMs,
            fajrAlarmTime = alarmTime,
            formattedAlarmTime12h = formatter12h.format(alarmTime),
            formattedAlarmTime24h = formatter24h.format(alarmTime),
            qiblaDirection = bearing,
            kaabaDistanceKm = distanceKm
        )
    }

    private fun calculateQibla(userLat: Double, userLng: Double): Pair<Double, Int> {
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LNG - userLng)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        var qiblaDegrees = Math.toDegrees(atan2(y, x))
        qiblaDegrees = (qiblaDegrees + 360.0) % 360.0

        // Haversine distance
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(KAABA_LAT - userLat)
        val dLon = Math.toRadians(KAABA_LNG - userLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(phi1) * cos(phi2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = (earthRadiusKm * c).toInt()

        return Pair(qiblaDegrees, distanceKm)
    }
}
