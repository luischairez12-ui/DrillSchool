package com.forgecrew.drillschool

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun MissionsPage(bestScores: Map<String, Int>, onPlay: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            Eyebrow("DRILLSCHOOL ACADEMY")
            Text("Training Missions", fontSize = 28.sp, fontWeight = FontWeight.Black, color = DeepSteel)
            SubText("Practice real concepts with fictional controls, conditions and hazards.")
        }
        trainingMissions.forEachIndexed { index, mission ->
            item {
                SchoolCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = OrangePale, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)) {
                            Box(Modifier.size(49.dp), contentAlignment = Alignment.Center) {
                                Icon(if (index == 0) Icons.Outlined.Explore else if (index == 1)
                                    Icons.Outlined.Route else Icons.Outlined.WarningAmber,
                                    null, tint = OrangeDark)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Eyebrow(when (index) { 0 -> "BEGINNER"; 1 -> "INTERMEDIATE"; else -> "ADVANCED" })
                            Text(mission.title.substringAfter('•').trim(), fontWeight = FontWeight.Black,
                                color = DeepSteel, fontSize = 18.sp)
                        }
                        val best = bestScores[mission.id] ?: 0
                        if (best > 0) Text("★ $best", color = SafeGreen, fontWeight = FontWeight.Black)
                    }
                    SubText(mission.description)
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {
                        Text("${oneDecimal(mission.totalFeet)} ft • fictional shot",
                            color = OrangeDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${mission.utilities.size} game hazards", color = SchoolMuted, fontSize = 12.sp)
                    }
                    Button(onClick = { onPlay(index) }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeDark)) {
                        Icon(Icons.Outlined.PlayArrow, null)
                        Text(" Start training")
                    }
                }
            }
        }
    }
}
