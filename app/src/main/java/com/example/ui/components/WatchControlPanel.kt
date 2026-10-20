package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.Madhab
import com.example.model.WatchMode
import com.example.model.WatchState
import com.example.ui.theme.ChakraPetchFamily
import com.example.ui.theme.LcdBacklight
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.ShareTechMonoFamily
import com.example.ui.theme.WatchGoldAccent
import com.example.ui.theme.WatchRedAccent

@Composable
fun WatchControlPanel(
    state: WatchState,
    onSelectMode: (WatchMode) -> Unit,
    onLightClick: () -> Unit,
    onToggleAlarm: () -> Unit,
    onTestAlarm: () -> Unit,
    onToggleMadhab: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("watch_control_panel"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141B17)),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF26352C)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Mode Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F1512))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WatchModeTab(
                    title = "WATCH",
                    isSelected = state.mode == WatchMode.TIME,
                    onClick = { onSelectMode(WatchMode.TIME) },
                    modifier = Modifier.weight(1f)
                )
                WatchModeTab(
                    title = "PRAYERS",
                    isSelected = state.mode == WatchMode.PRAYERS,
                    onClick = { onSelectMode(WatchMode.PRAYERS) },
                    modifier = Modifier.weight(1f)
                )
                WatchModeTab(
                    title = "QIBLA",
                    isSelected = state.mode == WatchMode.QIBLA,
                    onClick = { onSelectMode(WatchMode.QIBLA) },
                    modifier = Modifier.weight(1f)
                )
                WatchModeTab(
                    title = "ALARM",
                    isSelected = state.mode == WatchMode.ALARM,
                    onClick = { onSelectMode(WatchMode.ALARM) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Hardware / Feature Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Backlight button
                QuickActionButton(
                    icon = Icons.Default.Lightbulb,
                    label = if (state.isBacklightOn) "EL LIGHT ON" else "EL LIGHT",
                    isActive = state.isBacklightOn,
                    activeColor = LcdBacklight,
                    onClick = onLightClick,
                    testTag = "btn_light_toggle",
                    modifier = Modifier.weight(1f)
                )

                // Fajr Alarm Toggle
                QuickActionButton(
                    icon = Icons.Default.Alarm,
                    label = if (state.settings.fajrAlarmEnabled) "ALM: ${state.settings.fajrAlarmOffsetMinutes}m" else "ALM: OFF",
                    isActive = state.settings.fajrAlarmEnabled,
                    activeColor = WatchGoldAccent,
                    onClick = onToggleAlarm,
                    testTag = "btn_fajr_alarm_toggle",
                    modifier = Modifier.weight(1f)
                )

                // Madhab switch
                QuickActionButton(
                    icon = Icons.Default.Schedule,
                    label = if (state.settings.madhab == Madhab.HANAFI) "HANAFI" else "SHAFI'I",
                    isActive = true,
                    activeColor = Color(0xFF88C999),
                    onClick = onToggleMadhab,
                    testTag = "btn_madhab_toggle",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Test Alarm button, City / Settings & Mute
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Alarm button (Starts/stops piezo alarm tone)
                Button(
                    onClick = onTestAlarm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isAlarmRinging) WatchRedAccent else Color(0xFF28362D),
                        contentColor = if (state.isAlarmRinging) Color.White else PrimaryGold
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_test_alarm")
                ) {
                    Text(
                        text = if (state.isAlarmRinging) "■ STOP ALARM" else "▶ TEST ALARM",
                        fontFamily = ChakraPetchFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // City / Settings button
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF222C26),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("btn_open_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = WatchGoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SETTINGS",
                        fontFamily = ChakraPetchFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Sound Mute Toggle
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E2822))
                        .testTag("btn_toggle_sound")
                ) {
                    Icon(
                        imageVector = if (state.settings.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = if (state.settings.soundEnabled) "Mute sound" else "Unmute sound",
                        tint = if (state.settings.soundEnabled) PrimaryGold else Color(0xFFE57373)
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchModeTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (isSelected) PrimaryGold else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (isSelected) Color(0xFF0F1512) else Color(0xFF8CA094)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.15f) else Color(0xFF1B231F))
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.4f) else Color(0xFF2B3830),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else Color(0xFF758A7E),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (isActive) activeColor else Color(0xFFB0C2B7)
            )
        }
    }
}
