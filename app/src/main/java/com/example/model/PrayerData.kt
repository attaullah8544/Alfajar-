package com.example.model

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import java.util.Date

enum class WatchMode(val title: String) {
    TIME("WATCH"),
    PRAYERS("PRAYER LIST"),
    QIBLA("QIBLA"),
    ALARM("FAJR ALARM"),
    SETTINGS("SETTINGS")
}

enum class PrayerType(val code: String, val englishName: String, val arabicName: String) {
    FAJR("FAJR", "FAJR", "الفجر"),
    SHUROOQ("SHRQ", "SHUROOQ", "الشروق"),
    DHUHR("ZUHR", "DHUHR", "الظهر"),
    ASR("ASR", "ASR", "العصر"),
    MAGHRIB("MAGH", "MAGHRIB", "المغرب"),
    ISHA("ISHA", "ISHA", "العشاء")
}

data class PrayerTimeItem(
    val type: PrayerType,
    val time: Date,
    val formatted12h: String,
    val formatted24h: String,
    val isNext: Boolean = false,
    val isPassed: Boolean = false
)

data class CityPreset(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val defaultMethod: CalculationMethod = CalculationMethod.KARACHI
) {
    val displayName: String get() = "$name, $country"
}

object PresetCities {
    val Karachi = CityPreset("karachi", "Karachi", "Pakistan", 24.8607, 67.0011, CalculationMethod.KARACHI)
    val Makkah = CityPreset("makkah", "Makkah", "Saudi Arabia", 21.4225, 39.8262, CalculationMethod.UMM_AL_QURA)
    val Madinah = CityPreset("madinah", "Madinah", "Saudi Arabia", 24.5247, 39.5692, CalculationMethod.UMM_AL_QURA)
    val Cairo = CityPreset("cairo", "Cairo", "Egypt", 30.0444, 31.2357, CalculationMethod.EGYPTIAN)
    val London = CityPreset("london", "London", "UK", 51.5074, -0.1278, CalculationMethod.MUSLIM_WORLD_LEAGUE)
    val NewYork = CityPreset("new_york", "New York", "USA", 40.7128, -74.0060, CalculationMethod.NORTH_AMERICA)
    val Dubai = CityPreset("dubai", "Dubai", "UAE", 25.2048, 55.2708, CalculationMethod.DUBAI)
    val Istanbul = CityPreset("istanbul", "Istanbul", "Turkey", 41.0082, 28.9784, CalculationMethod.MUSLIM_WORLD_LEAGUE)
    val Jakarta = CityPreset("jakarta", "Jakarta", "Indonesia", -6.2088, 106.8456, CalculationMethod.SINGAPORE)
    val KualaLumpur = CityPreset("kuala_lumpur", "Kuala Lumpur", "Malaysia", 3.1390, 101.6869, CalculationMethod.SINGAPORE)
    val Lahore = CityPreset("lahore", "Lahore", "Pakistan", 31.5204, 74.3587, CalculationMethod.KARACHI)
    val Islamabad = CityPreset("islamabad", "Islamabad", "Pakistan", 33.6844, 73.0479, CalculationMethod.KARACHI)

    val list = listOf(
        Karachi, Makkah, Madinah, Cairo, London, NewYork, Dubai, Istanbul, Jakarta, KualaLumpur, Lahore, Islamabad
    )
}

data class CalculationMethodInfo(
    val method: CalculationMethod,
    val name: String,
    val description: String
)

object CalculationMethods {
    val list = listOf(
        CalculationMethodInfo(CalculationMethod.KARACHI, "Univ. Islamic Sciences, Karachi", "Fajr 18°, Isha 18° (Pakistan, Bangladesh, India, Afghanistan)"),
        CalculationMethodInfo(CalculationMethod.UMM_AL_QURA, "Umm Al-Qura, Makkah", "Fajr 18.5°, Isha 90min after Maghrib (Arabian Peninsula)"),
        CalculationMethodInfo(CalculationMethod.MUSLIM_WORLD_LEAGUE, "Muslim World League", "Fajr 18°, Isha 17° (Europe, Far East)"),
        CalculationMethodInfo(CalculationMethod.EGYPTIAN, "Egyptian General Authority", "Fajr 19.5°, Isha 17.5° (Africa, Syria, Lebanon)"),
        CalculationMethodInfo(CalculationMethod.NORTH_AMERICA, "ISNA (North America)", "Fajr 15°, Isha 15° (USA, Canada)"),
        CalculationMethodInfo(CalculationMethod.DUBAI, "Dubai (UAE)", "UAE Awqaf parameters"),
        CalculationMethodInfo(CalculationMethod.MOON_SIGHTING_COMMITTEE, "Moonsighting Committee", "Moonsighting worldwide calculation"),
        CalculationMethodInfo(CalculationMethod.QATAR, "Qatar", "Qatar Ministry of Awqaf"),
        CalculationMethodInfo(CalculationMethod.KUWAIT, "Kuwait", "Kuwait Ministry of Awqaf")
    )
}

data class WatchSettings(
    val selectedCity: CityPreset = PresetCities.Karachi,
    val customLat: Double? = null,
    val customLng: Double? = null,
    val locationLabel: String = "KARACHI",
    val calculationMethod: CalculationMethod = CalculationMethod.KARACHI,
    val madhab: Madhab = Madhab.HANAFI,
    val is24Hour: Boolean = false,
    val fajrAlarmEnabled: Boolean = true,
    val fajrAlarmOffsetMinutes: Int = 15, // Ring 15 min before Fajr
    val prayerAlertEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val hourlyChimeEnabled: Boolean = false,
    val hijriOffsetDays: Int = 0
)

data class WatchState(
    val timeMillis: Long = System.currentTimeMillis(),
    val hours: String = "12",
    val minutes: String = "00",
    val seconds: String = "00",
    val amPm: String = "AM",
    val dayOfWeek: String = "MON",
    val gregorianDate: String = "19 OCT 2026",
    val hijriDate: String = "08 RABI' II 1448 AH",
    val prayers: List<PrayerTimeItem> = emptyList(),
    val nextPrayer: PrayerTimeItem? = null,
    val countdownToNext: String = "00:00:00",
    val fajrAlarmTimeFormatted: String = "--:--",
    val isAlarmRinging: Boolean = false,
    val isBacklightOn: Boolean = false,
    val mode: WatchMode = WatchMode.TIME,
    val qiblaDirection: Double = 0.0,
    val kaabaDistanceKm: Int = 0,
    val settings: WatchSettings = WatchSettings()
)
