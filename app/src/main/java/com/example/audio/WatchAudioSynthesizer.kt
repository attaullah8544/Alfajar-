package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Authentic digital watch piezo beep synthesizer using Android AudioTrack PCM audio.
 * Generates the iconic crisp high-frequency beeps and alarms of classic Al-Fajr and Casio watches.
 */
class WatchAudioSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var alarmJob: Job? = null

    private fun generateTone(frequencyHz: Double, durationMs: Int, sampleRate: Int = 44100): ByteArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val generatedSnd = ByteArray(2 * numSamples)
        for (i in 0 until numSamples) {
            val angle = 2.0 * PI * i / (sampleRate / frequencyHz)
            // Digital watches often use square waves or sharp sine waves
            val sampleVal = if (sin(angle) >= 0) 0.7 else -0.7
            val sample = (sampleVal * 32767).toInt().toShort()
            val index = i * 2
            generatedSnd[index] = (sample.toInt() and 0x00ff).toByte()
            generatedSnd[index + 1] = ((sample.toInt() and 0xff00) shr 8).toByte()
        }
        return generatedSnd
    }

    private fun playPcm(buffer: ByteArray, sampleRate: Int = 44100) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Release after track finishes
            scope.launch {
                delay(150)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // AudioTrack might fail if device is muted or in restricted audio mode
        }
    }

    /**
     * Standard digital watch button tap beep: sharp ~3900 Hz tone for 32 ms.
     */
    fun playButtonBeep(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            val tone = generateTone(frequencyHz = 3920.0, durationMs = 32)
            playPcm(tone)
        }
    }

    /**
     * Dual-tone chirp for MODE switching: 3500Hz -> 4200Hz.
     */
    fun playModeChirp(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            val tone1 = generateTone(frequencyHz = 3520.0, durationMs = 28)
            val tone2 = generateTone(frequencyHz = 4186.0, durationMs = 36)
            playPcm(tone1)
            delay(35)
            playPcm(tone2)
        }
    }

    /**
     * Soft click beep for LIGHT activation.
     */
    fun playLightClick(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            val tone = generateTone(frequencyHz = 3300.0, durationMs = 20)
            playPcm(tone)
        }
    }

    /**
     * Plays the authentic Al-Fajr digital watch alarm chime (repeating double-beep cadence).
     */
    fun startAlarmBeep(onCadence: (() -> Unit)? = null) {
        stopAlarm()
        alarmJob = scope.launch {
            val beep = generateTone(frequencyHz = 4000.0, durationMs = 60)
            while (isActive) {
                onCadence?.invoke()
                playPcm(beep)
                delay(90)
                playPcm(beep)
                delay(90)
                playPcm(beep)
                delay(90)
                playPcm(beep)
                delay(650) // Pause between alarm sequences
            }
        }
    }

    fun stopAlarm() {
        alarmJob?.cancel()
        alarmJob = null
    }
}
