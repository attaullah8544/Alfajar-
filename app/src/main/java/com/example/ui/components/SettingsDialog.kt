package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.example.model.CalculationMethods
import com.example.model.CityPreset
import com.example.model.PresetCities
import com.example.model.WatchSettings
import com.example.ui.theme.ChakraPetchFamily
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.ShareTechMonoFamily
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.WatchGoldAccent

@Composable
fun SettingsDialog(
    settings: WatchSettings,
    onDismiss: () -> Unit,
    onCitySelected: (CityPreset) -> Unit,
    onMethodSelected: (CalculationMethod) -> Unit,
    onMadhabToggled: () -> Unit,
    onTimeFormatToggled: () -> Unit,
    onSoundToggled: () -> Unit,
    onFajrOffsetChanged: (Int) -> Unit,
    onHijriOffsetAdjusted: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("CITY / LOCATION", "METHOD & MADHAB", "ALARM & HIJRI")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("settings_dialog"),
            color = SurfaceDark,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AL-FAJR SETTINGS",
                            fontFamily = ChakraPetchFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = WatchGoldAccent
                        )
                        Text(
                            text = "Watch configuration & calculations",
                            fontSize = 12.sp,
                            color = Color(0xFFA0B3A8)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceContainerDark,
                    contentColor = PrimaryGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryGold
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontFamily = ChakraPetchFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> CitySelectorTab(
                        currentCity = settings.selectedCity,
                        onCitySelected = {
                            onCitySelected(it)
                            onDismiss()
                        }
                    )
                    1 -> CalculationMethodTab(
                        currentMethod = settings.calculationMethod,
                        isHanafi = settings.madhab == Madhab.HANAFI,
                        is24H = settings.is24Hour,
                        onMethodSelected = onMethodSelected,
                        onMadhabToggled = onMadhabToggled,
                        onTimeFormatToggled = onTimeFormatToggled
                    )
                    2 -> AlarmAndHijriTab(
                        settings = settings,
                        onFajrOffsetChanged = onFajrOffsetChanged,
                        onHijriOffsetAdjusted = onHijriOffsetAdjusted,
                        onSoundToggled = onSoundToggled
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_done_settings"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "DONE",
                        fontFamily = ChakraPetchFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CitySelectorTab(
    currentCity: CityPreset,
    onCitySelected: (CityPreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SELECT CITY (OFFLINE PRAYER TIMES):",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFA0B3A8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(PresetCities.list) { city ->
                val isSelected = city.id == currentCity.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCitySelected(city) }
                        .testTag("city_${city.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF283A30) else Color(0xFF1B231F)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) PrimaryGold else Color(0xFF708579),
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column {
                                Text(
                                    text = city.name,
                                    fontFamily = ChakraPetchFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSelected) PrimaryGold else Color.White
                                )
                                Text(
                                    text = city.country,
                                    fontSize = 12.sp,
                                    color = Color(0xFF90A397)
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = PrimaryGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationMethodTab(
    currentMethod: CalculationMethod,
    isHanafi: Boolean,
    is24H: Boolean,
    onMethodSelected: (CalculationMethod) -> Unit,
    onMadhabToggled: () -> Unit,
    onTimeFormatToggled: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Madhab & 24H Quick Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onMadhabToggled() }
                    .testTag("toggle_madhab"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2B24)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "ASR MADHAB",
                        fontFamily = ChakraPetchFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA0B3A8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHanafi) "HANAFI (Later Asr)" else "SHAFI'I (Standard)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryGold
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTimeFormatToggled() }
                    .testTag("toggle_24h"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2B24)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "TIME FORMAT",
                        fontFamily = ChakraPetchFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA0B3A8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (is24H) "24-HOUR CLOCK" else "12-HOUR CLOCK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "CALCULATION METHOD:",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFA0B3A8)
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(CalculationMethods.list) { info ->
                val isSelected = info.method == currentMethod
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF283A30) else Color(0xFF1B231F))
                        .clickable { onMethodSelected(info.method) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.name,
                            fontFamily = ChakraPetchFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) PrimaryGold else Color.White
                        )
                        Text(
                            text = info.description,
                            fontSize = 10.sp,
                            color = Color(0xFF8CA094),
                            maxLines = 1
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onMethodSelected(info.method) },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmAndHijriTab(
    settings: WatchSettings,
    onFajrOffsetChanged: (Int) -> Unit,
    onHijriOffsetAdjusted: (Int) -> Unit,
    onSoundToggled: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "FAJR RELATIVE ALARM OFFSET:",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFA0B3A8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        val offsets = listOf(0, 10, 15, 20, 30)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            offsets.forEach { offset ->
                val isSel = settings.fajrAlarmOffsetMinutes == offset
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) PrimaryGold else Color(0xFF1B231F))
                        .clickable { onFajrOffsetChanged(offset) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (offset == 0) "FAJR" else "-${offset}m",
                        fontFamily = ShareTechMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSel) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hijri date adjustment (-2 to +2 days)
        Text(
            text = "HIJRI DATE ADJUSTMENT (MOON SIGHTING):",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFA0B3A8)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1B231F))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onHijriOffsetAdjusted(-1) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("- 1 DAY", fontFamily = ChakraPetchFamily, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "${if (settings.hijriOffsetDays >= 0) "+" else ""}${settings.hijriOffsetDays} DAYS",
                fontFamily = ShareTechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PrimaryGold
            )

            OutlinedButton(
                onClick = { onHijriOffsetAdjusted(+1) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ 1 DAY", fontFamily = ChakraPetchFamily, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Watch Beep Sound
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1B231F))
                .clickable { onSoundToggled() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DIGITAL WATCH SOUNDS",
                    fontFamily = ChakraPetchFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = "Piezo button beeps and alarm sounds",
                    fontSize = 11.sp,
                    color = Color(0xFF8CA094)
                )
            }

            Text(
                text = if (settings.soundEnabled) "ENABLED" else "MUTED",
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (settings.soundEnabled) PrimaryGold else Color(0xFFE57373)
            )
        }
    }
}
