package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.WatchMode
import com.example.ui.WatchViewModel
import com.example.ui.components.SettingsDialog
import com.example.ui.components.WatchBezel
import com.example.ui.components.WatchControlPanel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChakraPetchFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.ShareTechMonoFamily
import com.example.ui.theme.WatchGoldAccent
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: WatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AlFajrWatchApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AlFajrWatchApp(viewModel: WatchViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun fetchGpsLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val latStr = String.format("%.2f", loc.latitude)
                    val lngStr = String.format("%.2f", loc.longitude)
                    viewModel.setGpsLocation(loc.latitude, loc.longitude, "GPS $latStr°,$lngStr°")
                    scope.launch {
                        snackbarHostState.showSnackbar("GPS location updated: $latStr°, $lngStr°")
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Unable to get current GPS fix. Using preset city.")
                    }
                }
            }.addOnFailureListener {
                scope.launch {
                    snackbarHostState.showSnackbar("GPS lookup failed. Using preset city.")
                }
            }
        } catch (_: Exception) {}
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchGpsLocation()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Location permission denied. You can select your city manually.")
            }
        }
    }

    BackHandler(enabled = state.mode != WatchMode.TIME) {
        viewModel.onSelectMode(WatchMode.TIME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0A0F0D),
                            Color(0xFF111714),
                            Color(0xFF090D0B)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2822)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "الفجر",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WatchGoldAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "AL-FAJR",
                                fontFamily = ChakraPetchFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = WatchGoldAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "RETRO ISLAMIC PRAYER WATCH",
                                fontFamily = ChakraPetchFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7E9487),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // GPS auto-detect button
                        IconButton(
                            onClick = {
                                val hasCoarse = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCoarse) {
                                    fetchGpsLocation()
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                }
                            },
                            modifier = Modifier.testTag("btn_gps_detect")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "Detect GPS location",
                                tint = PrimaryGold
                            )
                        }

                        // Settings button
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("btn_top_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Open Settings",
                                tint = Color(0xFFA0B5A9)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // VINTAGE AL-FAJR WATCH (Bezel + Physical Buttons + LCD Screen)
                WatchBezel(
                    state = state,
                    onLightClick = { viewModel.onLightPressed() },
                    onModeClick = { viewModel.onModePressed() },
                    onSetClick = { viewModel.onSetPressed() },
                    onAlarmClick = { viewModel.toggleFajrAlarm() }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // INTERACTIVE CONTROL PANEL (Quick Actions & Mode Switcher)
                WatchControlPanel(
                    state = state,
                    onSelectMode = { viewModel.onSelectMode(it) },
                    onLightClick = { viewModel.onLightPressed() },
                    onToggleAlarm = { viewModel.toggleFajrAlarm() },
                    onTestAlarm = { viewModel.testAlarmToggle() },
                    onToggleMadhab = { viewModel.toggleMadhab() },
                    onOpenSettings = { showSettingsDialog = true },
                    onToggleSound = { viewModel.toggleSound() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Hint Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101713)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF6B8274),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tap the watch's side buttons (LIGHT, MODE, SET, ALARM) to experience genuine digital watch feedback, piezo beeps, and electro-luminescent glow.",
                            fontSize = 11.sp,
                            color = Color(0xFF8BA093),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                settings = state.settings,
                onDismiss = { showSettingsDialog = false },
                onCitySelected = { viewModel.selectCity(it) },
                onMethodSelected = { viewModel.setCalculationMethod(it) },
                onMadhabToggled = { viewModel.toggleMadhab() },
                onTimeFormatToggled = { viewModel.toggleTimeFormat() },
                onSoundToggled = { viewModel.toggleSound() },
                onFajrOffsetChanged = { viewModel.setFajrOffset(it) },
                onHijriOffsetAdjusted = { viewModel.adjustHijriOffset(it) }
            )
        }
    }
}
