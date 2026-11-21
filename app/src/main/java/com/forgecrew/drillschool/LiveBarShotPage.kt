package com.forgecrew.drillschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiveBarShotPage(
    session: SimState,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Return to operator controls") }
                Column {
                    Eyebrow("LIVE BAR SHOT • SIMULATION")
                    Text("Bore Path Monitor", color = DeepSteel, fontSize = 25.sp,
                        fontWeight = FontWeight.Black)
                }
            }
        }
        item {
            SchoolCard {
                Eyebrow(session.mission.title)
                Text("Design line vs. current path", color = DeepSteel,
                    fontWeight = FontWeight.Black, fontSize = 17.sp)
                TrainingBoreCanvas(session, 280.dp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChartLegend(Color(0xFFF7901C), "Current")
                    ChartLegend(Color(0xFF91A0AC), "Design")
                    ChartLegend(Color(0xFFE24848), "Game hazards")
                }
                LinearProgressIndicator(progress = {
                    (session.stationFeet / session.mission.totalFeet).toFloat().coerceIn(0f, 1f)
                }, modifier = Modifier.fillMaxWidth().height(7.dp), color = OrangeDark,
                    trackColor = OrangePale)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SubText("Entry • 0 ft")
                    SubText("Head • ${oneDecimal(session.stationFeet)} ft")
                    SubText("Exit • ${oneDecimal(session.mission.totalFeet)} ft")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardMetric("DEPTH", "${oneDecimal(session.depthFeet)} ft", Modifier.weight(1f))
                DashboardMetric("PITCH", "${oneDecimal(session.pitchPercent)}%", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardMetric("CLOCK", "${session.lastClock} o'clock", Modifier.weight(1f))
                DashboardMetric("DISTANCE", "${oneDecimal(session.stationFeet)} ft", Modifier.weight(1f))
            }
        }
        item {
            SchoolCard {
                Eyebrow("LOCATOR • JUAN")
                Text("“${LocatorCoach.current(session).message}”",
                    color = DeepSteel, fontWeight = FontWeight.Bold)
                SubText("Profile and readings update whenever the operator advances or pulls back. " +
                    "All positions, hazards and tolerances are fictional.")
            }
        }
        item {
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeDark),
                shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.ArrowBack, null)
                Spacer(Modifier.width(8.dp))
                Text("Return to Simulator", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                Text("Restart training mission")
            }
        }
    }
}

@Composable
private fun DashboardMetric(title: String, value: String, modifier: Modifier) {
    SchoolCard(modifier) {
        Eyebrow(title)
        Text(value, fontWeight = FontWeight.Black, color = DeepSteel, fontSize = 21.sp)
    }
}

@Composable
private fun ChartLegend(color: Color, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(4.dp))
        Text(title, color = SchoolMuted, fontSize = 10.sp)
    }
}

/** Large colored cross-section, fictional training geometry; not a bore plan. */
@Composable
fun TrainingBoreCanvas(session: SimState, height: Dp) {
    Canvas(Modifier.fillMaxWidth().height(height).background(
        Color(0xFFEAF3F7), RoundedCornerShape(13.dp))) {
        val w = size.width
        val h = size.height
        val pad = 16.dp.toPx()
        val ground = h * .22f
        val drawableBottom = h - 22.dp.toPx()
        fun x(ft: Double): Float = pad + (ft / session.mission.totalFeet).toFloat() * (w - pad * 2)
        fun y(d: Double): Float = ground + (d.coerceIn(0.0, 12.0) / 12.0).toFloat() * (drawableBottom - ground)
        drawRect(Color(0xFFCAEBFF), size = Size(w, ground))
        drawCircle(Color(0xFFFFD96A), radius = 13.dp.toPx(), center = Offset(w * .82f, ground * .33f))
        drawRect(Color(0xFFEBD0A8), topLeft = Offset(0f, ground),
            size = Size(w, h - ground))
        // Decorative soil strata, not measured geology.
        listOf(.43f, .61f, .79f).forEachIndexed { i, fraction ->
            drawLine(
                if (i % 2 == 0) Color(0xFFCFAD82) else Color(0xFFD6B791),
                Offset(0f, h * fraction), Offset(w, h * fraction),
                strokeWidth = 5.dp.toPx()
            )
        }
        drawLine(Color(0xFF619B61), Offset(0f, ground), Offset(w, ground),
            strokeWidth = 4.dp.toPx())
        for (n in 0..4) {
            val xx = pad + (w - 2 * pad) * n / 4f
            drawLine(Color(0x44FFFFFF), Offset(xx, ground), Offset(xx, drawableBottom), 1.dp.toPx())
        }
        for (n in 0..4) {
            val yy = ground + (drawableBottom - ground) * n / 4f
            drawLine(Color(0x33FFFFFF), Offset(pad, yy), Offset(w-pad, yy), 1.dp.toPx())
        }
        // Smoothed example design/reference, not a real engineering plan.
        val design = Path()
        (0..30).forEach { index ->
            val s = index / 30.0
            val depth = 0.75 + (session.mission.exitDepthFeet - .75) * s +
                2.0 * sin(PI * s).coerceAtLeast(0.0)
            if (index == 0) design.moveTo(x(0.0), y(depth))
            else design.lineTo(x(session.mission.totalFeet * s), y(depth))
        }
        drawPath(design, Color(0xFF879BA6), style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
        session.mission.utilities.forEach { utility ->
            val loc = Offset(x(utility.stationFeet), y(utility.depthFeet))
            drawCircle(Color(0x55E1483C), radius = 15.dp.toPx(), center = loc)
            drawCircle(Color(0xFFC84334), radius = 9.dp.toPx(), center = loc,
                style = Stroke(2.5.dp.toPx()))
            drawLine(Color(0xFFC84334), loc.copy(x = loc.x - 5.dp.toPx()),
                loc.copy(x = loc.x + 5.dp.toPx()), 2.dp.toPx())
        }
        val exit = Offset(x(session.mission.totalFeet), y(session.mission.exitDepthFeet))
        drawCircle(SafeGreen, radius = 10.dp.toPx(), center = exit,
            style = Stroke(3.dp.toPx()))
        drawCircle(SafeGreen, radius = 3.dp.toPx(), center = exit)
        val path = Path()
        session.shots.forEachIndexed { index, shot ->
            if (index == 0) path.moveTo(x(shot.stationFeet), y(shot.depthFeet))
            else path.lineTo(x(shot.stationFeet), y(shot.depthFeet))
        }
        drawPath(path, Color(0xFFFF8100), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
        val head = session.shots.last()
        val headAt = Offset(x(head.stationFeet), y(head.depthFeet))
        drawCircle(DeepSteel, radius = 9.dp.toPx(), center = headAt)
        drawCircle(Color.White, radius = 3.dp.toPx(), center = headAt)
    }
}
