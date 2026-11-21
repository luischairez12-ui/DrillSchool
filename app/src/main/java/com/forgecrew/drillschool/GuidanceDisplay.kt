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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

/**
 * Visual training mock of a rig-side guidance display. Never claims to reproduce
 * original DigiTrak / Subsite firmware or represent live receiver telemetry.
 */
@Composable
fun GuidanceDisplayPanel(
    session: SimState,
    style: GuidanceStyle,
    threeD: Boolean,
    onViewChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF182B35),
        shape = RoundedCornerShape(19.dp),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.fillMaxSize().padding(7.dp)) {
            Row(
                Modifier.fillMaxWidth().height(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (style == GuidanceStyle.DIGITRAK) "F5-STYLE • TRAINING"
                    else "SUBSITE-STYLE • TRAINING",
                    color = Color.White, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row {
                    listOf(false, true).forEach { dimension ->
                        val selected = threeD == dimension
                        Surface(
                            onClick = { onViewChange(dimension) },
                            color = if (selected) OrangeDark else Color(0xFF304452),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (dimension) "3D" else "2D",
                                color = Color.White, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(
                    Modifier.width(76.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    GuidanceMetric("DEPTH", oneDecimal(session.depthFeet) + " ft")
                    GuidanceMetric("PITCH", oneDecimal(session.pitchPercent) + "%")
                    GuidanceMetric("CLOCK", session.lastClock.toString() + ":00")
                    GuidanceMetric("DISTANCE", oneDecimal(
                        (session.mission.totalFeet - session.stationFeet).coerceAtLeast(0.0)
                    ) + " ft")
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .background(Color(0xFFD5EFF9), RoundedCornerShape(9.dp))
                ) {
                    TrainingGuidanceCanvas(session, threeD, Modifier.fillMaxSize())
                }
                Column(
                    Modifier.width(68.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    GuidanceMetric("LINK", "SIM")
                    GuidanceMetric("TX TEMP", "—")
                    GuidanceMetric("MUD PSI", "—")
                    GuidanceMetric("FLOW", "—")
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth().height(23.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NOT LIVE TELEMETRY", color = Color(0xFFFFC389),
                    fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    "ROD " + session.rodNumber +
                        "  •  " + oneDecimal(session.stationFeet) + " ft",
                    color = Color.White, fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun GuidanceMetric(title: String, value: String) {
    Column {
        Text(title, color = Color(0xFFADC1CA),
            fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(value, color = Color.White,
            fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TrainingGuidanceCanvas(
    session: SimState,
    threeD: Boolean,
    modifier: Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val horizon = h * 0.29f
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFBCE7FF), Color(0xFFE6F5FF)),
                startY = 0f, endY = horizon
            ),
            size = Size(w, horizon)
        )
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFE0B587), Color(0xFFB98658), Color(0xFF946642)),
                startY = horizon, endY = h
            ),
            topLeft = Offset(0f, horizon),
            size = Size(w, h - horizon)
        )
        drawLine(
            Color(0xFF70A960),
            Offset(0f, horizon), Offset(w, horizon),
            strokeWidth = 3.dp.toPx()
        )

        if (threeD) {
            // A stylized perspective training view, not OEM 3D target steering.
            val vanishing = Offset(w * .53f, horizon)
            listOf(0f, .25f, .5f, .75f, 1f).forEach { t ->
                drawLine(Color(0x66FFF0DD), vanishing,
                    Offset(w * t, h), strokeWidth = 1.dp.toPx())
            }
            listOf(.48f, .66f, .83f).forEach { t ->
                val y = h * t
                drawLine(Color(0x33FFF0DD), Offset(0f, y),
                    Offset(w, y), strokeWidth = 1.dp.toPx())
            }
        } else {
            listOf(.47f, .68f, .85f).forEach { y ->
                drawLine(
                    Color(0x559E6F49),
                    Offset(0f, h * y), Offset(w, h * y),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        val total = max(session.mission.totalFeet, 1.0)
        fun position(station: Double, depth: Double, lateral: Double): Offset {
            val x = (station / total).toFloat().coerceIn(0f, 1f)
            val y = (depth.coerceIn(0.0, 12.0) / 12.0).toFloat()
            return if (threeD) {
                Offset(
                    w * (.10f + .82f * x),
                    h * (.39f + .42f * y + lateral.toFloat() * .005f)
                )
            } else {
                Offset(
                    w * (.06f + .88f * x),
                    h * (.34f + .60f * y)
                )
            }
        }

        val target = position(
            session.mission.totalFeet,
            session.mission.exitDepthFeet,
            session.mission.exitLateralFeet
        )
        val first = session.shots.firstOrNull()
        if (first != null) {
            val guide = Path()
            val from = position(first.stationFeet, first.depthFeet, first.lateralFeet)
            guide.moveTo(from.x, from.y)
            guide.lineTo(target.x, target.y)
            drawPath(
                guide, Color(0xFFFCF6EB),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(7.dp.toPx(), 6.dp.toPx())
                    )
                )
            )

            val bore = Path()
            session.shots.forEachIndexed { index, shot ->
                val p = position(shot.stationFeet, shot.depthFeet, shot.lateralFeet)
                if (index == 0) bore.moveTo(p.x, p.y)
                else bore.lineTo(p.x, p.y)
            }
            drawPath(bore, Color(0xFFF47D0C),
                style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))

            val last = session.shots.last()
            val current = position(last.stationFeet, last.depthFeet, last.lateralFeet)
            drawCircle(DeepSteel, 7.dp.toPx(), current)
            drawCircle(OrangeDark, 3.dp.toPx(), current)
        }
        drawCircle(Color.White, 9.dp.toPx(), target)
        drawCircle(OrangeDark, 7.dp.toPx(), target,
            style = Stroke(2.5.dp.toPx()))
        drawCircle(OrangeDark, 2.5.dp.toPx(), target)
    }
}
