package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WatchState
import com.example.ui.theme.ChakraPetchFamily
import com.example.ui.theme.WatchButtonSteel
import com.example.ui.theme.WatchCaseDark
import com.example.ui.theme.WatchCaseSurface
import com.example.ui.theme.WatchGoldAccent
import com.example.ui.theme.WatchGoldMuted

@Composable
fun WatchBezel(
    state: WatchState,
    onLightClick: () -> Unit,
    onModeClick: () -> Unit,
    onSetClick: () -> Unit,
    onAlarmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP WATCH STRAP SEGMENT
        WatchStrapTop()

        // MAIN WATCH BODY & SIDE BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // LEFT BUTTONS (LIGHT & MODE)
            Column(
                verticalArrangement = Arrangement.spacedBy(90.dp),
                horizontalAlignment = Alignment.End
            ) {
                WatchSideButton(
                    label = "LIGHT",
                    testTag = "watch_button_light",
                    onClick = onLightClick,
                    isLeft = true
                )
                WatchSideButton(
                    label = "MODE",
                    testTag = "watch_button_mode",
                    onClick = onModeClick,
                    isLeft = true
                )
            }

            // WATCH CASE HOUSING THE LCD
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .height(390.dp)
                    .shadow(16.dp, RoundedCornerShape(36.dp))
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF26322C),
                                WatchCaseSurface,
                                WatchCaseDark,
                                Color(0xFF141A17)
                            )
                        )
                    )
                    .border(3.dp, Color(0xFF3B4D43), RoundedCornerShape(36.dp))
                    .border(6.dp, Color(0xFF1B231F), RoundedCornerShape(36.dp))
                    .padding(12.dp)
            ) {
                // Bezel Screws Canvas
                BezelDecorations()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TOP BEZEL INSIGNIA
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIGHT",
                            fontFamily = ChakraPetchFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7E9487)
                        )

                        // Iconic AL-FAJR Golden Branding
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AL-FAJR",
                                    fontFamily = ChakraPetchFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = WatchGoldAccent,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "الفجر",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = WatchGoldAccent
                                )
                            }
                            Text(
                                text = "ISLAMIC PRAYER WATCH",
                                fontFamily = ChakraPetchFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = WatchGoldMuted,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "SET",
                            fontFamily = ChakraPetchFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7E9487)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // THE LCD SCREEN
                    LcdScreen(
                        state = state,
                        onModeClick = onModeClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // BOTTOM BEZEL INSIGNIA
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MODE",
                            fontFamily = ChakraPetchFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7E9487)
                        )

                        Text(
                            text = "WATER RESIST 50M • AUTO QIBLA",
                            fontFamily = ChakraPetchFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7E74),
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "ALARM",
                            fontFamily = ChakraPetchFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7E9487)
                        )
                    }
                }
            }

            // RIGHT BUTTONS (SET & ALARM)
            Column(
                verticalArrangement = Arrangement.spacedBy(90.dp),
                horizontalAlignment = Alignment.Start
            ) {
                WatchSideButton(
                    label = "SET",
                    testTag = "watch_button_set",
                    onClick = onSetClick,
                    isLeft = false
                )
                WatchSideButton(
                    label = "ALARM",
                    testTag = "watch_button_alarm",
                    onClick = onAlarmClick,
                    isLeft = false
                )
            }
        }

        // BOTTOM WATCH STRAP SEGMENT
        WatchStrapBottom()
    }
}

@Composable
private fun WatchSideButton(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    isLeft: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.88f else 1.0f, label = "btn_press")

    Column(
        horizontalAlignment = if (isLeft) Alignment.End else Alignment.Start,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .testTag(testTag)
                .scale(scale)
                .size(width = 18.dp, height = 36.dp)
                .clip(
                    if (isLeft) RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    else RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            WatchButtonSteel,
                            Color(0xFF53635B),
                            Color(0xFF28322D)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFF6B7E74),
                    if (isLeft) RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    else RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

@Composable
private fun BezelDecorations() {
    Canvas(modifier = Modifier.fillMaxWidth().height(360.dp)) {
        val screwColor = Color(0xFF4C5E53)
        val screwHighlight = Color(0xFF7B9183)

        // 4 corner watch screw accents
        val corners = listOf(
            Offset(20.dp.toPx(), 20.dp.toPx()),
            Offset(size.width - 20.dp.toPx(), 20.dp.toPx()),
            Offset(20.dp.toPx(), size.height - 20.dp.toPx()),
            Offset(size.width - 20.dp.toPx(), size.height - 20.dp.toPx())
        )

        corners.forEach { pos ->
            drawCircle(color = screwColor, radius = 5.dp.toPx(), center = pos)
            drawCircle(color = screwHighlight, radius = 2.dp.toPx(), center = pos)
        }
    }
}

@Composable
private fun WatchStrapTop() {
    Column(
        modifier = Modifier
            .width(190.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF141916),
                        Color(0xFF1E2521),
                        Color(0xFF161C18)
                    )
                )
            )
            .border(1.dp, Color(0xFF2E3832), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Resin ribbed ridges on watch strap
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(2.dp)
                    .background(Color(0xFF0C100E))
            )
        }
    }
}

@Composable
private fun WatchStrapBottom() {
    Column(
        modifier = Modifier
            .width(190.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF161C18),
                        Color(0xFF1E2521),
                        Color(0xFF141916)
                    )
                )
            )
            .border(1.dp, Color(0xFF2E3832), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(2.dp)
                    .background(Color(0xFF0C100E))
            )
        }
    }
}
