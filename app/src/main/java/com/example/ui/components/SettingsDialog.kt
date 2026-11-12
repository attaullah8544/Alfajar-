package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.example.model.CalculationMethods
import com.example.model.CityPreset
import com.example.model.LocationDefaultsHelper
import com.example.model.PresetCities
import com.example.model.WatchSettings
import com.example.network.NominatimResult
import com.example.ui.theme.ChakraPetchFamily
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.ShareTechMonoFamily
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.WatchGoldAccent

@Composable
fun SettingsDialog(
    settings: WatchSettings,
    searchQuery: String,
    searchResults: List<NominatimResult>,
    isSearching: Boolean,
    searchError: String?,
    onSearchQueryChanged: (String) -> Unit,
    onOnlineLocationSelected: (NominatimResult) -> Unit,
    onCitySelected: (CityPreset) -> Unit,
    onMethodSelected: (CalculationMethod) -> Unit,
    onMadhabToggled: () -> Unit,
    onTimeFormatToggled: () -> Unit,
    onSoundToggled: () -> Unit,
    onFajrOffsetChanged: (Int) -> Unit,
    onHijriOffsetAdjusted: (Int) -> Unit,
    onWidgetTransparencyChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ONLINE SEARCH", "PRESET CITIES", "METHOD & MADHAB", "WIDGETS", "ALARM & HIJRI")

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AL-FAJR SETTINGS",
                                fontFamily = ChakraPetchFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = WatchGoldAccent
                            )
                        }
                        Text(
                            text = "Auto-configure watch & widgets by location",
                            fontSize = 11.sp,
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

                Spacer(modifier = Modifier.height(10.dp))

                // Active Location Banner
                CurrentActiveLocationBanner(settings = settings)

                Spacer(modifier = Modifier.height(10.dp))

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
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> OnlineLocationSearchTab(
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        isSearching = isSearching,
                        searchError = searchError,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onLocationSelected = {
                            onOnlineLocationSelected(it)
                            onDismiss()
                        }
                    )
                    1 -> PresetCitiesTab(
                        currentCity = settings.selectedCity,
                        onCitySelected = {
                            onCitySelected(it)
                            onDismiss()
                        }
                    )
                    2 -> CalculationMethodTab(
                        currentMethod = settings.calculationMethod,
                        isHanafi = settings.madhab == Madhab.HANAFI,
                        is24H = settings.is24Hour,
                        onMethodSelected = onMethodSelected,
                        onMadhabToggled = onMadhabToggled,
                        onTimeFormatToggled = onTimeFormatToggled
                    )
                    3 -> HomeWidgetsTab(
                        settings = settings,
                        onWidgetTransparencyChanged = onWidgetTransparencyChanged
                    )
                    4 -> AlarmAndHijriTab(
                        settings = settings,
                        onFajrOffsetChanged = onFajrOffsetChanged,
                        onHijriOffsetAdjusted = onHijriOffsetAdjusted,
                        onSoundToggled = onSoundToggled
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
private fun CurrentActiveLocationBanner(settings: WatchSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2620)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PrimaryGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CURRENT: ${settings.locationLabel}",
                        fontFamily = ChakraPetchFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${settings.country ?: ""} • ${settings.calculationMethod.name}",
                        fontSize = 10.sp,
                        color = Color(0xFFA0B5A9),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = if (settings.madhab == Madhab.HANAFI) "HANAFI" else "SHAFI'I",
                fontFamily = ChakraPetchFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = PrimaryGold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF26372E))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun OnlineLocationSearchTab(
    searchQuery: String,
    searchResults: List<NominatimResult>,
    isSearching: Boolean,
    searchError: String?,
    onSearchQueryChanged: (String) -> Unit,
    onLocationSelected: (NominatimResult) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SEARCH ANY WORLDWIDE CITY / ADDRESS ONLINE:",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFFA0B3A8)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("online_location_search_input"),
            placeholder = {
                Text(
                    text = "e.g. Lahore, Medina, Toronto, London...",
                    fontSize = 13.sp,
                    color = Color(0xFF6B8074)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PrimaryGold
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryGold
                    )
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color(0xFFA0B3A8)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGold,
                unfocusedBorderColor = Color(0xFF2E3E34),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = PrimaryGold
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Explanation pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF16211B))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PrimaryGold,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Auto-configures calculation method, Asr madhab, Qibla & coordinates.",
                fontSize = 10.sp,
                color = Color(0xFF98ADA0)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (searchError != null && searchResults.isEmpty() && !isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = searchError,
                    fontSize = 13.sp,
                    color = Color(0xFFE57373),
                    fontFamily = ChakraPetchFamily
                )
            }
        } else if (searchResults.isEmpty() && searchQuery.trim().length >= 2 && !isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching locations found online.",
                    fontSize = 13.sp,
                    color = Color(0xFF8CA094),
                    fontFamily = ChakraPetchFamily
                )
            }
        } else if (searchResults.isEmpty()) {
            // Suggestion chips when search is empty
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Text(
                    text = "POPULAR ONLINE SEARCHES:",
                    fontFamily = ChakraPetchFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7E9487)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val quickCities = listOf(
                    "Makkah, Saudi Arabia",
                    "Karachi, Pakistan",
                    "Cairo, Egypt",
                    "Istanbul, Turkey",
                    "London, United Kingdom",
                    "Kuala Lumpur, Malaysia",
                    "New York, USA",
                    "Dubai, UAE"
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickCities) { cityStr ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSearchQueryChanged(cityStr) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A241F)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cityStr,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE0EAE3),
                                    fontFamily = ChakraPetchFamily
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(searchResults) { result ->
                    val cityName = result.address?.bestCityName
                        ?: result.name
                        ?: result.displayName.split(",").firstOrNull()?.trim()
                        ?: "LOCATION"

                    val countryName = result.address?.country ?: ""
                    val method = LocationDefaultsHelper.recommendCalculationMethod(
                        countryCode = result.address?.countryCode,
                        countryName = countryName
                    )
                    val madhab = LocationDefaultsHelper.recommendMadhab(
                        countryCode = result.address?.countryCode,
                        countryName = countryName
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLocationSelected(result) }
                            .testTag("online_loc_${result.placeId ?: result.lat}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A23)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = PrimaryGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cityName,
                                        fontFamily = ChakraPetchFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    if (countryName.isNotEmpty()) {
                                        Text(
                                            text = ", $countryName",
                                            fontSize = 12.sp,
                                            color = Color(0xFF90A397),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = if (madhab == Madhab.HANAFI) "HNF" else "SHF",
                                    fontFamily = ChakraPetchFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = PrimaryGold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF283930))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = result.displayName,
                                fontSize = 10.sp,
                                color = Color(0xFF7A8E82),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Auto-method: ${method.name}",
                                    fontSize = 10.sp,
                                    color = PrimaryGold.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${String.format("%.2f", result.lat.toDoubleOrNull() ?: 0.0)}°, ${String.format("%.2f", result.lon.toDoubleOrNull() ?: 0.0)}°",
                                    fontFamily = ShareTechMonoFamily,
                                    fontSize = 10.sp,
                                    color = Color(0xFF8CA094)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetCitiesTab(
    currentCity: CityPreset,
    onCitySelected: (CityPreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SELECT FROM PRESET CITIES (OFFLINE PRAYER TIMES):",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
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
                                    text = "${city.country} • ${city.defaultMethod.name}",
                                    fontSize = 11.sp,
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

@Composable
fun HomeWidgetsTab(
    settings: WatchSettings,
    onWidgetTransparencyChanged: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pinMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    val previewBgAlpha = when (settings.widgetTransparencyPercent) {
        0 -> 0.95f
        25 -> 0.72f
        50 -> 0.45f
        75 -> 0.22f
        100 -> 0.04f
        else -> 0.95f
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "AL-FAJR RETRO HOME SCREEN WIDGETS",
            fontFamily = ChakraPetchFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = PrimaryGold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Bring the classic Al-Fajr green LCD prayer watch face directly onto your Android Home Screen.",
            fontSize = 11.sp,
            color = Color(0xFFA0B3A8)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Widget Background Transparency Control Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("widget_transparency_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16231C)),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF344A3E))
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WIDGET TRANSPARENCY",
                            fontFamily = ChakraPetchFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PrimaryGold
                        )
                        Text(
                            text = "Blend watch face with your wallpaper",
                            fontSize = 10.sp,
                            color = Color(0xFFA0B3A8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF23352B))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when (settings.widgetTransparencyPercent) {
                                0 -> "0% (SOLID)"
                                25 -> "25% (FROST)"
                                50 -> "50% (MEDIUM)"
                                75 -> "75% (CLEAR)"
                                100 -> "100% (PURE GLASS)"
                                else -> "${settings.widgetTransparencyPercent}%"
                            },
                            fontFamily = ChakraPetchFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = PrimaryGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5 Transparency Level Buttons
                val levels = listOf(
                    0 to "0% Solid",
                    25 to "25% Frost",
                    50 to "50% Med",
                    75 to "75% Clear",
                    100 to "100% Glass"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    levels.forEach { (pct, label) ->
                        val isSel = settings.widgetTransparencyPercent == pct
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PrimaryGold else Color(0xFF1D2A22))
                                .border(
                                    1.dp,
                                    if (isSel) PrimaryGold else Color(0xFF34453A),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onWidgetTransparencyChanged(pct) }
                                .testTag("btn_transparency_$pct")
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = ChakraPetchFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isSel) Color(0xFF0C1410) else Color(0xFFC5D9CE),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "★ Applies instantly to all active home screen widgets.",
                    fontSize = 9.sp,
                    color = Color(0xFF8CA597)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Widget 1: 4x2 Full Watch Face
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2620)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "4x2 Full Watch Face Widget",
                            fontFamily = ChakraPetchFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Complete LCD screen with 6 prayer times, clock, date, Hijri & next prayer countdown.",
                            fontSize = 10.sp,
                            color = Color(0xFF8CA094)
                        )
                    }

                    Button(
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                                val provider = android.content.ComponentName(context, com.example.widget.AlFajrWidgetProvider::class.java)
                                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                    appWidgetManager.requestPinAppWidget(provider, null, null)
                                    pinMessage = "Widget pin requested! Confirm on your home screen launcher."
                                } else {
                                    pinMessage = "Long press empty space on your home screen and select 'Al-Fajr Prayer Watch'."
                                }
                            } else {
                                pinMessage = "Long press empty space on your home screen and select 'Al-Fajr Prayer Watch'."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_pin_full_widget")
                    ) {
                        Text("ADD", fontFamily = ChakraPetchFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Miniature LCD Preview with Live Transparency
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    Color(0xFF233930).copy(alpha = previewBgAlpha),
                                    Color(0xFF15261E).copy(alpha = previewBgAlpha),
                                    Color(0xFF0C1612).copy(alpha = previewBgAlpha)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            if (settings.widgetTransparencyPercent == 100) Color(0x6684A996) else Color(0xFF4C7560),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(8.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(settings.locationLabel.take(12), fontFamily = ChakraPetchFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFE4F0E8))
                            Text("08 RABI' II 1448 AH", fontFamily = ShareTechMonoFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFFE4F0E8))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text("12:00", fontFamily = ShareTechMonoFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFE8F5EC))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FAJR 05:12", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAC2B4))
                            Text("ZUHR 12:15", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAC2B4))
                            Text("ASR 03:45", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF2C979))
                            Text("MAGH 06:02", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAC2B4))
                            Text("ISHA 07:22", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAC2B4))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Widget 2: 2x1 Compact Next Prayer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2620)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2x1 Next Prayer Widget",
                            fontFamily = ChakraPetchFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Compact banner highlighting upcoming prayer and live countdown.",
                            fontSize = 10.sp,
                            color = Color(0xFF8CA094)
                        )
                    }

                    Button(
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                                val provider = android.content.ComponentName(context, com.example.widget.AlFajrCompactWidgetProvider::class.java)
                                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                    appWidgetManager.requestPinAppWidget(provider, null, null)
                                    pinMessage = "Widget pin requested! Confirm on your home screen launcher."
                                } else {
                                    pinMessage = "Long press empty space on your home screen and select 'Al-Fajr Next Prayer'."
                                }
                            } else {
                                pinMessage = "Long press empty space on your home screen and select 'Al-Fajr Next Prayer'."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_pin_compact_widget")
                    ) {
                        Text("ADD", fontFamily = ChakraPetchFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Miniature Compact Preview with Live Transparency
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    Color(0xFF233930).copy(alpha = previewBgAlpha),
                                    Color(0xFF15261E).copy(alpha = previewBgAlpha),
                                    Color(0xFF0C1612).copy(alpha = previewBgAlpha)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            if (settings.widgetTransparencyPercent == 100) Color(0x6684A996) else Color(0xFF4C7560),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(settings.locationLabel.take(12), fontFamily = ChakraPetchFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFE4F0E8))
                            Text("NEXT: ASR (03:45 PM)", fontFamily = ChakraPetchFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFF2C979))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0C1410).copy(alpha = if (settings.widgetTransparencyPercent == 100) 0.15f else 0.85f))
                                .border(1.dp, Color(0xFF3B5647), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("01:24:00", fontFamily = ShareTechMonoFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFF2C979))
                        }
                    }
                }
            }
        }

        if (pinMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pinMessage ?: "",
                fontSize = 11.sp,
                color = PrimaryGold,
                fontFamily = ChakraPetchFamily
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Manual instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131A16)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "HOW TO ADD WIDGETS ON ANDROID:",
                    fontFamily = ChakraPetchFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Go to your phone's Home Screen\n2. Long press any empty space\n3. Tap 'Widgets' and scroll to 'Al-Fajr Watch'\n4. Drag either widget to your screen!",
                    fontSize = 10.sp,
                    color = Color(0xFF8CA094),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

