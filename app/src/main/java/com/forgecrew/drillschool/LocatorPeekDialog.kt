package com.forgecrew.drillschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Handheld receiver peek inspired by the user's field photographs.
 * No sensor data is read; all values are from the local fictional mission.
 */
@Composable
fun LocatorPeekDialog(
    session: SimState,
    style: GuidanceStyle,
    onDismiss: () -> Unit
) {
    val reading = trainingGuidanceReading(session)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(.94f),
            color = Color.White,
            shape = RoundedCornerShape(23.dp),
            shadowElevation = 10.dp
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PhonelinkRing, null, tint = OrangeDark)
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Locator Handheld View", color = DeepSteel,
                            fontWeight = FontWeight.Black, fontSize = 19.sp)
                        Text("What the locator sees • simulated",
                            color = SchoolMuted, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, "Close locator view",
                            tint = DeepSteel)
                    }
                }
                Surface(
                    color = if (style == GuidanceStyle.DIGITRAK)
                        Color(0xFFD6EBA8) else Color(0xFFCFD9DE),
                    shape = RoundedCornerShape(21.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("FIELD RECEIVER • TRAINING",
                            color = DeepSteel, fontWeight = FontWeight.Black,
                            fontSize = 10.sp)
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(11.dp),
                            color = Color(0xFFE7ECE6),
                            border = androidx.compose.foundation.BorderStroke(
                                3.dp, Color(0xFF465258))
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("ROLL: " + reading.clock + ":00",
                                        color = DeepSteel, fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.weight(1f))
                                    Text("MODEL DATA", color = SchoolMuted,
                                        fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(6.dp))
                                HandheldGuidanceDiagram(
                                    session = session,
                                    modifier = Modifier.fillMaxWidth().height(110.dp)
                                )
                                Spacer(Modifier.height(5.dp))
                                HorizontalDivider(color = Color(0xFFBAC6C3))
                                Row(
                                    Modifier.fillMaxWidth().padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LocatorValue("PITCH", oneDecimal(reading.pitchPercent) + "%")
                                    LocatorValue("DEPTH", feetAndInches(reading.depthFeet))
                                    LocatorValue("TO EXIT", oneDecimal(reading.distanceToExitFeet) + " ft")
                                }
                            }
                        }
                        Spacer(Modifier.height(9.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            listOf("◀", "▲", "▼", "●").forEach { key ->
                                Surface(
                                    modifier = Modifier.padding(horizontal = 5.dp),
                                    shape = RoundedCornerShape(30.dp),
                                    color = Color(0xFF4B5962)
                                ) {
                                    Text(key, color = Color.White,
                                        fontSize = 13.sp, fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp))
                                }
                            }
                        }
                    }
                }
                Text(
                    "This is a training view inspired by handheld locating equipment, not a live receiver or an exact manufacturer interface. Actual locating requires a calibrated instrument and approved field procedures.",
                    fontSize = 11.sp, lineHeight = 15.sp, color = SchoolMuted
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeDark)
                ) {
                    Text("Back to Operator Seat")
                }
            }
        }
    }
}

@Composable
private fun LocatorValue(name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, fontSize = 9.sp, color = SchoolMuted, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 12.sp, color = DeepSteel, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun HandheldGuidanceDiagram(
    session: SimState,
    modifier: Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val ink = Color(0xFF3E555D)
        drawLine(Color(0xFF9AABA8),
            Offset(0f, h * .18f), Offset(w, h * .18f), 1.dp.toPx())
        drawLine(ink, Offset(w * .08f, h * .76f),
            Offset(w * .93f, h * .76f), 1.dp.toPx())
        val endX = w * .88f
        val endY = h * .42f
        drawLine(ink, Offset(w * .12f, h * .71f),
            Offset(endX, endY), 3.dp.toPx(), cap = StrokeCap.Round)
        val progress = (session.stationFeet / session.mission.totalFeet)
            .toFloat().coerceIn(0f, 1f)
        val tip = Offset(
            w * (.12f + .76f * progress),
            h * (.71f - .29f * progress)
        )
        drawCircle(Color(0xFFDA7B26), 7.dp.toPx(), tip)
        drawCircle(ink, 4.dp.toPx(), tip)
        drawCircle(ink, 6.dp.toPx(), Offset(endX, endY),
            style = Stroke(2.dp.toPx()))
        drawLine(ink, Offset(endX, h * .12f),
            Offset(endX, h * .85f), 1.dp.toPx())
        listOf(.2f, .4f, .6f, .8f).forEach { t ->
            drawLine(Color(0xFFB6C4C0),
                Offset(w * t, h * .22f), Offset(w * t, h * .89f),
                1.dp.toPx())
        }
    }
}
