package com.forgecrew.drillschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Subsite-inspired alternative training composition. This is intentionally
 * *not* a recolor of the DCI clock-and-reticle screen: it centers on a
 * line/grade guidance picture, with separate pitch/roll and station panels.
 * All readings are game-generated and must never guide a real HDD bore.
 */
@Composable
fun SubsiteTrainingDisplay(
    session: SimState,
    threeD: Boolean,
    modifier: Modifier = Modifier
) {
    val value = trainingGuidanceReading(session)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        color = Color(0xFFFAFCFB)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(28.dp)
                    .padding(horizontal = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SUBSITE-STYLE • DRILL-TO",
                    color = Color(0xFF277488), fontSize = 10.sp,
                    fontWeight = FontWeight.Black)
                Text(if (threeD) "3D LINE GUIDANCE" else "2D GRADE VIEW",
                    color = SchoolMuted, fontSize = 9.sp,
                    fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = Color(0xFFCBDCE1))
            Row(
                Modifier.fillMaxWidth().weight(1f).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Column(
                    Modifier.width(85.dp).fillMaxHeight(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("PITCH", fontSize = 9.sp, color = SchoolMuted,
                        fontWeight = FontWeight.Black)
                    Text(signedTrainingPercent(value.pitchPercent),
                        fontSize = 22.sp, color = DeepSteel,
                        fontWeight = FontWeight.Black, maxLines = 1)
                    Text("DEPTH", fontSize = 9.sp, color = SchoolMuted,
                        fontWeight = FontWeight.Black)
                    Text(oneDecimal(value.depthFeet) + " ft",
                        fontSize = 16.sp, color = DeepSteel,
                        fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Surface(
                        color = Color(0xFFE7F4F1),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("TOOLFACE", fontSize = 8.sp, color = SchoolMuted,
                                fontWeight = FontWeight.Bold)
                            Text(value.clock.toString() + ":00",
                                fontSize = 17.sp, color = Color(0xFF16806F),
                                fontWeight = FontWeight.Black)
                        }
                    }
                }
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        SubsiteBoreGuidanceCanvas(
                            session = session, threeD = threeD,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        if (threeD) "MODEL PATH • PERSPECTIVE"
                        else "MODEL PATH • PROFILE",
                        color = Color(0xFF37788C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(5.dp))
                }
                Column(
                    Modifier.width(58.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    SubsiteStatus("SIGNAL", "SIM")
                    SubsiteStatus("POWER", "—")
                    SubsiteStatus("TEMP", "—")
                    SubsiteStatus("PSI", "—")
                }
            }
            Surface(color = Color(0xFFF1F3EA)) {
                Row(
                    Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SlimGuidanceMetric(
                        "STATION", oneDecimal(session.stationFeet) + " ft",
                        Modifier.weight(1f)
                    )
                    VerticalDivider(
                        modifier = Modifier.height(28.dp),
                        color = Color(0xFFCAD4CB)
                    )
                    SlimGuidanceMetric(
                        "GRADE Δ", signedTrainingFeet(value.depthDifferenceAtExitFeet),
                        Modifier.weight(1f)
                    )
                    VerticalDivider(
                        modifier = Modifier.height(28.dp),
                        color = Color(0xFFCAD4CB)
                    )
                    SlimGuidanceMetric(
                        "SIDE Δ", signedTrainingFeet(value.lateralDifferenceAtExitFeet),
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubsiteStatus(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFE9EEF0)
    ) {
        Column(
            Modifier.padding(horizontal = 2.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = SchoolMuted,
                fontSize = 8.sp, fontWeight = FontWeight.Bold,
                maxLines = 1)
            Text(value, color = Color(0xFF277A71),
                fontSize = 11.sp, fontWeight = FontWeight.Black,
                maxLines = 1)
        }
    }
}

@Composable
private fun SubsiteBoreGuidanceCanvas(
    session: SimState,
    threeD: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas
        val grid = Color(0xFFCED8DA)
        val centerLine = Color(0xFF376B7A)
        val start = Offset(w * .09f, h * .66f)
        val target = Offset(w * .87f, h * .33f)
        if (threeD) {
            // Stylized perspective corridor is a training visualization.
            val horizon = Offset(w * .54f, h * .22f)
            listOf(0f, .25f, .5f, .75f, 1f).forEach { tick ->
                drawLine(
                    grid, horizon,
                    Offset(w * tick, h * .93f),
                    1.dp.toPx()
                )
            }
            listOf(.47f, .66f, .85f).forEach { fraction ->
                drawLine(grid, Offset(0f, h * fraction),
                    Offset(w, h * fraction), 1.dp.toPx())
            }
        } else {
            for (i in 0..5) {
                val y = (i + 1) * h / 7f
                drawLine(grid, Offset(0f, y), Offset(w, y),
                    1.dp.toPx())
            }
            for (i in 0..5) {
                val x = (i + 1) * w / 7f
                drawLine(grid, Offset(x, 0f), Offset(x, h),
                    1.dp.toPx())
            }
        }

        val reference = Path().apply {
            moveTo(start.x, start.y)
            lineTo(target.x, target.y)
        }
        drawPath(reference, centerLine, style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(7.dp.toPx(), 5.dp.toPx())
            )
        ))

        // Actual (fictional) bore path, scaled into a separate tracking diagram.
        val steps = session.shots
        if (steps.isNotEmpty()) {
            val total = session.mission.totalFeet.coerceAtLeast(1.0)
            val bore = Path()
            steps.forEachIndexed { index, shot ->
                val progress = (shot.stationFeet / total).toFloat()
                    .coerceIn(0f, 1f)
                val z = (shot.depthFeet / 12.0).toFloat()
                    .coerceIn(0f, 1f)
                val x = w * (.09f + .78f * progress)
                val y = h * (.27f + .6f * z)
                if (index == 0) bore.moveTo(x, y)
                else bore.lineTo(x, y)
            }
            drawPath(bore, Color(0xFFE8841E),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            val last = steps.last()
            val progress = (last.stationFeet / total).toFloat()
                .coerceIn(0f, 1f)
            val z = (last.depthFeet / 12.0).toFloat()
                .coerceIn(0f, 1f)
            val position = Offset(w * (.09f + .78f * progress), h * (.27f + .6f * z))
            drawCircle(Color(0xFFED7C20), 7.dp.toPx(), position)
            drawCircle(Color.White, 3.dp.toPx(), position)
        }
        drawCircle(Color(0xFF47A37D), 9.dp.toPx(), target,
            style = Stroke(2.dp.toPx()))
        drawCircle(Color(0xFF47A37D), 3.dp.toPx(), target)
        drawLine(centerLine, Offset(target.x, target.y - 15.dp.toPx()),
            Offset(target.x, target.y + 15.dp.toPx()),
            1.dp.toPx())
        drawLine(centerLine, Offset(target.x - 15.dp.toPx(), target.y),
            Offset(target.x + 15.dp.toPx(), target.y),
            1.dp.toPx())
    }
}
