package com.forgecrew.drillschool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The two operator displays now have genuinely separate compositions.
 * Both are original training illustrations inspired by field photography;
 * neither reproduces the manufacturer's original firmware.
 */
@Composable
fun GuidanceDisplayPanel(
    session: SimState,
    style: GuidanceStyle,
    onStyleChange: (GuidanceStyle) -> Unit,
    threeD: Boolean,
    onViewChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var pickDisplay by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier,
        color = Color(0xFF2B3943),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.fillMaxSize().padding(5.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth().height(32.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    Surface(
                        onClick = { pickDisplay = true },
                        shape = RoundedCornerShape(9.dp),
                        color = Color(0xFF43535E)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (style == GuidanceStyle.DIGITRAK) "DigiTrak-style ▾"
                                else "Subsite-style ▾",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = pickDisplay,
                        onDismissRequest = { pickDisplay = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("DigiTrak-style display") },
                            onClick = {
                                pickDisplay = false
                                onStyleChange(GuidanceStyle.DIGITRAK)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Subsite-style display") },
                            onClick = {
                                pickDisplay = false
                                onStyleChange(GuidanceStyle.SUBSITE)
                            }
                        )
                    }
                }
                listOf(false, true).forEach { mode ->
                    Surface(
                        onClick = { onViewChange(mode) },
                        color = if (threeD == mode) OrangeDark else Color(0xFF53636C),
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Text(
                            if (mode) "3D" else "2D",
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (style == GuidanceStyle.DIGITRAK) {
                DigiTrakTrainingDisplay(
                    session = session,
                    threeD = threeD,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            } else {
                SubsiteTrainingDisplay(
                    session = session,
                    threeD = threeD,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }

            Text(
                "TRAINING VALUES ONLY • NOT CONNECTED TO EQUIPMENT",
                modifier = Modifier.fillMaxWidth().padding(start = 5.dp, bottom = 1.dp),
                fontSize = 8.sp,
                color = Color(0xFFFFCE99),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

internal fun signedTrainingFeet(value: Double): String =
    (if (value > 0.001) "+" else "") + oneDecimal(value) + " ft"

internal fun signedTrainingPercent(value: Double): String =
    (if (value > 0.001) "+" else "") + oneDecimal(value) + "%"

@Composable
internal fun SlimGuidanceMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 9.sp, color = SchoolMuted,
            fontWeight = FontWeight.Bold, maxLines = 1)
        Text(
            value, fontSize = if (emphasized) 13.sp else 11.sp,
            color = DeepSteel, fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}
