package com.forgecrew.drillschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.roundToInt

/** Real gestures, fictional training response. Actual drill controls vary by rig. */
@Composable
fun OperatorSeatPage(
    session: SimState,
    missionIndex: Int,
    onMission: (Int) -> Unit,
    onRotate: (Int, Int) -> Unit,
    onStopRotation: () -> Unit,
    onMove: (SimMode, Int) -> Unit,
    onPullBack: () -> Unit,
    onReset: () -> Unit,
    onPeek: () -> Unit,
    onReplayLocator: () -> Unit,
    onSettings: () -> Unit,
    onHaptic: (FeedbackCue) -> Unit,
    voiceEnabled: Boolean
) {
    var leftAxis by remember { mutableFloatStateOf(0f) }
    var rightAxis by remember { mutableFloatStateOf(0f) }
    var missionPicker by remember { mutableStateOf(false) }
    var lastHeldClock by remember { mutableIntStateOf(0) }
    val latestSession by rememberUpdatedState(session)
    val rotationInput = when { leftAxis > 0.22f -> 1; leftAxis < -0.22f -> -1; else -> 0 }
    val thrustInput = when { rightAxis < -0.22f -> 1; rightAxis > 0.22f -> -1; else -> 0 }
    val rpm = if (rotationInput == 0) 0 else (25 + 85 * abs(leftAxis)).roundToInt()
    val locator = LocatorCoach.current(session)

    LaunchedEffect(rotationInput, rpm, session.phase) {
        if (session.phase != SimPhase.RUNNING) return@LaunchedEffect
        if (rotationInput == 0) {
            onStopRotation()
            return@LaunchedEffect
        }
        while (isActive) {
            onRotate(rotationInput, rpm)
            delay(if (rpm >= 85) 210 else 340)
        }
    }
    LaunchedEffect(thrustInput, rotationInput, session.phase) {
        if (thrustInput == 0 || session.phase != SimPhase.RUNNING) return@LaunchedEffect
        while (isActive && latestSession.phase == SimPhase.RUNNING) {
            if (thrustInput > 0) onMove(
                if (rotationInput != 0) SimMode.ROTATE else SimMode.STEER,
                latestSession.lastClock
            ) else onPullBack()
            delay(650)
        }
    }
    LaunchedEffect(rotationInput, thrustInput) {
        if (rotationInput != 0 || thrustInput != 0) onHaptic(FeedbackCue.ENGAGE)
    }
    LaunchedEffect(session.lastClock, locator.key) {
        if (locator.targetClock == session.lastClock && lastHeldClock != session.lastClock) {
            lastHeldClock = session.lastClock
            onHaptic(FeedbackCue.CLOCK)
        } else if (locator.targetClock != session.lastClock) lastHeldClock = 0
    }

    Column(Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("SIMULATOR • TRAINING MODE")
                        Text("Operator Seat", fontSize = 24.sp, fontWeight = FontWeight.Black, color = DeepSteel)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, "Training settings", tint = DeepSteel)
                    }
                }
                Box {
                    OutlinedButton(
                        onClick = { missionPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Icon(Icons.Outlined.Flag, null, tint = OrangeDark)
                        Spacer(Modifier.width(8.dp))
                        Text(session.mission.title, Modifier.weight(1f), maxLines = 1,
                            overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                        Icon(Icons.Outlined.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = missionPicker, onDismissRequest = { missionPicker = false }) {
                        trainingMissions.forEachIndexed { index, mission ->
                            DropdownMenuItem(text = { Text(mission.title) }, onClick = {
                                leftAxis = 0f; rightAxis = 0f; missionPicker = false; onMission(index)
                            })
                        }
                    }
                }
                LinearProgressIndicator(
                    progress = { (session.stationFeet / session.mission.totalFeet).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = OrangeDark, trackColor = OrangePale
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    SubText("${oneDecimal(session.stationFeet)} ft drilled")
                    SubText("${oneDecimal(session.mission.totalFeet - session.stationFeet)} ft to exit")
                }
            }
        }
        item {
            Surface(color = DeepSteel, shape = RoundedCornerShape(19.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.RecordVoiceOver, null, tint = Orange, modifier = Modifier.size(25.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("LOCATOR • JUAN", color = Color.White, fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f), letterSpacing = 1.sp, fontSize = 12.sp)
                        TextButton(onClick = onReplayLocator,
                            contentPadding = PaddingValues(horizontal = 4.dp)) {
                            Icon(Icons.Outlined.VolumeUp, null, tint = Color.White,
                                modifier = Modifier.size(19.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(if (voiceEnabled) "REPLAY" else "PLAY", color = Color.White,
                                fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Text("“${locator.message}”", color = Color.White, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Radio, null, tint = Orange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(locator.objective, color = Color(0xFFFFCEA4),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("BORE VIEW • TRAINING MODEL")
                        Text("Live shot position", color = DeepSteel, fontWeight = FontWeight.Bold)
                    }
                    Text("${session.lastClock} O'CLOCK", color = OrangeDark, fontWeight = FontWeight.Black)
                }
                TrainingBoreCanvas(session = session, height = 148.dp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniMetric("DEPTH", "${oneDecimal(session.depthFeet)} ft", Modifier.weight(1f))
                    MiniMetric("PITCH", "${oneDecimal(session.pitchPercent)}%", Modifier.weight(1f))
                    MiniMetric("ROTATION", "${if (rotationInput == 0) 0 else rpm} RPM", Modifier.weight(1f))
                }
                Button(onClick = onPeek, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeDark),
                    shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Outlined.StackedLineChart, null)
                    Spacer(Modifier.width(7.dp))
                    Text("Peek at Live Bar Shot", fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            SchoolCard {
                Eyebrow("MISSION STATUS")
                val accent = when (session.phase) {
                    SimPhase.RUNNING -> DeepSteel
                    SimPhase.COMPLETED -> SafeGreen
                    SimPhase.FAILED -> Color(0xFFC43A36)
                }
                Text(session.report, fontWeight = FontWeight.SemiBold, color = accent)
                if (session.phase == SimPhase.COMPLETED) {
                    Text("BEST SHOT • ${session.score}/100", color = SafeGreen,
                        fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
                OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.RestartAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Restart mission")
                }
            }
        }
        item { SubText("Educational game: fictional tool response and hazards. Not for use on real jobs.") }
    }
    OperatorControlDock(
        session = session,
        rotationInput = rotationInput,
        rpm = rpm,
        thrustInput = thrustInput,
        onLeftAxis = { leftAxis = it },
        onRightAxis = { rightAxis = it },
        onRotate = onRotate,
        onMove = onMove,
        onPullBack = onPullBack,
        onPeek = onPeek
    )
    }
}

@Composable
private fun OperatorControlDock(
    session: SimState,
    rotationInput: Int,
    rpm: Int,
    thrustInput: Int,
    onLeftAxis: (Float) -> Unit,
    onRightAxis: (Float) -> Unit,
    onRotate: (Int, Int) -> Unit,
    onMove: (SimMode, Int) -> Unit,
    onPullBack: () -> Unit,
    onPeek: () -> Unit
) {
SchoolCard(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Eyebrow("OPERATOR CONTROLS")
            Text("Rotation + Thrust", fontSize = 20.sp,
                color = DeepSteel, fontWeight = FontWeight.Black)
        }
        TextButton(onClick = onPeek, contentPadding = PaddingValues(horizontal = 4.dp)) {
            Icon(Icons.Outlined.StackedLineChart, null, Modifier.size(17.dp))
            Spacer(Modifier.width(4.dp))
            Text("Live Bar Shot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            TrainingJoystick(
                label = "ROTATE", hint = "Left / right", horizontal = true,
                onAxis = onLeftAxis, enabled = session.phase == SimPhase.RUNNING
            )
            Text("${if (rotationInput == 0) "STOPPED" else "$rpm RPM"}",
                fontSize = 11.sp, color = OrangeDark, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            TrainingJoystick(
                label = "PUSH / PULL", hint = "Up / down", horizontal = false,
                onAxis = onRightAxis, enabled = session.phase == SimPhase.RUNNING
            )
            Text(when (thrustInput) { 1 -> "PUSHING"; -1 -> "PULLING"; else -> "NEUTRAL" },
                fontSize = 11.sp, color = OrangeDark, fontWeight = FontWeight.Bold)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { onRotate(-1, 40) }, modifier = Modifier.weight(1f),
            enabled = session.phase == SimPhase.RUNNING, contentPadding = PaddingValues(5.dp)) {
            Icon(Icons.Outlined.RotateLeft, null, Modifier.size(18.dp))
            Text(" Rotate", fontSize = 12.sp)
        }
        OutlinedButton(onClick = { onMove(SimMode.STEER, session.lastClock) },
            modifier = Modifier.weight(1f), enabled = session.phase == SimPhase.RUNNING,
            contentPadding = PaddingValues(5.dp)) {
            Icon(Icons.Outlined.ArrowForward, null, Modifier.size(18.dp))
            Text(" Push 5 ft", fontSize = 12.sp)
        }
        OutlinedButton(onClick = onPullBack, modifier = Modifier.weight(1f),
            enabled = session.phase == SimPhase.RUNNING && session.shots.size > 1,
            contentPadding = PaddingValues(5.dp)) {
            Icon(Icons.Outlined.ArrowBack, null, Modifier.size(18.dp))
            Text(" Pull", fontSize = 12.sp)
        }
    }
}
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = SchoolMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = DeepSteel, fontSize = 13.sp, fontWeight = FontWeight.Black,
            maxLines = 1)
    }
}

@Composable
private fun TrainingJoystick(
    label: String,
    hint: String,
    horizontal: Boolean,
    onAxis: (Float) -> Unit,
    enabled: Boolean
) {
    val maxMovePx = with(LocalDensity.current) { 27.dp.toPx() }
    var knob by remember { mutableStateOf(Offset.Zero) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = DeepSteel, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text(hint, color = SchoolMuted, fontSize = 10.sp)
        Spacer(Modifier.height(5.dp))
        Canvas(
            Modifier.size(106.dp)
                .pointerInput(enabled, horizontal) {
                    if (enabled) detectDragGestures(
                        onDrag = { change, amount ->
                            change.consume()
                            knob = if (horizontal) Offset((knob.x + amount.x).coerceIn(-maxMovePx, maxMovePx), 0f)
                                else Offset(0f, (knob.y + amount.y).coerceIn(-maxMovePx, maxMovePx))
                            onAxis(if (horizontal) knob.x / maxMovePx else knob.y / maxMovePx)
                        },
                        onDragEnd = { knob = Offset.Zero; onAxis(0f) },
                        onDragCancel = { knob = Offset.Zero; onAxis(0f) }
                    )
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val outer = size.minDimension / 2 - 3.dp.toPx()
            drawCircle(Color(0xFFF4F6F8), outer, center)
            drawCircle(Color(0xFFC7D0D5), outer, center, style = Stroke(2.dp.toPx()))
            drawCircle(DeepSteel, outer * .76f, center)
            drawCircle(OrangeDark, outer * .78f, center, style = Stroke(5.dp.toPx()))
            if (horizontal) {
                drawLine(OrangeDark, Offset(center.x - outer * .92f, center.y),
                    Offset(center.x - outer * .72f, center.y), 3.dp.toPx(), cap = StrokeCap.Round)
                drawLine(OrangeDark, Offset(center.x + outer * .72f, center.y),
                    Offset(center.x + outer * .92f, center.y), 3.dp.toPx(), cap = StrokeCap.Round)
            } else {
                drawLine(OrangeDark, Offset(center.x, center.y - outer * .92f),
                    Offset(center.x, center.y - outer * .72f), 3.dp.toPx(), cap = StrokeCap.Round)
                drawLine(OrangeDark, Offset(center.x, center.y + outer * .72f),
                    Offset(center.x, center.y + outer * .92f), 3.dp.toPx(), cap = StrokeCap.Round)
            }
            val stick = center + knob
            drawCircle(Color(0xFF080E13), outer * .53f, stick)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF646D76), Color(0xFF252C32))),
                outer * .44f, stick)
            drawCircle(Color(0xFF77818A), outer * .40f, stick,
                style = Stroke(1.5.dp.toPx()))
        }
    }
}
