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
import androidx.compose.ui.unit.Dp
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


    // Cockpit-first layout: mission + locator + live bore remain visible above
    // a compact joystick dock, instead of letting the dock consume the screen.
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            item {
                Surface(color = Color.White, shape = RoundedCornerShape(17.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine)) {
                    Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Flag, null, tint = OrangeDark,
                                        modifier = Modifier.size(19.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("OPERATOR SEAT", color = OrangeDark,
                                            letterSpacing = 1.sp, fontSize = 9.sp,
                                            fontWeight = FontWeight.Black)
                                        Text(session.mission.title, color = DeepSteel,
                                            fontSize = 14.sp, fontWeight = FontWeight.Black,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(onClick = { missionPicker = true },
                                        modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Outlined.ArrowDropDown,
                                            "Choose training mission", tint = OrangeDark)
                                    }
                                }
                                DropdownMenu(expanded = missionPicker,
                                    onDismissRequest = { missionPicker = false }) {
                                    trainingMissions.forEachIndexed { index, mission ->
                                        DropdownMenuItem(text = { Text(mission.title) }, onClick = {
                                            leftAxis = 0f
                                            rightAxis = 0f
                                            missionPicker = false
                                            onMission(index)
                                        })
                                    }
                                }
                            }
                            IconButton(onClick = onSettings, modifier = Modifier.size(38.dp)) {
                                Icon(Icons.Outlined.Settings, "Simulator settings", tint = DeepSteel)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { (session.stationFeet / session.mission.totalFeet).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp),
                            color = OrangeDark, trackColor = OrangePale
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${oneDecimal(session.stationFeet)} ft drilled",
                                fontSize = 10.sp, color = SchoolMuted)
                            Text("${oneDecimal(session.mission.totalFeet - session.stationFeet)} ft to exit",
                                fontSize = 10.sp, color = SchoolMuted)
                        }
                    }
                }
            }
            item {
                Surface(color = DeepSteel, shape = RoundedCornerShape(17.dp)) {
                    Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.RecordVoiceOver, null, tint = Orange,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("LOCATOR • JUAN", color = Color.White,
                                fontSize = 11.sp, fontWeight = FontWeight.Black,
                                modifier = Modifier.weight(1f))
                            TextButton(onClick = onReplayLocator,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier.height(29.dp)) {
                                Icon(Icons.Outlined.VolumeUp, null, tint = Color.White,
                                    modifier = Modifier.size(17.dp))
                                Text(if (voiceEnabled) " Replay" else " Play",
                                    color = Color.White, fontSize = 11.sp)
                            }
                        }
                        Text("“${locator.message}”", color = Color.White, fontSize = 13.sp,
                            lineHeight = 17.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text(locator.objective, color = Color(0xFFFFC38C),
                            fontSize = 10.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            item {
                Surface(color = Color.White, shape = RoundedCornerShape(17.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine)) {
                    Column(Modifier.padding(9.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("LIVE BORE • TRAINING MODEL", color = OrangeDark,
                                fontSize = 10.sp, letterSpacing = .5.sp,
                                fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                            Text("${session.lastClock} O'CLOCK",
                                color = DeepSteel, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        TrainingBoreCanvas(session = session, height = 135.dp)
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            MiniMetric("DEPTH", "${oneDecimal(session.depthFeet)} ft",
                                Modifier.weight(1f))
                            MiniMetric("PITCH", "${oneDecimal(session.pitchPercent)}%",
                                Modifier.weight(1f))
                            MiniMetric("ROTATION", "${if (rotationInput == 0) 0 else rpm} RPM",
                                Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                if (session.phase != SimPhase.RUNNING) {
                    SchoolCard {
                        Text(session.report,
                            color = if (session.phase == SimPhase.COMPLETED) SafeGreen
                                else Color(0xFFC43A36),
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        if (session.phase == SimPhase.COMPLETED) {
                            Text("SCORE • ${session.score}/100", color = SafeGreen,
                                fontWeight = FontWeight.Black)
                        }
                        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.RestartAlt, null)
                            Text(" Restart mission")
                        }
                    }
                } else {
                    Text(session.report, color = SchoolMuted, fontSize = 10.sp,
                        maxLines = 2, lineHeight = 12.sp)
                }
            }
            item {
                Text("Fictional training physics • not real drilling or utility safety guidance.",
                    color = SchoolMuted, fontSize = 9.sp)
            }
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
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp),
        shape = RoundedCornerShape(17.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("DRILL CONTROLS", color = OrangeDark, fontSize = 10.sp,
                    letterSpacing = .7.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = onPeek, modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Icon(Icons.Outlined.StackedLineChart, null, Modifier.size(17.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Peek at Live Bar Shot", fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                }
            }
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)) {
                    Text("ROTATE", fontSize = 10.sp, color = DeepSteel,
                        fontWeight = FontWeight.Black)
                    TrainingJoystick(label = "", hint = "", horizontal = true,
                        onAxis = onLeftAxis, enabled = session.phase == SimPhase.RUNNING,
                        diameter = 81.dp)
                    Text(if (rotationInput == 0) "STOPPED" else "${rpm} RPM",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OrangeDark)
                }
                Surface(shape = RoundedCornerShape(14.dp), color = OrangePale) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CLOCK", color = SchoolMuted, fontSize = 9.sp)
                        Text("${session.lastClock}", fontSize = 24.sp,
                            color = DeepSteel, fontWeight = FontWeight.Black)
                        Text("O'CLOCK", color = OrangeDark,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)) {
                    Text("PUSH / PULL", fontSize = 10.sp, color = DeepSteel,
                        fontWeight = FontWeight.Black)
                    TrainingJoystick(label = "", hint = "", horizontal = false,
                        onAxis = onRightAxis, enabled = session.phase == SimPhase.RUNNING,
                        diameter = 81.dp)
                    Text(when (thrustInput) {
                        1 -> "PUSHING"
                        -1 -> "PULLING"
                        else -> "NEUTRAL"
                    }, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OrangeDark)
                }
            }
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = { onRotate(-1, 0) },
                    modifier = Modifier.weight(1f).height(34.dp),
                    enabled = session.phase == SimPhase.RUNNING,
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Outlined.RotateLeft, null, Modifier.size(15.dp))
                    Text(" Clock -1", fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = { onMove(SimMode.STEER, session.lastClock) },
                    modifier = Modifier.weight(1f).height(34.dp),
                    enabled = session.phase == SimPhase.RUNNING,
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Outlined.ArrowForward, null, Modifier.size(15.dp))
                    Text(" Push 5 ft", fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = onPullBack,
                    modifier = Modifier.weight(1f).height(34.dp),
                    enabled = session.phase == SimPhase.RUNNING && session.shots.size > 1,
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Icon(Icons.Outlined.ArrowBack, null, Modifier.size(15.dp))
                    Text(" Pull", fontSize = 10.sp)
                }
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
    enabled: Boolean,
    diameter: Dp = 106.dp
) {
    val maxMovePx = with(LocalDensity.current) { (diameter * 0.26f).toPx() }
    var knob by remember { mutableStateOf(Offset.Zero) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (label.isNotBlank()) Text(label, color = DeepSteel, fontWeight = FontWeight.Black, fontSize = 12.sp)
        if (hint.isNotBlank()) Text(hint, color = SchoolMuted, fontSize = 10.sp)
        if (label.isNotBlank() || hint.isNotBlank()) Spacer(Modifier.height(5.dp))
        Canvas(
            Modifier.size(diameter)
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
