package com.forgecrew.drillschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * BoreLab-color operator guidance panel modeled after the user's real
 * DCI/Aurora and Subsite reference photographs, with independently drawn UI.
 * It is not their software or a representation of measured telemetry.
 */
@Composable
fun GuidanceDisplayPanel(
    session: SimState,
    style: GuidanceStyle,
    threeD: Boolean,
    onViewChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val reading = trainingGuidanceReading(session)
    val isSubsite = style == GuidanceStyle.SUBSITE
    Surface(
        modifier = modifier,
        color = Color(0xFF2D3B45),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().height(29.dp).padding(start = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isSubsite) "SUBSITE-INSPIRED • OPERATOR"
                    else "DIGITRAK-INSPIRED • OPERATOR",
                    modifier = Modifier.weight(1f),
                    color = Color.White, fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold, maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                listOf(false, true).forEach { mode ->
                    Surface(
                        onClick = { onViewChange(mode) },
                        color = if (mode == threeD) OrangeDark else Color(0xFF53616C),
                        shape = RoundedCornerShape(7.dp),
                        modifier = Modifier.padding(end = 3.dp)
                    ) {
                        Text(
                            if (mode) "3D" else "2D",
                            color = Color.White, fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = if (isSubsite) Color(0xFFFCFCF6) else Color(0xFFF8FAFD),
                shape = RoundedCornerShape(11.dp)
            ) {
                Column(Modifier.fillMaxSize().padding(3.dp)) {
                    Row(
                        Modifier.fillMaxWidth().height(19.dp)
                            .padding(horizontal = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isSubsite) "GUIDANCE • TRAINING" else "SIM / CH —",
                            color = SchoolMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (threeD) "TARGET STEERING • 3D" else "CLASSIC VIEW • 2D",
                            color = OrangeDark, fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Column(
                            Modifier.width(94.dp).fillMaxHeight()
                                .padding(start = 1.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            GuidanceClock(
                                clock = reading.clock, isSubsite = isSubsite,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                            Text(
                                "PITCH " + signedPercent(reading.pitchPercent),
                                color = DeepSteel, fontWeight = FontWeight.Black,
                                fontSize = 12.sp, maxLines = 1
                            )
                            Text(
                                "DEPTH " + oneDecimal(reading.depthFeet) + " ft",
                                color = SchoolMuted, fontSize = 10.sp,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(3.dp))
                        }
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            TrainingTargetView(
                                reading = reading, threeD = threeD,
                                isSubsite = isSubsite,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                "SIMULATED TARGET",
                                modifier = Modifier.align(Alignment.BottomCenter)
                                    .padding(bottom = 3.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = SchoolMuted, maxLines = 1
                            )
                        }
                        Column(
                            modifier = Modifier.width(62.dp).fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GuidanceStatus("LINK", "SIM", true)
                            GuidanceStatus("BATTERY", "—")
                            GuidanceStatus("TEMP", "—")
                            GuidanceStatus("FLUID PSI", "—")
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE0E6EC), thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(31.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GuidanceFooterMetric("MODEL ΔL",
                            signedFeet(reading.lateralDifferenceAtExitFeet), Modifier.weight(1f))
                        GuidanceFooterMetric("MODEL ΔD",
                            signedFeet(reading.depthDifferenceAtExitFeet), Modifier.weight(1f))
                        GuidanceFooterMetric("TO EXIT",
                            oneDecimal(reading.distanceToExitFeet) + " ft", Modifier.weight(1f))
                    }
                }
            }
            Text(
                "ILLUSTRATIVE GAME READINGS • NO LIVE LOCATOR CONNECTION",
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 1.dp),
                fontSize = 8.sp, color = Color(0xFFFFCF9C),
                fontWeight = FontWeight.SemiBold, maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun signedPercent(value: Double): String =
    (if (value > 0.001) "+" else "") + oneDecimal(value) + "%"

private fun signedFeet(value: Double): String =
    (if (value > 0.001) "+" else "") + oneDecimal(value) + " ft"

@Composable
private fun GuidanceFooterMetric(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = SchoolMuted, fontSize = 8.sp,
            fontWeight = FontWeight.Bold, maxLines = 1)
        Text(value, color = DeepSteel, fontSize = 10.sp,
            fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun GuidanceStatus(label: String, value: String, accent: Boolean = false) {
    Surface(
        color = if (accent) Color(0xFFEAF3E5) else Color(0xFFEDF1F4),
        shape = RoundedCornerShape(5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = SchoolMuted, fontSize = 8.sp,
                maxLines = 1, fontWeight = FontWeight.Bold)
            Text(value, color = if (accent) Color(0xFF3D8B4D) else DeepSteel,
                fontWeight = FontWeight.Black, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun GuidanceClock(
    clock: Int,
    isSubsite: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) * 0.43f
            if (radius <= 1f) return@Canvas
            drawCircle(
                if (isSubsite) Color(0xFFECF0ED) else Color(0xFFE9F1F5),
                radius, center
            )
            drawCircle(
                Color(0xFF75818B), radius, center, style = Stroke(2.dp.toPx())
            )
            drawCircle(Color.White, radius * .67f, center)
            for (i in 0 until 12) {
                val angle = -PI / 2.0 + i * PI / 6.0
                val from = Offset(
                    center.x + cos(angle).toFloat() * radius * .77f,
                    center.y + sin(angle).toFloat() * radius * .77f
                )
                val to = Offset(
                    center.x + cos(angle).toFloat() * radius * .94f,
                    center.y + sin(angle).toFloat() * radius * .94f
                )
                drawLine(Color(0xFF5A6771), from, to,
                    if (i % 3 == 0) 2.2.dp.toPx() else 1.1.dp.toPx())
            }
            val angle = -PI / 2.0 + (clock % 12) * PI / 6.0
            drawLine(
                if (isSubsite) Color(0xFF248F6D) else Color(0xFFE64D32),
                center,
                Offset(
                    center.x + cos(angle).toFloat() * radius * .85f,
                    center.y + sin(angle).toFloat() * radius * .85f
                ),
                4.dp.toPx(), cap = StrokeCap.Round
            )
            drawCircle(DeepSteel, 2.6.dp.toPx(), center)
        }
        Text(
            clock.toString() + ":00",
            fontSize = 16.sp,
            color = DeepSteel, fontWeight = FontWeight.Black
        )
        Text(
            "12", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold,
            color = DeepSteel, modifier = Modifier.align(Alignment.TopCenter).padding(top = 5.dp)
        )
        Text(
            "6", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold,
            color = DeepSteel, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp)
        )
    }
}

@Composable
private fun TrainingTargetView(
    reading: TrainingGuidanceReading,
    threeD: Boolean,
    isSubsite: Boolean,
    modifier: Modifier
) {
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) * .39f
        if (radius <= 1f) return@Canvas
        val axis = Color(0xFF364A55)
        if (threeD) {
            // Three-dimensional *styled* target interpretation, not DCI software.
            listOf(1f, .72f, .44f).forEach { fraction ->
                val rx = radius * fraction
                val ry = rx * .61f
                drawOval(
                    color = Color(0xFFAFBEC7),
                    topLeft = Offset(center.x - rx, center.y - ry),
                    size = Size(rx * 2f, ry * 2f),
                    style = Stroke(1.dp.toPx())
                )
            }
            listOf(-.7f, 0f, .7f).forEach { t ->
                drawLine(Color(0xFFCBD4DB),
                    Offset(center.x + radius * t * .4f, center.y - radius * .92f),
                    Offset(center.x + radius * t, center.y + radius * .92f),
                    1.dp.toPx()
                )
            }
        } else {
            drawCircle(axis, radius, center, style = Stroke(1.8.dp.toPx()))
            drawCircle(Color(0xFFCDD5DD), radius * .55f, center,
                style = Stroke(1.dp.toPx()))
        }
        drawLine(axis,
            Offset(center.x - radius, center.y),
            Offset(center.x + radius, center.y),
            1.3.dp.toPx()
        )
        drawLine(
            Color(0xFFD44738),
            Offset(center.x, center.y - radius),
            Offset(center.x, center.y + radius),
            2.4.dp.toPx()
        )
        val marker = Offset(
            center.x + reading.targetX * radius * .68f,
            center.y + reading.targetY * radius * .68f
        )
        val targetColor = if (isSubsite) Color(0xFF4A9C69) else Color(0xFF567FDA)
        drawCircle(targetColor.copy(alpha = .22f), radius * .23f, marker)
        drawCircle(targetColor, radius * .08f, marker)
        drawCircle(DeepSteel, radius * .028f, marker)
        drawCircle(Color(0xFFDF7011), radius * .07f, center,
            style = Stroke(1.5.dp.toPx()))
    }
}
