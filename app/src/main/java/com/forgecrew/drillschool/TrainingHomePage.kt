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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Academy landing screen: never launches the engine or locator voice on app start. */
@Composable
fun TrainingHomePage(
    session: SimState,
    rig: RigProfile,
    bestScores: Map<String, Int>,
    onSimulator: () -> Unit,
    onLessons: () -> Unit,
    onMissions: () -> Unit,
    onProgress: () -> Unit,
    onRigSettings: () -> Unit,
    onResume: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = OrangePale,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("WELCOME TO DRILLSCHOOL")
                        Text("Training Academy", color = DeepSteel,
                            fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Text("Learn • Practice • Operate",
                            color = SchoolMuted, fontSize = 13.sp)
                    }
                    Icon(Icons.Outlined.Construction, null, tint = OrangeDark,
                        modifier = Modifier.size(50.dp))
                }
            }
        }
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("YOUR CURRENT TRAINING")
                        Text(session.mission.title, color = DeepSteel,
                            fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        SubText(oneDecimal(session.stationFeet) + " / " +
                            oneDecimal(session.mission.totalFeet) + " ft • simulated bore")
                    }
                    Button(onClick = onResume,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeDark)) {
                        Icon(Icons.Outlined.PlayArrow, null)
                        Text(" Resume")
                    }
                }
                LinearProgressIndicator(
                    progress = {
                        (session.stationFeet / session.mission.totalFeet)
                            .toFloat().coerceIn(0f, 1f)
                    },
                    color = OrangeDark, trackColor = OrangePale,
                    modifier = Modifier.fillMaxWidth().height(7.dp)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth()) {
                SchoolHomeTile("Simulator", "Operator-seat practice",
                    Icons.Outlined.SportsEsports, onSimulator, Modifier.weight(1f))
                SchoolHomeTile("Lessons", "Learn HDD fundamentals",
                    Icons.Outlined.MenuBook, onLessons, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth()) {
                SchoolHomeTile("Missions", "Job-based challenges",
                    Icons.Outlined.Flag, onMissions, Modifier.weight(1f))
                SchoolHomeTile("Progress", "Your best scores",
                    Icons.Outlined.EmojiEvents, onProgress, Modifier.weight(1f))
            }
        }
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.SettingsInputComponent, null, tint = OrangeDark,
                        modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Eyebrow("CURRENT RIG")
                        Text(rig.displayName, color = DeepSteel,
                            fontWeight = FontWeight.Bold)
                        SubText("Change drill and training display")
                    }
                    TextButton(onClick = onRigSettings) { Text("Change") }
                }
                SubText("Completed training scores: " +
                    bestScores.values.count { it > 0 } + " of " +
                    trainingMissions.size + " missions")
            }
        }
        item {
            SubText("DrillSchool is a simplified educational simulator, not a machine " +
                "control system or substitute for equipment and utility safety training.")
        }
    }
}

@Composable
private fun SchoolHomeTile(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(132.dp),
        onClick = onClick,
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine)
    ) {
        Column(
            Modifier.padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(color = OrangePale, shape = RoundedCornerShape(12.dp)) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = OrangeDark)
                }
            }
            Column {
                Text(title, color = DeepSteel, fontWeight = FontWeight.Black,
                    fontSize = 17.sp)
                Text(description, color = SchoolMuted, fontSize = 11.sp,
                    lineHeight = 13.sp, maxLines = 2)
            }
        }
    }
}
