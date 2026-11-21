package com.forgecrew.drillschool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** These are our training layouts; they are not copies of OEM firmware. */
enum class GuidanceStyle(val title: String, val description: String) {
    DIGITRAK("DigiTrak-inspired", "Compact clock, pitch and depth view"),
    SUBSITE("Subsite-inspired", "High-contrast guidance and tracking")
}

enum class PracticeMode(val label: String) {
    FREE("Free Practice"), GUIDED("Guided Mission")
}

@Composable
fun SimulatorSetupPage(
    session: SimState,
    rig: RigProfile,
    displayStyle: GuidanceStyle,
    practiceMode: PracticeMode,
    onStyle: (GuidanceStyle) -> Unit,
    onMode: (PracticeMode) -> Unit,
    onMission: (Int) -> Unit,
    onRigSettings: () -> Unit,
    onLaunch: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Eyebrow("DRILLSCHOOL • SIMULATOR SETUP")
            Text("Set up your operator seat", color = DeepSteel,
                fontSize = 24.sp, fontWeight = FontWeight.Black)
            SubText("Choose the mission, training mode and guidance display before starting.")
        }
        item {
            SchoolCard {
                Eyebrow("TRAINING MISSION")
                trainingMissions.forEachIndexed { index, mission ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = session.mission.id == mission.id,
                            onClick = { onMission(index) }
                        )
                        Text(mission.title, modifier = Modifier.weight(1f),
                            color = DeepSteel, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            SchoolCard {
                Eyebrow("TRAINING MODE")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PracticeMode.entries.forEach { mode ->
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = practiceMode == mode,
                            onClick = { onMode(mode) },
                            label = { Text(mode.label, fontSize = 12.sp) }
                        )
                    }
                }
                SubText(if (practiceMode == PracticeMode.FREE)
                    "Explore Rotate and Thrust/Pull with no automatic locator voice commands."
                else "Juan gives scripted practice instructions once you start the mission.")
            }
        }
        item {
            SchoolCard {
                Eyebrow("GUIDANCE DISPLAY")
                GuidanceStyle.entries.forEach { style ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = displayStyle == style,
                            onClick = { onStyle(style) }
                        )
                        Column(Modifier.weight(1f)) {
                            Text(style.title, color = DeepSteel,
                                fontWeight = FontWeight.Bold)
                            Text(style.description, color = SchoolMuted, fontSize = 11.sp)
                        }
                    }
                }
                SubText("2D/3D switches inside the simulator. Displays are educational " +
                    "interpretations, not exact DigiTrak or Subsite firmware.")
            }
        }
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Construction, null,
                        tint = OrangeDark, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Eyebrow("TRAINING RIG")
                        Text(rig.displayName, color = DeepSteel, fontWeight = FontWeight.Bold)
                        SubText(oneDecimal(rig.rodFeet) + " ft nominal rods")
                    }
                    TextButton(onClick = onRigSettings) { Text("Change") }
                }
            }
        }
        item {
            Button(
                onClick = onLaunch,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text("Launch Operator Seat", fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp)
            }
        }
    }
}
