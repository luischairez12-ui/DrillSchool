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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * Independent MAG 9-inspired educational layout.
 * Clock, pitch, depth, projected depth and rod count come ONLY from the
 * fictional engine. Not original Underground Magnetics software.
 */
@Composable
fun Mag9TrainingDisplay(
    session: SimState,
    threeD: Boolean,
    modifier: Modifier = Modifier
) {
    val m = trainingGuidanceReading(session)
    Surface(
        modifier = modifier,
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(11.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(6.dp)) {
            Row(
                Modifier.fillMaxWidth().height(25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("MAG 9-STYLE / TRAINING", fontWeight = FontWeight.Black,
                    color = DeepSteel, fontSize = 10.sp)
                Text("ROD " + m.rodNumber, fontSize = 11.sp,
                    color = OrangeDark, fontWeight = FontWeight.Black)
            }
            HorizontalDivider(color = SchoolLine)
            Row(
                Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Column(
                    Modifier.width(91.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    MagMetric("TOOLFACE", m.toolfaceLabel)
                    MagMetric("PITCH", signedTrainingPercent(m.pitchPercent))
                    MagMetric("CURRENT", feetAndInches(m.depthFeet))
                }
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SchoolLine)
                ) {
                    Column(
                        Modifier.padding(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (threeD) "TARGET • PERSPECTIVE" else "CENTERING TARGET",
                            color = SchoolMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Mag9Target(
                            reading = m, perspective = threeD,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                        val aligned = kotlin.math.abs(m.verticalErrorAtLocatorFeet) < .15 &&
                            kotlin.math.abs(m.lateralErrorAtLocatorFeet) < .15
                        Text(
                            if (aligned) "CENTERED • GAME TARGET" else "ALIGN THE TARGET",
                            color = if (aligned) SafeGreen else OrangeDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Column(
                    Modifier.width(84.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    MagMetric("PROJECTED", feetAndInches(m.projectedDepthAtLocatorFeet))
                    MagMetric("AT LOCATOR", feetAndInches(m.targetDepthAtLocatorFeet))
                    MagMetric("DISTANCE", feetAndInches(m.distanceToLocatorFeet))
                }
            }
            Surface(color = Color(0xFFEAF0F2), shape = RoundedCornerShape(8.dp)) {
                Row(
                    Modifier.fillMaxWidth().height(42.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SlimGuidanceMetric(
                        "MODEL V Δ", signedTrainingFeet(m.verticalErrorAtLocatorFeet),
                        Modifier.weight(1f)
                    )
                    VerticalDivider(Modifier.height(23.dp), color = SchoolLine)
                    SlimGuidanceMetric(
                        "MODEL H Δ", signedTrainingFeet(m.lateralErrorAtLocatorFeet),
                        Modifier.weight(1f)
                    )
                    VerticalDivider(Modifier.height(23.dp), color = SchoolLine)
                    SlimGuidanceMetric(
                        "LOCATOR ST", oneDecimal(m.locatorStationFeet) + " ft",
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MagMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 8.sp,
            fontWeight = FontWeight.Bold, color = SchoolMuted, maxLines = 1)
        Text(value, fontSize = 15.sp,
            fontWeight = FontWeight.Black, color = DeepSteel, maxLines = 1)
    }
}

@Composable
private fun Mag9Target(
    reading: TrainingGuidanceReading,
    perspective: Boolean,
    modifier: Modifier
) {
    Canvas(modifier) {
        val mid = Offset(size.width / 2f, size.height / 2f)
        val r = min(size.width, size.height) * .39f
        if (r < 4f) return@Canvas
        val dark = Color(0xFF4A5F69)
        if (perspective) {
            for (i in 1..3) {
                val fraction = i / 3f
                drawOval(
                    color = Color(0xFFBCC9CC),
                    topLeft = Offset(mid.x - r * fraction, mid.y - r * fraction * .65f),
                    size = androidx.compose.ui.geometry.Size(
                        2 * r * fraction, 2 * r * fraction * .65f
                    ),
                    style = Stroke(1.dp.toPx())
                )
            }
        } else {
            drawCircle(dark, r, mid, style = Stroke(2.dp.toPx()))
            drawCircle(Color(0xFFCDDBE1), r * .5f, mid, style = Stroke(1.dp.toPx()))
        }
        drawLine(dark, Offset(mid.x - r, mid.y), Offset(mid.x + r, mid.y),
            1.5.dp.toPx())
        drawLine(Color(0xFFCE5548), Offset(mid.x, mid.y - r),
            Offset(mid.x, mid.y + r), 2.4.dp.toPx())
        val marker = Offset(
            mid.x + reading.targetX * r * .75f,
            mid.y + reading.targetY * r * .75f
        )
        drawCircle(Color(0x4496A6FF), r * .23f, marker)
        drawCircle(Color(0xFF3E5DCA), r * .09f, marker)
        drawCircle(Color.White, r * .029f, marker)
        drawCircle(OrangeDark, r * .052f, mid, style = Stroke(2.dp.toPx()))
    }
}
