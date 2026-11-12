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

    @Test
    fun hijriCalendarHelper_returnsDetailedHijriComponents() {
        val details = HijriCalendarHelper.getHijriDetails(LocalDate.of(2026, 10, 19), offsetDays = 0)
        assertTrue(details.day in 1..30)
        assertTrue(details.monthNumber in 1..12)
        assertTrue(details.year >= 1445)
        assertTrue(details.monthNameEn.isNotEmpty())
        assertTrue(details.monthNameAr.isNotEmpty())
        assertTrue(details.fullDateAr.contains("هـ"))
    }

    @Test
    fun hijriCalendarHelper_handlesOffsetDaysCorrectly() {
        val base = HijriCalendarHelper.getHijriDetails(LocalDate.of(2026, 10, 19), offsetDays = 0)
        val plusOne = HijriCalendarHelper.getHijriDetails(LocalDate.of(2026, 10, 19), offsetDays = 1)
        val minusOne = HijriCalendarHelper.getHijriDetails(LocalDate.of(2026, 10, 19), offsetDays = -1)

        // Day should reflect day offset
        assertTrue(base.day != plusOne.day || base.monthNumber != plusOne.monthNumber)
        assertTrue(base.day != minusOne.day || base.monthNumber != minusOne.monthNumber)
    }

    @Test
    fun locationDefaultsHelper_recommendsCorrectMethodAndMadhab() {
        // Pakistan -> Karachi, Hanafi
        val pkMethod = com.example.model.LocationDefaultsHelper.recommendCalculationMethod("pk", "Pakistan")
        val pkMadhab = com.example.model.LocationDefaultsHelper.recommendMadhab("pk", "Pakistan")
        assertEquals(CalculationMethod.KARACHI, pkMethod)
        assertEquals(Madhab.HANAFI, pkMadhab)

        // Saudi Arabia -> Umm Al Qura, Shafi'i
        val saMethod = com.example.model.LocationDefaultsHelper.recommendCalculationMethod("sa", "Saudi Arabia")
        val saMadhab = com.example.model.LocationDefaultsHelper.recommendMadhab("sa", "Saudi Arabia")
        assertEquals(CalculationMethod.UMM_AL_QURA, saMethod)
        assertEquals(Madhab.SHAFI, saMadhab)

        // Egypt -> Egyptian
        val egMethod = com.example.model.LocationDefaultsHelper.recommendCalculationMethod("eg", "Egypt")
        assertEquals(CalculationMethod.EGYPTIAN, egMethod)

        // USA -> North America
        val usMethod = com.example.model.LocationDefaultsHelper.recommendCalculationMethod("us", "United States")
        assertEquals(CalculationMethod.NORTH_AMERICA, usMethod)
    }

    @Test
    fun watchSettings_widgetTransparency_defaultsToSolidZero() {
        val settings = WatchSettings()
        assertEquals(0, settings.widgetTransparencyPercent)

        val transparentSettings = settings.copy(widgetTransparencyPercent = 100)
        assertEquals(100, transparentSettings.widgetTransparencyPercent)
    }
}
