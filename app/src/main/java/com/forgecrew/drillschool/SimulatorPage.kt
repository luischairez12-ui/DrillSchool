package com.forgecrew.drillschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SimulatorPage(
    session: SimState,
    missionIndex: Int,
    onMission: (Int) -> Unit,
    onMove: (SimMode, Int) -> Unit,
    onReset: () -> Unit,
    onLearn: () -> Unit
) {
    var clock by remember { mutableIntStateOf(12) }
    var mode by remember { mutableStateOf(SimMode.STEER) }
    var missionMenuOpen by remember { mutableStateOf(false) }
    val mission = session.mission
    val nextHazard = TrainingEngine.nearestHazard(session)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Eyebrow("TRAINING YARD")
                Text("Operator Console", style = MaterialTheme.typography.headlineMedium,
                    color = DeepSteel, fontWeight = FontWeight.Black)
                SubText("Practice clock steering, pitch control, and reading the bore path.")
            }
        }
        item {
            SchoolCard {
                Eyebrow("TRAINING MISSION")
                Box {
                    OutlinedButton(
                        onClick = { missionMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Text(mission.title, modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold, maxLines = 1)
                        Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Choose a mission")
                    }
                    DropdownMenu(expanded = missionMenuOpen,
                        onDismissRequest = { missionMenuOpen = false }) {
                        trainingMissions.forEachIndexed { index, item ->
                            DropdownMenuItem(text = { Text(item.title) }, onClick = {
                                onMission(index)
                                missionMenuOpen = false
                            })
                        }
                    }
                }
                SubText(mission.description)
            }
        }
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("LIVE BORE PROFILE")
                    Spacer(Modifier.weight(1f))
                    Text("${oneDecimal(session.stationFeet)} / ${oneDecimal(mission.totalFeet)} ft",
                        color = OrangeDark, fontWeight = FontWeight.Bold)
                }
                BoreProfile(session)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(Color(0xFFDB533D), "Game hazard")
                    LegendDot(OrangeDark, "Actual path")
                    LegendDot(SafeGreen, "Exit target")
                }
                LinearProgressIndicator(
                    progress = { (session.stationFeet / mission.totalFeet).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = OrangeDark, trackColor = OrangePale
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Readout("DEPTH", "${oneDecimal(session.depthFeet)} ft", Modifier.weight(1f))
                Readout("PITCH", "${oneDecimal(session.pitchPercent)}%", Modifier.weight(1f))
                Readout("OFFSET", "${oneDecimal(session.lateralFeet)} ft", Modifier.weight(1f))
            }
        }
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("Drill controls", "Choose toolface, then advance a rod segment.")
                    Spacer(Modifier.weight(1f))
                    Text("5 FT", color = OrangeDark, fontWeight = FontWeight.Black)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = mode == SimMode.STEER,
                        onClick = { mode = SimMode.STEER }, label = { Text("Steer / push") },
                        leadingIcon = { Icon(Icons.Outlined.Explore, null, Modifier.size(17.dp)) })
                    FilterChip(selected = mode == SimMode.ROTATE,
                        onClick = { mode = SimMode.ROTATE }, label = { Text("Rotate") },
                        leadingIcon = { Icon(Icons.Outlined.RotateRight, null, Modifier.size(17.dp)) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("TOOLFACE")
                        Text("$clock o'clock", fontWeight = FontWeight.Black, fontSize = 25.sp,
                            color = DeepSteel)
                        SubText(if (mode == SimMode.ROTATE) "Rotating: direction held in this game"
                            else when (clock) {
                                12 -> "Pitch tends upward"
                                6 -> "Pitch tends downward"
                                3 -> "Lateral change right"
                                9 -> "Lateral change left"
                                else -> "Combined pitch/lateral correction"
                            })
                    }
                    ToolfaceDial(clock, Modifier.size(112.dp))
                }
                Slider(value = clock.toFloat(),
                    onValueChange = { clock = it.roundToInt().coerceIn(1, 12) },
                    valueRange = 1f..12f, steps = 10,
                    enabled = mode == SimMode.STEER,
                    modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(12, 3, 6, 9).forEach { value ->
                        OutlinedButton(onClick = { clock = value },
                            modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(11.dp),
                            enabled = mode == SimMode.STEER) {
                            Text("$value", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Button(
                    onClick = { onMove(mode, clock) },
                    enabled = session.phase == SimPhase.RUNNING,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeDark)
                ) {
                    Icon(Icons.Outlined.DoubleArrow, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Advance 5 ft", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                }
                OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text("Reset mission")
                }
            }
        }
        item {
            SchoolCard {
                Eyebrow("LOCATOR / CREW READOUT")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Sensors, contentDescription = null,
                        tint = OrangeDark, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${session.thrusts} advances  •  ${session.steerThrusts} steers",
                            fontWeight = FontWeight.Bold, color = DeepSteel)
                        SubText(nextHazard?.let { "Next game hazard: ${it.label} at ${oneDecimal(it.stationFeet)} ft" }
                            ?: "No remaining utility markers ahead")
                    }
                }
                HorizontalDivider(color = SchoolLine)
                val resultColor = when (session.phase) {
                    SimPhase.RUNNING -> DeepSteel
                    SimPhase.COMPLETED -> SafeGreen
                    SimPhase.FAILED -> Color(0xFFB83535)
                }
                Text(session.report, fontWeight = FontWeight.SemiBold, color = resultColor)
                if (session.phase == SimPhase.COMPLETED) {
                    Text("TRAINING SCORE: ${session.score}/100",
                        fontWeight = FontWeight.Black, color = SafeGreen,
                        style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("Shot log", "Most recent simulator steps")
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onLearn) { Text("Learn basics ›") }
                }
                session.shots.takeLast(6).asReversed().forEach { shot ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${oneDecimal(shot.stationFeet)} ft", Modifier.width(63.dp),
                            fontWeight = FontWeight.Bold, color = DeepSteel)
                        Text("${oneDecimal(shot.depthFeet)} ft deep", Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall, color = SchoolMuted)
                        Text("${oneDecimal(shot.pitchPercent)}%  •  ${if (shot.mode == SimMode.STEER) "S" else "R"}",
                            style = MaterialTheme.typography.bodySmall, color = OrangeDark)
                    }
                }
            }
        }
        item { SubText("Educational simulation only. Steering sensitivity, ground, utilities and depth are fictional. Never use this model to plan a real bore or establish utility clearance.") }
    }
}

@Composable
private fun LegendDot(color: Color, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(50)))
        Spacer(Modifier.width(4.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = SchoolMuted)
    }
}

@Composable
private fun Readout(label: String, value: String, modifier: Modifier) {
    SchoolCard(modifier) {
        Eyebrow(label)
        Text(value, fontWeight = FontWeight.Black, color = DeepSteel,
            fontSize = 19.sp, maxLines = 1)
    }
}

@Composable
private fun ToolfaceDial(clock: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f - 8.dp.toPx()
        drawCircle(OrangePale, r + 7.dp.toPx(), Offset(cx, cy))
        drawCircle(DeepSteel, r, Offset(cx, cy), style = Stroke(width = 3.dp.toPx()))
        for (index in 0..11) {
            val a = (index * kotlin.math.PI / 6.0 - kotlin.math.PI / 2.0)
            val px = kotlin.math.cos(a).toFloat()
            val py = kotlin.math.sin(a).toFloat()
            drawLine(DeepSteel, Offset(cx + r * 0.82f * px, cy + r * 0.82f * py),
                Offset(cx + r * 0.97f * px, cy + r * 0.97f * py), strokeWidth = 2.dp.toPx())
        }
        val angle = ((clock % 12) * kotlin.math.PI / 6.0 - kotlin.math.PI / 2.0)
        drawLine(OrangeDark, Offset(cx, cy),
            Offset(cx + r * 0.70f * kotlin.math.cos(angle).toFloat(),
                cy + r * 0.70f * kotlin.math.sin(angle).toFloat()),
            strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(OrangeDark, 6.dp.toPx(), Offset(cx, cy))
    }
}

@Composable
private fun BoreProfile(session: SimState) {
    Canvas(Modifier.fillMaxWidth().height(184.dp).background(Color(0xFFFFF8ED),
        RoundedCornerShape(16.dp))) {
        val left = 18.dp.toPx()
        val right = size.width - 15.dp.toPx()
        val ground = 22.dp.toPx()
        val bottom = size.height - 18.dp.toPx()
        fun posX(ft: Double): Float = left + ((right - left) * ft / session.mission.totalFeet).toFloat()
        fun posY(depth: Double): Float = ground + ((bottom - ground) * depth / 12.0).toFloat()
        drawRect(Color(0xFFFAEDDA), topLeft = Offset(left, ground),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - ground))
        for (level in 0..12 step 3) {
            val y = posY(level.toDouble())
            drawLine(Color(0xFFD9CDBB), Offset(left, y), Offset(right, y), 1.dp.toPx())
        }
        drawLine(Color(0xFF587C64), Offset(left, ground), Offset(right, ground),
            strokeWidth = 3.dp.toPx())
        session.mission.utilities.forEach { hazard ->
            val offset = Offset(posX(hazard.stationFeet), posY(hazard.depthFeet))
            drawCircle(Color(0x22C83B3B), radius = 17.dp.toPx(), center = offset)
            drawCircle(Color(0xFFC4473C), radius = 8.dp.toPx(), center = offset,
                style = Stroke(3.dp.toPx()))
            drawLine(Color(0xFFC4473C), Offset(offset.x - 5.dp.toPx(), offset.y),
                Offset(offset.x + 5.dp.toPx(), offset.y), 2.dp.toPx())
        }
        val target = Offset(posX(session.mission.totalFeet), posY(session.mission.exitDepthFeet))
        drawCircle(SafeGreen, radius = 12.dp.toPx(), center = target,
            style = Stroke(3.dp.toPx()))
        drawCircle(SafeGreen, radius = 3.dp.toPx(), center = target)
        val path = Path()
        session.shots.forEachIndexed { index, shot ->
            val x = posX(shot.stationFeet)
            val y = posY(shot.depthFeet)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, OrangeDark, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        val last = session.shots.last()
        drawCircle(DeepSteel, radius = 7.dp.toPx(), center = Offset(posX(last.stationFeet), posY(last.depthFeet)))
        drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(posX(last.stationFeet), posY(last.depthFeet)))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SubText("ENTRY")
        SubText("Game-only side profile • 0–12 ft")
        SubText("EXIT")
    }
}
