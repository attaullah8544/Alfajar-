package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.Madhab
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.model.WatchMode
import com.example.model.WatchState
import com.example.ui.theme.ChakraPetchFamily
import com.example.ui.theme.LcdBacklight
import com.example.ui.theme.LcdBacklightGhost
import com.example.ui.theme.LcdBacklightText
import com.example.ui.theme.LcdGhostSegment
import com.example.ui.theme.LcdScreenGreen
import com.example.ui.theme.LcdScreenText
import com.example.ui.theme.ShareTechMonoFamily

@Composable
fun LcdScreen(
    state: WatchState,
    onModeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBacklight = state.isBacklightOn

    val bgColor by animateColorAsState(
        targetValue = if (isBacklight) LcdBacklight else LcdScreenGreen,
        animationSpec = tween(durationMillis = 150),
        label = "lcd_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isBacklight) LcdBacklightText else LcdScreenText,
        animationSpec = tween(durationMillis = 150),
        label = "lcd_text"
    )

    val ghostColor by animateColorAsState(
        targetValue = if (isBacklight) LcdBacklightGhost else LcdGhostSegment,
        animationSpec = tween(durationMillis = 150),
        label = "lcd_ghost"
    )

    // Inner bevel & bezel frame of LCD
    Box(
        modifier = modifier
            .testTag("lcd_screen_container")
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F1612)) // Dark recess
            .border(2.dp, Color(0xFF1E2B23), RoundedCornerShape(16.dp))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(1.dp, Color(0x33000000), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP STATUS & FLAGS ROW
            LcdTopStatusRow(state = state, textColor = textColor, ghostColor = ghostColor)

            // MAIN CONTENT DEPENDING ON MODE
            when (state.mode) {
                WatchMode.TIME -> {
                    LcdMainTimeContent(
                        state = state,
                        textColor = textColor,
                        ghostColor = ghostColor
                    )
                }
                WatchMode.PRAYERS -> {
                    LcdPrayersModeContent(
                        state = state,
                        textColor = textColor,
                        ghostColor = ghostColor
                    )
                }
                WatchMode.QIBLA -> {
                    LcdQiblaModeContent(
                        state = state,
                        textColor = textColor,
                        ghostColor = ghostColor
                    )
                }
                WatchMode.ALARM -> {
                    LcdAlarmModeContent(
                        state = state,
                        textColor = textColor,
                        ghostColor = ghostColor
                    )
                }
                WatchMode.SETTINGS -> {
                    LcdSettingsModeContent(
                        state = state,
                        textColor = textColor,
                        ghostColor = ghostColor
                    )
                }
            }

            // BOTTOM PRAYER ROW (Shown in TIME mode and general views)
            if (state.mode == WatchMode.TIME) {
                LcdBottomPrayerTable(
                    prayers = state.prayers,
                    is24Hour = state.settings.is24Hour,
                    textColor = textColor,
                    lcdBgColor = bgColor
                )
            } else {
                // In sub-modes, show a navigation helper footer
                LcdModeNavigationFooter(
                    mode = state.mode,
                    textColor = textColor,
                    ghostColor = ghostColor
                )
            }
        }
    }
}

@Composable
private fun LcdTopStatusRow(
    state: WatchState,
    textColor: Color,
    ghostColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Line 1: City / Location + Hijri Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.settings.locationLabel.take(12),
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textColor,
                letterSpacing = 0.5.sp
            )

            Text(
                text = state.hijriDate,
                fontFamily = ShareTechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Line 2: Day of Week + Gregorian Date + Watch Status Flags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Day of week
                Text(
                    text = state.dayOfWeek,
                    fontFamily = ChakraPetchFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = textColor,
                    modifier = Modifier
                        .background(textColor.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = state.gregorianDate,
                    fontFamily = ShareTechMonoFamily,
                    fontSize = 11.sp,
                    color = textColor
                )
            }

            // Active Watch Flags (Al-Fajr standard indicators)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LcdFlag(
                    text = "ALM",
                    isActive = state.settings.fajrAlarmEnabled,
                    textColor = textColor,
                    ghostColor = ghostColor
                )
                LcdFlag(
                    text = "PRAY",
                    isActive = state.settings.prayerAlertEnabled,
                    textColor = textColor,
                    ghostColor = ghostColor
                )
                LcdFlag(
                    text = if (state.settings.madhab == Madhab.HANAFI) "HNF" else "SHF",
                    isActive = true,
                    textColor = textColor,
                    ghostColor = ghostColor
                )
                LcdFlag(
                    text = if (state.settings.is24Hour) "24H" else "12H",
                    isActive = true,
                    textColor = textColor,
                    ghostColor = ghostColor
                )
                LcdFlag(
                    text = if (state.settings.soundEnabled) "BEEP" else "MUTE",
                    isActive = state.settings.soundEnabled,
                    textColor = textColor,
                    ghostColor = ghostColor
                )
            }
        }
    }
}

@Composable
private fun LcdFlag(
    text: String,
    isActive: Boolean,
    textColor: Color,
    ghostColor: Color
) {
    Text(
        text = text,
        fontFamily = ChakraPetchFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        color = if (isActive) textColor else ghostColor,
        modifier = if (isActive) {
            Modifier
                .background(textColor.copy(alpha = 0.12f), RoundedCornerShape(2.dp))
                .padding(horizontal = 2.dp, vertical = 0.5.dp)
        } else Modifier
    )
}

@Composable
private fun LcdMainTimeContent(
    state: WatchState,
    textColor: Color,
    ghostColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Center Time Clock
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Ghosted background segments for 7-segment digital realism
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "88:88",
                    fontFamily = ShareTechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 58.sp,
                    color = ghostColor,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "88",
                        fontFamily = ShareTechMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = ghostColor
                    )
                    Text(
                        text = "AM",
                        fontFamily = ChakraPetchFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ghostColor
                    )
                }
            }

            // Real sharp active LCD digits
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${state.hours}:${state.minutes}",
                    fontFamily = ShareTechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 58.sp,
                    color = textColor,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = state.seconds,
                        fontFamily = ShareTechMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = textColor
                    )
                    if (!state.settings.is24Hour) {
                        Text(
                            text = state.amPm,
                            fontFamily = ChakraPetchFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Middle Banner: Next Prayer countdown
        LcdNextPrayerBanner(state = state, textColor = textColor)
    }
}

@Composable
private fun LcdNextPrayerBanner(
    state: WatchState,
    textColor: Color
) {
    val next = state.nextPrayer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(textColor.copy(alpha = 0.08f))
            .border(1.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Next Prayer Name & Arabic
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NEXT: ",
                    fontFamily = ChakraPetchFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
                Text(
                    text = next?.type?.englishName ?: "PRAYER",
                    fontFamily = ChakraPetchFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = next?.type?.arabicName ?: "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Countdown timer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "IN ",
                    fontFamily = ChakraPetchFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
                Text(
                    text = state.countdownToNext,
                    fontFamily = ShareTechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun LcdBottomPrayerTable(
    prayers: List<PrayerTimeItem>,
    is24Hour: Boolean,
    textColor: Color,
    lcdBgColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(textColor.copy(alpha = 0.05f))
            .border(1.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            prayers.forEach { prayer ->
                val isNext = prayer.isNext
                val timeStr = if (is24Hour) {
                    prayer.formatted24h
                } else {
                    // Short 12h without AM/PM for compact LCD table
                    prayer.formatted12h.replace(" AM", "").replace(" PM", "")
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isNext) textColor else Color.Transparent)
                        .padding(vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = prayer.type.code,
                        fontFamily = ChakraPetchFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (isNext) lcdBgColor else textColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeStr,
                        fontFamily = ShareTechMonoFamily,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp,
                        color = if (isNext) lcdBgColor else textColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun LcdPrayersModeContent(
    state: WatchState,
    textColor: Color,
    ghostColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRAYER SCHEDULE",
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textColor
            )
            Text(
                text = if (state.settings.madhab == Madhab.HANAFI) "MADHAB: HANAFI" else "MADHAB: SHAFI'I",
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 6 prayer items with Arabic names & exact times
        state.prayers.forEach { item ->
            val isNext = item.isNext
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isNext) textColor.copy(alpha = 0.15f) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isNext) "► " else "  ",
                        fontFamily = ShareTechMonoFamily,
                        fontSize = 12.sp,
                        color = textColor
                    )
                    Text(
                        text = item.type.englishName.padEnd(8),
                        fontFamily = ChakraPetchFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textColor
                    )
                    Text(
                        text = item.type.arabicName,
                        fontSize = 13.sp,
                        color = textColor
                    )
                }

                Text(
                    text = if (state.settings.is24Hour) item.formatted24h else item.formatted12h,
                    fontFamily = ShareTechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun LcdQiblaModeContent(
    state: WatchState,
    textColor: Color,
    ghostColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QIBLA DIRECTION",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Large Bearing Display
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = String.format("%.1f", state.qiblaDirection),
                fontFamily = ShareTechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = textColor
            )
            Text(
                text = "° N",
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = textColor,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Distance to Kaaba
        Text(
            text = "DISTANCE TO MAKKAH: ${state.kaabaDistanceKm} KM",
            fontFamily = ShareTechMonoFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Compass orientation indicator
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(6.dp))
                .background(textColor.copy(alpha = 0.12f))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "◄ KAABA BEARING ${String.format("%03.0f°", state.qiblaDirection)} ►",
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}

@Composable
private fun LcdAlarmModeContent(
    state: WatchState,
    textColor: Color,
    ghostColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FAJR RELATIVE ALARM",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Alarm Status
        Text(
            text = if (state.settings.fajrAlarmEnabled) "STATUS: ACTIVE" else "STATUS: OFF",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = if (state.settings.fajrAlarmEnabled) textColor else textColor.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Alarm Offset
        Text(
            text = if (state.settings.fajrAlarmOffsetMinutes == 0)
                "RINGS AT EXACT FAJR"
            else
                "RINGS ${state.settings.fajrAlarmOffsetMinutes} MIN BEFORE FAJR",
            fontFamily = ShareTechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Next Alarm Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(textColor.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "ALARM AT: ",
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textColor
            )
            Text(
                text = state.fajrAlarmTimeFormatted,
                fontFamily = ShareTechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
        }

        if (state.isAlarmRinging) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "⚡ ALARM RINGING ⚡",
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = textColor
            )
        }
    }
}

@Composable
private fun LcdSettingsModeContent(
    state: WatchState,
    textColor: Color,
    ghostColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "WATCH CONFIGURATION",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        val items = listOf(
            "CITY" to state.settings.selectedCity.name.uppercase(),
            "CALC METHOD" to state.settings.calculationMethod.name,
            "ASR MADHAB" to if (state.settings.madhab == Madhab.HANAFI) "HANAFI" else "SHAFI'I",
            "TIME FORMAT" to if (state.settings.is24Hour) "24-HOUR" else "12-HOUR",
            "HIJRI ADJUST" to "${if (state.settings.hijriOffsetDays >= 0) "+" else ""}${state.settings.hijriOffsetDays} DAYS",
            "BEEP SOUND" to if (state.settings.soundEnabled) "ON" else "MUTED"
        )

        items.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontFamily = ChakraPetchFamily,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
                Text(
                    text = value,
                    fontFamily = ShareTechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun LcdModeNavigationFooter(
    mode: WatchMode,
    textColor: Color,
    ghostColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(textColor.copy(alpha = 0.1f))
            .padding(vertical = 4.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "[MODE: ${mode.title}]  PRESS MODE TO CYCLE",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
