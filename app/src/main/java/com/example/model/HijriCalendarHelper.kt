package com.example.model

import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

object HijriCalendarHelper {

    private val MONTH_NAMES_EN = arrayOf(
        "MUHARRAM",
        "SAFAR",
        "RABI' I",
        "RABI' II",
        "JUMADA I",
        "JUMADA II",
        "RAJAB",
        "SHA'BAN",
        "RAMADAN",
        "SHAWWAL",
        "DHU AL-QI'DAH",
        "DHU AL-HIJJAH"
    )

    private val MONTH_NAMES_AR = arrayOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الثاني",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    fun getHijriDate(localDate: LocalDate = LocalDate.now(), offsetDays: Int = 0): String {
        return try {
            val adjustedDate = localDate.plusDays(offsetDays.toLong())
            val hijrahDate: HijrahDate = HijrahChronology.INSTANCE.date(adjustedDate)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val monthIndex = hijrahDate.get(ChronoField.MONTH_OF_YEAR) - 1
            val year = hijrahDate.get(ChronoField.YEAR)

            val monthName = if (monthIndex in MONTH_NAMES_EN.indices) MONTH_NAMES_EN[monthIndex] else "HIJRI"
            val dayStr = String.format("%02d", day)
            "$dayStr $monthName $year AH"
        } catch (_: Exception) {
            "08 RABI' II 1448 AH"
        }
    }

    fun getHijriDayAndMonth(localDate: LocalDate = LocalDate.now(), offsetDays: Int = 0): Pair<Int, String> {
        return try {
            val adjustedDate = localDate.plusDays(offsetDays.toLong())
            val hijrahDate: HijrahDate = HijrahChronology.INSTANCE.date(adjustedDate)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val monthIndex = hijrahDate.get(ChronoField.MONTH_OF_YEAR) - 1
            val monthName = if (monthIndex in MONTH_NAMES_EN.indices) MONTH_NAMES_EN[monthIndex] else "HIJRI"
            Pair(day, monthName)
        } catch (_: Exception) {
            Pair(8, "RABI' II")
        }
    }
}
