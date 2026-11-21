package com.forgecrew.drillschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Dedicated DigiTrak-inspired training interpretation:
 * left 12-position toolface + pitch, central target reticle,
 * right telemetry strip and horizontal/vertical offset ruler.
 * Not copied from DCI firmware and contains no real measurements.
 */
@Composable
fun DigiTrakTrainingDisplay(
    session: SimState,
    threeD: Boolean,
    modifier: Modifier = Modifier
) {
    val value = trainingGuidanceReading(session)
    Surface(modifier, shape = RoundedCornerShape(11.dp), color = Color(0xFFFAFCFD)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(24.dp)
                    .background(Color(0xFF4B98BE))
                    .padding(horizontal = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("F5-STYLE / SIM CH 1", color = Color.White,
                    fontWeight = FontWeight.Black, fontSize = 10.sp)
                Text(if (threeD) "TARGET GUIDANCE • 3D" else "CLASSIC • 2D",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
            Row(
                Modifier.fillMaxWidth().weight(1f).padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Column(
                    Modifier.width(101.dp).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    DigiToolfaceDial(value.clock, Modifier.fillMaxWidth().weight(1f))
                    Text("PITCH", fontSize = 9.sp, color = SchoolMuted,
                        fontWeight = FontWeight.Bold)
                    Text(signedTrainingPercent(value.pitchPercent),
                        fontSize = 21.sp, color = DeepSteel,
                        fontWeight = FontWeight.Black, maxLines = 1)
                    Text("DEPTH " + oneDecimal(value.depthFeet) + " ft",
                        fontSize = 10.sp, color = DeepSteel,
                        fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                }
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Box(Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center) {
                        DigiTargetReticle(value, threeD, Modifier.fillMaxSize())
                    }
                    Text(
                        "PROJECTED EXIT • GAME MODEL",
                        fontSize = 8.sp, color = SchoolMuted,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(5.dp))
                }
                Column(
                    Modifier.width(55.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    DigiStatus("LINK", "SIM", true)
                    DigiStatus("BATT", "—")
                    DigiStatus("TEMP", "—")
                    DigiStatus("PSI", "—")
                }
            }
            Surface(color = Color(0xFFE2F0F8)) {
                Row(
                    Modifier.fillMaxWidth().height(45.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SlimGuidanceMetric(
                        "H OFFSET", signedTrainingFeet(value.lateralDifferenceAtExitFeet),
                        Modifier.weight(1f)
                    )
                    VerticalDivider(Modifier.height(29.dp), color = Color(0xFFB6CEDD))
                    SlimGuidanceMetric(
                        "V OFFSET", signedTrainingFeet(value.depthDifferenceAtExitFeet),
                        Modifier.weight(1f)
                    )
                    VerticalDivider(Modifier.height(29.dp), color = Color(0xFFB6CEDD))
                    SlimGuidanceMetric(
                        "TO EXIT", oneDecimal(value.distanceToExitFeet) + " ft",
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DigiStatus(label: String, value: String, live: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (live) Color(0xFFE5F4E6) else Color(0xFFEDF1F4),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(vertical = 5.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 8.sp,
                fontWeight = FontWeight.Bold, color = SchoolMuted,
                maxLines = 1)
            Text(value, fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (live) Color(0xFF388548) else DeepSteel,
                maxLines = 1)
        }
    }
}

@Composable
private fun DigiToolfaceDial(clock: Int, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val middle = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) * 0.45f
            if (radius < 5f) return@Canvas
            drawCircle(Color(0xFFD6E4EC), radius, middle)
            drawCircle(Color(0xFF657581), radius, middle,
                style = Stroke(2.dp.toPx()))
            drawCircle(Color(0xFFFEFEFD), radius * .67f, middle)
            for (index in 0 until 12) {
                val angle = -PI / 2 + index * PI / 6
                drawLine(
                    Color(0xFF687A84),
                    Offset(
                        middle.x + cos(angle).toFloat() * radius * .77f,
                        middle.y + sin(angle).toFloat() * radius * .77f
                    ),
                    Offset(
                        middle.x + cos(angle).toFloat() * radius * .94f,
                        middle.y + sin(angle).toFloat() * radius * .94f
                    ),
                    if (index % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
                )
            }
            val position = -PI / 2 + (clock % 12) * PI / 6
            drawLine(
                Color(0xFFE14A34), middle,
                Offset(
                    middle.x + cos(position).toFloat() * radius * .83f,
                    middle.y + sin(position).toFloat() * radius * .83f
                ),
                5.dp.toPx(), cap = StrokeCap.Round
            )
        }
        Text(clock.toString() + ":00",
            fontSize = 20.sp,
            color = DeepSteel,
            fontWeight = FontWeight.Black)
        Text("12", modifier = Modifier.align(Alignment.TopCenter)
            .padding(top = 7.dp), fontSize = 9.sp,
            fontWeight = FontWeight.Black, color = DeepSteel)
        Text("6", modifier = Modifier.align(Alignment.BottomCenter)
            .padding(bottom = 7.dp), fontSize = 9.sp,
            fontWeight = FontWeight.Black, color = DeepSteel)
    }
}

@Composable
private fun DigiTargetReticle(
    value: TrainingGuidanceReading, threeD: Boolean, modifier: Modifier
) {
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) * .43f
        if (radius < 5f) return@Canvas
        val frame = Color(0xFF263A44)
        if (threeD) {
            // Concentric perspective lines for a game-only 3D guidance option.
            listOf(1f, .76f, .53f).forEach { fraction ->
                drawOval(
                    color = Color(0xFF93A6B2),
                    topLeft = Offset(
                        center.x - radius * fraction,
                        center.y - radius * fraction * .6f
                    ),
                    size = Size(radius * fraction * 2, radius * fraction * 1.2f),
                    style = Stroke(1.dp.toPx())
                )
            }
            listOf(-.8f, 0f, .8f).forEach { x ->
                drawLine(
                    Color(0xFFDAE3E9),
                    Offset(center.x + radius * x * .35f, center.y - radius),
                    Offset(center.x + radius * x, center.y + radius),
                    1.dp.toPx()
                )
            }
        } else {
            drawCircle(frame, radius, center, style = Stroke(2.dp.toPx()))
            drawCircle(Color(0xFFC1CCD6), radius * .56f, center,
                style = Stroke(1.dp.toPx()))
        }
        drawLine(frame,
            Offset(center.x - radius, center.y),
            Offset(center.x + radius, center.y),
            1.6.dp.toPx())
        drawLine(Color(0xFFD84035),
            Offset(center.x, center.y - radius),
            Offset(center.x, center.y + radius),
            2.8.dp.toPx())
        val position = Offset(
            center.x + value.targetX * radius * .76f,
            center.y + value.targetY * radius * .76f
        )
        drawCircle(Color(0x665B80DC), radius * .22f, position)
        drawCircle(Color(0xFF426DCF), radius * .09f, position)
        drawCircle(Color(0xFF1D3C8E), radius * .035f, position)
        drawCircle(Color(0xFFF38318), radius * .06f, center,
            style = Stroke(2.dp.toPx()))
    }
}
