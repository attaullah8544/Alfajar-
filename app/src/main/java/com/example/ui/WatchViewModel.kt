package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.example.audio.WatchAudioSynthesizer
import com.example.data.WatchPreferences
import com.example.model.CityPreset
import com.example.model.HijriCalendarHelper
import com.example.service.PrayerCalculator
import com.example.model.PrayerTimeItem
import com.example.model.PresetCities
import com.example.model.WatchMode
import com.example.model.WatchSettings
import com.example.model.WatchState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WatchViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = WatchPreferences(application)
    val audioSynthesizer = WatchAudioSynthesizer()

    private val _state = MutableStateFlow(WatchState())
    val state: StateFlow<WatchState> = _state.asStateFlow()

    private var tickerJob: Job? = null
    private var backlightTimerJob: Job? = null

    init {
        val savedSettings = preferences.loadSettings()
        _state.update { it.copy(settings = savedSettings) }
        startWatchTicker()
    }

    private fun startWatchTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                tick()
                delay(1000L)
            }
        }
    }

    private fun tick() {
        val now = Date()
        val calendar = Calendar.getInstance().apply { time = now }
        val currentSettings = _state.value.settings

        val is24H = currentSettings.is24Hour
        val hours = if (is24H) {
            String.format(Locale.ENGLISH, "%02d", calendar.get(Calendar.HOUR_OF_DAY))
        } else {
            val h = calendar.get(Calendar.HOUR)
            val h12 = if (h == 0) 12 else h
            String.format(Locale.ENGLISH, "%02d", h12)
        }
        val minutes = String.format(Locale.ENGLISH, "%02d", calendar.get(Calendar.MINUTE))
        val seconds = String.format(Locale.ENGLISH, "%02d", calendar.get(Calendar.SECOND))
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"

        val dayFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val dayOfWeek = dayFormat.format(now).uppercase()

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val gregorianDate = dateFormat.format(now).uppercase()

        val hijriDate = HijriCalendarHelper.getHijriDate(
            localDate = LocalDate.now(),
            offsetDays = currentSettings.hijriOffsetDays
        )

        // Prayer calculations
        val calc = com.example.service.PrayerCalculator.calculate(currentSettings, now)

        val countdownStr = formatCountdown(calc.countdownMs)

        // Check alarm trigger
        val isAlarmTime = currentSettings.fajrAlarmEnabled &&
                calendar.get(Calendar.SECOND) == 0 &&
                Math.abs(now.time - calc.fajrAlarmTime.time) < 60000L

        if (isAlarmTime && !_state.value.isAlarmRinging) {
            triggerAlarm()
        }

        _state.update { current ->
            current.copy(
                timeMillis = now.time,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                amPm = amPm,
                dayOfWeek = dayOfWeek,
                gregorianDate = gregorianDate,
                hijriDate = hijriDate,
                prayers = calc.prayers,
                nextPrayer = calc.nextPrayer,
                countdownToNext = countdownStr,
                fajrAlarmTimeFormatted = if (is24H) calc.formattedAlarmTime24h else calc.formattedAlarmTime12h,
                qiblaDirection = calc.qiblaDirection,
                kaabaDistanceKm = calc.kaabaDistanceKm
            )
        }
    }

    private fun formatCountdown(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
    }

    // Interactive button actions
    fun onModePressed() {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playModeChirp(sound)

        val modes = WatchMode.entries
        val nextIndex = (_state.value.mode.ordinal + 1) % modes.size
        _state.update { it.copy(mode = modes[nextIndex]) }
    }

    fun onSelectMode(mode: WatchMode) {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        _state.update { it.copy(mode = mode) }
    }

    fun onLightPressed() {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playLightClick(sound)

        val currentlyOn = _state.value.isBacklightOn
        if (currentlyOn) {
            backlightTimerJob?.cancel()
            _state.update { it.copy(isBacklightOn = false) }
        } else {
            _state.update { it.copy(isBacklightOn = true) }
            backlightTimerJob?.cancel()
            backlightTimerJob = viewModelScope.launch {
                delay(4500L) // 4.5 seconds backlight
                _state.update { it.copy(isBacklightOn = false) }
            }
        }
    }

    fun onSetPressed() {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)

        when (_state.value.mode) {
            WatchMode.ALARM -> {
                // Cycle Fajr alarm offset: 10 -> 15 -> 20 -> 30 -> 0 -> 10
                val currentOffset = _state.value.settings.fajrAlarmOffsetMinutes
                val nextOffset = when (currentOffset) {
                    10 -> 15
                    15 -> 20
                    20 -> 30
                    30 -> 0
                    else -> 10
                }
                updateSettings { it.copy(fajrAlarmOffsetMinutes = nextOffset) }
            }
            WatchMode.PRAYERS -> {
                // Toggle Hanafi / Shafi'i
                toggleMadhab()
            }
            WatchMode.TIME -> {
                // Toggle 12h / 24h
                toggleTimeFormat()
            }
            else -> {
                toggleTimeFormat()
            }
        }
    }

    fun toggleFajrAlarm() {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        val newEnabled = !_state.value.settings.fajrAlarmEnabled
        updateSettings { it.copy(fajrAlarmEnabled = newEnabled) }
    }

    fun toggleMadhab() {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        val currentMadhab = _state.value.settings.madhab
        val nextMadhab = if (currentMadhab == Madhab.HANAFI) Madhab.SHAFI else Madhab.HANAFI
        updateSettings { it.copy(madhab = nextMadhab) }
    }

    fun toggleTimeFormat() {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        val next24 = !_state.value.settings.is24Hour
        updateSettings { it.copy(is24Hour = next24) }
    }

    fun toggleSound() {
        val nextSound = !_state.value.settings.soundEnabled
        if (nextSound) {
            audioSynthesizer.playButtonBeep(true)
        }
        updateSettings { it.copy(soundEnabled = nextSound) }
    }

    fun togglePrayerAlert() {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        val nextAlert = !_state.value.settings.prayerAlertEnabled
        updateSettings { it.copy(prayerAlertEnabled = nextAlert) }
    }

    fun selectCity(city: CityPreset) {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        updateSettings {
            it.copy(
                selectedCity = city,
                customLat = null,
                customLng = null,
                locationLabel = city.name.uppercase(),
                calculationMethod = city.defaultMethod
            )
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        updateSettings { it.copy(calculationMethod = method) }
    }

    fun setFajrOffset(minutes: Int) {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        updateSettings { it.copy(fajrAlarmOffsetMinutes = minutes) }
    }

    fun adjustHijriOffset(delta: Int) {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        val newOffset = (_state.value.settings.hijriOffsetDays + delta).coerceIn(-3, 3)
        updateSettings { it.copy(hijriOffsetDays = newOffset) }
    }

    fun setGpsLocation(lat: Double, lng: Double, label: String = "GPS LOCATION") {
        val sound = _state.value.settings.soundEnabled
        audioSynthesizer.playButtonBeep(sound)
        updateSettings {
            it.copy(
                customLat = lat,
                customLng = lng,
                locationLabel = label.uppercase()
            )
        }
    }

    fun testAlarmToggle() {
        if (_state.value.isAlarmRinging) {
            stopAlarm()
        } else {
            triggerAlarm()
        }
    }

    private fun triggerAlarm() {
        _state.update { it.copy(isAlarmRinging = true) }
        audioSynthesizer.startAlarmBeep()
    }

    fun stopAlarm() {
        audioSynthesizer.stopAlarm()
        _state.update { it.copy(isAlarmRinging = false) }
    }

    private fun updateSettings(transform: (WatchSettings) -> WatchSettings) {
        val updated = transform(_state.value.settings)
        preferences.saveSettings(updated)
        _state.update { it.copy(settings = updated) }
        tick()
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        backlightTimerJob?.cancel()
        audioSynthesizer.stopAlarm()
    }
}
