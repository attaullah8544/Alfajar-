package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.example.model.CityPreset
import com.example.model.PresetCities
import com.example.model.WatchSettings

class WatchPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("al_fajr_watch_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CITY_ID = "selected_city_id"
        private const val KEY_CUSTOM_LAT = "custom_lat"
        private const val KEY_CUSTOM_LNG = "custom_lng"
        private const val KEY_LOCATION_LABEL = "location_label"
        private const val KEY_CALC_METHOD = "calc_method"
        private const val KEY_MADHAB_HANAFI = "madhab_hanafi"
        private const val KEY_24_HOUR = "is_24_hour"
        private const val KEY_FAJR_ALARM_ENABLED = "fajr_alarm_enabled"
        private const val KEY_FAJR_ALARM_OFFSET = "fajr_alarm_offset"
        private const val KEY_PRAYER_ALERT = "prayer_alert_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HOURLY_CHIME = "hourly_chime"
        private const val KEY_HIJRI_OFFSET = "hijri_offset"
    }

    fun loadSettings(): WatchSettings {
        val cityId = prefs.getString(KEY_CITY_ID, "karachi") ?: "karachi"
        val city = PresetCities.list.find { it.id == cityId } ?: PresetCities.Karachi
        val customLat = if (prefs.contains(KEY_CUSTOM_LAT)) prefs.getFloat(KEY_CUSTOM_LAT, 0f).toDouble() else null
        val customLng = if (prefs.contains(KEY_CUSTOM_LNG)) prefs.getFloat(KEY_CUSTOM_LNG, 0f).toDouble() else null
        val label = prefs.getString(KEY_LOCATION_LABEL, city.name.uppercase()) ?: city.name.uppercase()

        val methodName = prefs.getString(KEY_CALC_METHOD, CalculationMethod.KARACHI.name)
        val method = try {
            CalculationMethod.valueOf(methodName ?: CalculationMethod.KARACHI.name)
        } catch (_: Exception) {
            CalculationMethod.KARACHI
        }

        val isHanafi = prefs.getBoolean(KEY_MADHAB_HANAFI, true)
        val is24Hour = prefs.getBoolean(KEY_24_HOUR, false)
        val fajrAlarmEnabled = prefs.getBoolean(KEY_FAJR_ALARM_ENABLED, true)
        val fajrOffset = prefs.getInt(KEY_FAJR_ALARM_OFFSET, 15)
        val prayerAlert = prefs.getBoolean(KEY_PRAYER_ALERT, true)
        val sound = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        val chime = prefs.getBoolean(KEY_HOURLY_CHIME, false)
        val hijriOffset = prefs.getInt(KEY_HIJRI_OFFSET, 0)

        return WatchSettings(
            selectedCity = city,
            customLat = customLat,
            customLng = customLng,
            locationLabel = label,
            calculationMethod = method,
            madhab = if (isHanafi) Madhab.HANAFI else Madhab.SHAFI,
            is24Hour = is24Hour,
            fajrAlarmEnabled = fajrAlarmEnabled,
            fajrAlarmOffsetMinutes = fajrOffset,
            prayerAlertEnabled = prayerAlert,
            soundEnabled = sound,
            hourlyChimeEnabled = chime,
            hijriOffsetDays = hijriOffset
        )
    }

    fun saveSettings(settings: WatchSettings) {
        prefs.edit().apply {
            putString(KEY_CITY_ID, settings.selectedCity.id)
            if (settings.customLat != null && settings.customLng != null) {
                putFloat(KEY_CUSTOM_LAT, settings.customLat.toFloat())
                putFloat(KEY_CUSTOM_LNG, settings.customLng.toFloat())
            } else {
                remove(KEY_CUSTOM_LAT)
                remove(KEY_CUSTOM_LNG)
            }
            putString(KEY_LOCATION_LABEL, settings.locationLabel)
            putString(KEY_CALC_METHOD, settings.calculationMethod.name)
            putBoolean(KEY_MADHAB_HANAFI, settings.madhab == Madhab.HANAFI)
            putBoolean(KEY_24_HOUR, settings.is24Hour)
            putBoolean(KEY_FAJR_ALARM_ENABLED, settings.fajrAlarmEnabled)
            putInt(KEY_FAJR_ALARM_OFFSET, settings.fajrAlarmOffsetMinutes)
            putBoolean(KEY_PRAYER_ALERT, settings.prayerAlertEnabled)
            putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            putBoolean(KEY_HOURLY_CHIME, settings.hourlyChimeEnabled)
            putInt(KEY_HIJRI_OFFSET, settings.hijriOffsetDays)
            apply()
        }
    }
}
