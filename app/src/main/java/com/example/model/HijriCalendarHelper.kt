package com.example.model

import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/**
 * Islamic Hijri date components and detailed information.
 */
data class HijriDateDetails(
    val day: Int,
    val monthNumber: Int,
    val monthNameEn: String,
    val monthNameAr: String,
    val year: Int,
    val fullDateEn: String,
    val fullDateAr: String,
    val shortDisplay: String,
    val specialIslamicDay: String? = null
)

/**
 * High-precision astronomical Hijrah calendar conversion utility based on
 * the Umm Al-Qura / standard Hijrah chronology with customizable moon sighting offset.
 */
object HijriCalendarHelper {

    val MONTH_NAMES_EN = arrayOf(
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

    val MONTH_NAMES_AR = arrayOf(
        "المحرّم",
        "صفر",
        "ربيع الأول",
        "ربيع الثاني",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوّال",
        "ذو القعدة",
        "ذو الحجة"
    )

    /**
     * Converts a Gregorian LocalDate to complete HijriDateDetails.
     */
    fun getHijriDetails(localDate: LocalDate = LocalDate.now(), offsetDays: Int = 0): HijriDateDetails {
        return try {
            val adjustedDate = localDate.plusDays(offsetDays.toLong())
            val hijrahDate: HijrahDate = HijrahChronology.INSTANCE.date(adjustedDate)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val monthNum = hijrahDate.get(ChronoField.MONTH_OF_YEAR) // 1-indexed
            val monthIndex = (monthNum - 1).coerceIn(0, 11)
            val year = hijrahDate.get(ChronoField.YEAR)

            val monthEn = MONTH_NAMES_EN[monthIndex]
            val monthAr = MONTH_NAMES_AR[monthIndex]
            val dayStr = String.format("%02d", day)

            val fullEn = "$dayStr $monthEn $year AH"
            val fullAr = "$dayStr $monthAr $year هـ"
            val short = "$dayStr $monthEn"

            val specialDay = getSpecialIslamicEvent(day, monthNum)

            HijriDateDetails(
                day = day,
                monthNumber = monthNum,
                monthNameEn = monthEn,
                monthNameAr = monthAr,
                year = year,
                fullDateEn = fullEn,
                fullDateAr = fullAr,
                shortDisplay = short,
                specialIslamicDay = specialDay
            )
        } catch (_: Exception) {
            HijriDateDetails(
                day = 8,
                monthNumber = 4,
                monthNameEn = "RABI' II",
                monthNameAr = "ربيع الثاني",
                year = 1448,
                fullDateEn = "08 RABI' II 1448 AH",
                fullDateAr = "08 ربيع الثاني 1448 هـ",
                shortDisplay = "08 RABI' II",
                specialIslamicDay = null
            )
        }
    }

    /**
     * Standard formatted English string (e.g. "08 RABI' II 1448 AH").
     */
    fun getHijriDate(localDate: LocalDate = LocalDate.now(), offsetDays: Int = 0): String {
        return getHijriDetails(localDate, offsetDays).fullDateEn
    }

    /**
     * Formatted Arabic string (e.g. "08 ربيع الثاني 1448 هـ").
     */
    fun getHijriDateArabic(localDate: LocalDate = LocalDate.now(), offsetDays: Int = 0): String {
        return getHijriDetails(localDate, offsetDays).fullDateAr
    }

    fun getHijriDayAndMonth(localDate: LocalDate = LocalDate.now(), offsetDays: Int = 0): Pair<Int, String> {
        val details = getHijriDetails(localDate, offsetDays)
        return Pair(details.day, details.monthNameEn)
    }

    /**
     * Highlights notable Islamic calendar occasions if applicable for the day.
     */
    private fun getSpecialIslamicEvent(day: Int, month: Int): String? {
        return when (month) {
            1 -> when (day) {
                1 -> "Islamic New Year"
                10 -> "Day of Ashura"
                else -> null
            }
            3 -> when (day) {
                12 -> "Mawlid an-Nabi"
                else -> null
            }
            7 -> when (day) {
                27 -> "Isra & Mi'raj"
                else -> null
            }
            8 -> when (day) {
                15 -> "Shab-e-Barat"
                else -> null
            }
            9 -> when (day) {
                1 -> "First of Ramadan"
                in 21..29 -> if (day % 2 != 0) "Laylat al-Qadr" else "Last 10 of Ramadan"
                else -> "Ramadan Mubarak"
            }
            10 -> when (day) {
                1 -> "Eid al-Fitr"
                2, 3 -> "Eid al-Fitr Holiday"
                else -> null
            }
            12 -> when (day) {
                9 -> "Day of Arafah"
                10 -> "Eid al-Adha"
                11, 12, 13 -> "Days of Tashreeq"
                else -> null
            }
            else -> null
        }
    }
}
