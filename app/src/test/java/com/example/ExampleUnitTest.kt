package com.example

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.example.model.HijriCalendarHelper
import com.example.model.PresetCities
import com.example.model.WatchSettings
import com.example.service.PrayerCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Date

class ExampleUnitTest {

    @Test
    fun prayerCalculator_calculatesSixPrayersForKarachi() {
        val settings = WatchSettings(
            selectedCity = PresetCities.Karachi,
            calculationMethod = CalculationMethod.KARACHI,
            madhab = Madhab.HANAFI
        )
        val result = PrayerCalculator.calculate(settings, Date())
        assertEquals(6, result.prayers.size)
        assertNotNull(result.nextPrayer)
        assertTrue(result.countdownMs >= 0)
        assertTrue(result.qiblaDirection in 0.0..360.0)
        assertTrue(result.kaabaDistanceKm > 0)
    }

    @Test
    fun prayerCalculator_fajrAlarm_is15MinutesBeforeFajr() {
        val settings = WatchSettings(
            selectedCity = PresetCities.Makkah,
            calculationMethod = CalculationMethod.UMM_AL_QURA,
            fajrAlarmEnabled = true,
            fajrAlarmOffsetMinutes = 15
        )
        // First get today's Fajr time
        val initialResult = PrayerCalculator.calculate(settings, Date())
        val todayFajr = initialResult.prayers.first { it.type.name == "FAJR" }.time
        // Set test current time to 30 minutes before Fajr
        val testDate = Date(todayFajr.time - 30 * 60 * 1000L)
        val result = PrayerCalculator.calculate(settings, testDate)
        val fajrPrayer = result.prayers.first { it.type.name == "FAJR" }
        val diffMinutes = (fajrPrayer.time.time - result.fajrAlarmTime.time) / (60 * 1000)

        assertEquals(15L, diffMinutes)
    }

    @Test
    fun hijriCalendarHelper_formatsValidHijriDate() {
        val hijriDateStr = HijriCalendarHelper.getHijriDate(LocalDate.of(2026, 10, 19), offsetDays = 0)
        assertTrue(hijriDateStr.contains("AH"))
    }
}
