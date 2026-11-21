package com.forgecrew.drillschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Educational rig-side cockpit. Guidance monitor owns the clock and pitch.
 * Left control rotates, right control pushes/pulls; both work simultaneously.
 */
@Composable
fun OperatorSeatPage(
    session: SimState,
    rig: RigProfile,
    displayStyle: GuidanceStyle,
    threeD: Boolean,
    onThreeD: (Boolean) -> Unit,
    practiceMode: PracticeMode,
    onRotate: (Int, Int) -> Unit,
    onStopRotation: () -> Unit,
    onMove: (SimMode, Int) -> Unit,
    onPullBack: () -> Unit,
    onAddRod: () -> Unit,
    onReset: () -> Unit,
    onPeek: () -> Unit,
    onPeekLocator: () -> Unit,
    onReplayLocator: () -> Unit,
    onHaptic: (FeedbackCue) -> Unit,
    voiceEnabled: Boolean
) {
    var rotateAxis by remember { mutableFloatStateOf(0f) }
    var thrustAxis by remember { mutableFloatStateOf(0f) }
    val currentSession by rememberUpdatedState(session)
    val rotation = when {
        rotateAxis > .23f -> 1
        rotateAxis < -.23f -> -1
        else -> 0
    }
    val thrust = when {
        thrustAxis < -.23f -> 1
        thrustAxis > .23f -> -1
        else -> 0
    }
    val rpm = if (rotation == 0) 0 else
        (20 + (rig.maxSpindleRpm - 20) * abs(rotateAxis)).roundToInt()
            .coerceIn(0, rig.maxSpindleRpm)
    val locator = LocatorCoach.current(session)

    LaunchedEffect(rotation, rpm, session.phase) {
        if (session.phase != SimPhase.RUNNING) return@LaunchedEffect
        if (rotation == 0) {
            onStopRotation()
            return@LaunchedEffect
        }
        while (isActive && currentSession.phase == SimPhase.RUNNING) {
            onRotate(rotation, rpm)
            delay(340)
        }
    }
    LaunchedEffect(thrust, rotation, session.phase, session.awaitingNextRod) {
        if (thrust == 0 || session.phase != SimPhase.RUNNING ||
            session.awaitingNextRod) return@LaunchedEffect
        // First step is applied on touch-down so fast taps are never lost.
        // Repetition begins after a short hold, avoiding a double first step.
        delay(520)
        while (isActive && currentSession.phase == SimPhase.RUNNING &&
            !currentSession.awaitingNextRod) {
            if (thrust > 0) {
                onMove(if (rotation != 0) SimMode.ROTATE else SimMode.STEER,
                    currentSession.lastClock)
            } else {
                onPullBack()
            }
            delay(600)
        }
    }
    LaunchedEffect(rotation, thrust) {
        if (rotation != 0 || thrust != 0) onHaptic(FeedbackCue.ENGAGE)
    }
    LaunchedEffect(session.lastClock, locator.key) {
        if (locator.targetClock == session.lastClock) onHaptic(FeedbackCue.CLOCK)
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GuidanceDisplayPanel(
            session = session,
            style = displayStyle,
            threeD = threeD,
            onViewChange = onThreeD,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        Surface(color = Color.White, shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine)) {
            Row(
                Modifier.fillMaxWidth().height(47.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Flag, null, tint = OrangeDark,
                    modifier = Modifier.size(21.dp))
                Spacer(Modifier.width(7.dp))
                Column(Modifier.weight(1f)) {
                    Text(session.mission.title, fontWeight = FontWeight.Black,
                        fontSize = 12.sp, color = DeepSteel, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text(oneDecimal(session.stationFeet) + " / " +
                        oneDecimal(session.mission.totalFeet) +
                        " ft  •  Rod " + session.rodNumber,
                        fontSize = 10.sp, color = SchoolMuted)
                }
                TextButton(
                    onClick = onPeekLocator,
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Icon(Icons.Outlined.PhonelinkRing, null, Modifier.size(15.dp))
                    Text(" Locator", fontSize = 10.sp)
                }
                TextButton(
                    onClick = onPeek,
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    Icon(Icons.Outlined.Route, null, Modifier.size(15.dp))
                    Text(" Bar Shot", fontSize = 10.sp)
                }
            }
        }

        Surface(color = OrangePale, shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF4CA9D))) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 53.dp, max = 72.dp)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.RecordVoiceOver, null, tint = OrangeDark,
                    modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (practiceMode == PracticeMode.GUIDED) "LOCATOR JUAN"
                        else "FREE PRACTICE",
                        color = OrangeDark, fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                    Text(
                        if (practiceMode == PracticeMode.GUIDED) locator.message
                        else "No automatic callouts. Practice rotating and pushing at your own pace.",
                        color = DeepSteel,
                        fontSize = 11.sp, lineHeight = 14.sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }
                if (practiceMode == PracticeMode.GUIDED) {
                    IconButton(onClick = onReplayLocator, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (voiceEnabled) Icons.Outlined.VolumeUp
                            else Icons.Outlined.VolumeOff,
                            "Replay locator instruction", tint = OrangeDark
                        )
                    }
                }
            }
        }

        if (session.awaitingNextRod) {
            Surface(color = OrangePale, shape = RoundedCornerShape(12.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rod " + session.rodNumber + " complete",
                        color = DeepSteel, modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Button(
                        onClick = {
                            thrustAxis = 0f
                            rotateAxis = 0f
                            onAddRod()
                        },
                        modifier = Modifier.height(33.dp),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 1.dp)
                    ) { Text("Add rod", fontSize = 11.sp) }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Surface(
                Modifier.weight(1f).fillMaxHeight(),
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ROTATE", color = OrangeDark,
                        fontSize = 13.sp, fontWeight = FontWeight.Black)
                    RotationTrainingDial(
                        onAxis = { rotateAxis = it },
                        enabled = session.phase == SimPhase.RUNNING
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onRotate(-1, 0); onHaptic(FeedbackCue.ENGAGE) },
                            modifier = Modifier.size(34.dp),
                            enabled = session.phase == SimPhase.RUNNING
                        ) {
                            Icon(Icons.Outlined.ChevronLeft, "Rotate counterclockwise",
                                tint = OrangeDark)
                        }
                        Text(if (rpm == 0) "STOPPED" else rpm.toString() + " RPM",
                            color = SchoolMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { onRotate(1, 0); onHaptic(FeedbackCue.ENGAGE) },
                            modifier = Modifier.size(34.dp),
                            enabled = session.phase == SimPhase.RUNNING
                        ) {
                            Icon(Icons.Outlined.ChevronRight, "Rotate clockwise",
                                tint = OrangeDark)
                        }
                    }
                }
            }

            Surface(
                Modifier.weight(1f).fillMaxHeight(),
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("THRUST / PULL", color = OrangeDark,
                        fontSize = 13.sp, fontWeight = FontWeight.Black)
                    ThrustHoldButton(
                        label = "PUSH",
                        icon = Icons.Outlined.ArrowUpward,
                        enabled = session.phase == SimPhase.RUNNING &&
                            !session.awaitingNextRod,
                        onHeld = { pressed ->
                            if (pressed) {
                                thrustAxis = -1f
                                onMove(
                                    if (rotation != 0) SimMode.ROTATE else SimMode.STEER,
                                    session.lastClock
                                )
                            } else thrustAxis = 0f
                        },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    ThrustHoldButton(
                        label = "PULL",
                        icon = Icons.Outlined.ArrowDownward,
                        enabled = session.phase == SimPhase.RUNNING &&
                            session.shots.size > 1,
                        onHeld = { pressed ->
                            if (pressed) {
                                thrustAxis = 1f
                                onPullBack()
                            } else thrustAxis = 0f
                        },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    Text(
                        if (session.awaitingNextRod) "ADD NEXT ROD"
                        else if (thrust == 0) "NEUTRAL  •  0% INPUT"
                        else if (thrust > 0) "PUSH  •  100% INPUT"
                        else "PULL  •  100% INPUT",
                        color = SchoolMuted, fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (session.phase != SimPhase.RUNNING) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(session.report, modifier = Modifier.weight(1f),
                    maxLines = 2, color = DeepSteel, fontSize = 11.sp)
                TextButton(onClick = onReset) { Text("Restart") }
            }
        }
    }
}

@Composable
private fun RotationTrainingDial(
    onAxis: (Float) -> Unit,
    enabled: Boolean
) {
    var knob by remember { mutableFloatStateOf(0f) }
    Canvas(
        Modifier.size(96.dp).pointerInput(enabled) {
            if (enabled) detectDragGestures(
                onDrag = { change, drag ->
                    change.consume()
                    knob = (knob + drag.x / 42.dp.toPx()).coerceIn(-1f, 1f)
                    onAxis(knob)
                },
                onDragEnd = { knob = 0f; onAxis(0f) },
                onDragCancel = { knob = 0f; onAxis(0f) }
            )
        }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * .48f
        drawCircle(Color(0xFFF3F5F6), radius, center)
        drawCircle(SchoolLine, radius, center,
            style = Stroke(2.dp.toPx()))
        drawCircle(OrangeDark, radius * .82f, center,
            style = Stroke(4.dp.toPx()))
        drawCircle(DeepSteel, radius * .60f, center)
        drawCircle(Brush.radialGradient(
            listOf(Color(0xFF5A6974), Color(0xFF1C2E37))
        ), radius * .48f, center + Offset(knob * radius * .28f, 0f))
        drawLine(
            OrangeDark,
            Offset(center.x + knob * radius * .28f, center.y - radius * .43f),
            Offset(center.x + knob * radius * .28f, center.y - radius * .12f),
            4.dp.toPx(), cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ThrustHoldButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onHeld: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (enabled) Color(0xFFF8FAFB) else Color(0xFFE9EBEE)
    Surface(
        modifier = modifier.pointerInput(enabled) {
            if (enabled) detectTapGestures(
                onPress = {
                    onHeld(true)
                    try {
                        tryAwaitRelease()
                    } finally {
                        onHeld(false)
                    }
                }
            )
        },
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, label, tint = if (enabled) OrangeDark else SchoolMuted,
                modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, color = if (enabled) DeepSteel else SchoolMuted,
                fontWeight = FontWeight.Black, fontSize = 15.sp)
        }
    }
}
