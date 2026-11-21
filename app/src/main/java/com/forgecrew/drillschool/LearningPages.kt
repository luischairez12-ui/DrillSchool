package com.forgecrew.drillschool

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class SkillLesson(
    val id: String,
    val title: String,
    val description: String,
    val teaching: String,
    val question: String,
    val choices: List<String>,
    val answerIndex: Int,
    val explanation: String
)

private val lessons = listOf(
    SkillLesson("clock", "Clock-face steering", "Toolface orientation and direction",
        "In common HDD walkover steering, a clock-face representation communicates drill-head orientation. With the intended reference, 12 typically denotes up, 3 right, 6 down and 9 left. Actual response depends on the tool and ground, so the game exaggerates the result.",
        "Which clock position would you select to practice an upward correction in the simulation?",
        listOf("12 o'clock", "3 o'clock", "6 o'clock"), 0,
        "12 o'clock steers up in DrillSchool's model. Always verify readings and procedures on actual equipment."),
    SkillLesson("rotate", "Rotate vs steer", "When rotation changes the training outcome",
        "The training model represents rotating and advancing as maintaining the current path inclination, while orienting the face and pushing without continuous rotation creates a steering correction. This is an educational simplification, not calibrated equipment behavior.",
        "Which DrillSchool control keeps the current pitch unchanged?",
        listOf("Steer at 6", "Rotate and advance", "Steer at 12"), 1,
        "Rotate holds pitch in the simulator. Real performance depends on drill design and conditions."),
    SkillLesson("pitch", "Reading pitch", "Inclination and depth estimates",
        "A locator can report pitch as a percentage slope or an angle, depending on equipment. In our model, positive pitch increases depth when advancing and negative pitch brings the drill head upward.",
        "A negative pitch in this simulator means the head is trending...",
        listOf("deeper", "toward the surface", "to the right"), 1,
        "A negative simulated pitch moves the path toward the surface over the next advance."),
    SkillLesson("sonde", "Locator fundamentals", "Pitch, roll, depth and interference",
        "Walkover guidance uses a transmitter in the drill head and a receiver above ground. The locator and driller work as a team to interpret roll, pitch, position and depth. Interference, conditions and tool calibration matter.",
        "What is a key purpose of a walkover receiver?",
        listOf("To measure truck fuel", "To locate and interpret a drill-head signal", "To replace utility locating"), 1,
        "The receiver helps identify transmitter position and orientation. It does not replace utility locating."),
    SkillLesson("safety", "Underground utility safety", "Real clearances are never set by this game",
        "The colored obstacle bubbles in DrillSchool are fictional game zones. They do not represent legal separation distances, actual utilities or approval to drill. Real work requires local procedures, proper utility marks, verification, communication and all applicable safeguards.",
        "Can DrillSchool's game clearance radius be used as a real utility clearance?",
        listOf("Yes, always", "Only on short shots", "No, it is fictional"), 2,
        "Correct. Never use the simulator to decide real utility clearances or authorize a bore."),
    SkillLesson("crew", "Crew communication", "Passing information between locator and rig",
        "Bore plans and readings change during a job. Crew members need clear communication of current position, pitch, roll, next action and hazards. The simulator's shot log can help practice noticing trends between steps.",
        "Before a steering adjustment, the team should first...",
        listOf("verify the current reading and planned direction", "skip the locator", "ignore changes in pitch"), 0,
        "Verify the current readings, target and risks before deciding what to do next.")
)

@Composable
fun LessonsPage(prefs: SharedPreferences) {
    var activeLesson by remember { mutableStateOf<SkillLesson?>(null) }
    var completionVersion by remember { mutableIntStateOf(0) }
    val completed = remember(completionVersion) {
        prefs.getStringSet("completed_lessons", emptySet())?.toSet() ?: emptySet()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Eyebrow("DRILLSCHOOL ACADEMY")
            Text("Learn the basics", style = MaterialTheme.typography.headlineMedium,
                color = DeepSteel, fontWeight = FontWeight.Black)
            SubText("Short field-inspired lessons, with a quiz after each one.")
        }
        item {
            SchoolCard {
                Eyebrow("LESSON PROGRESS")
                Text("${completed.size} / ${lessons.size} complete",
                    fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                LinearProgressIndicator(progress = { completed.size.toFloat() / lessons.size },
                    modifier = Modifier.fillMaxWidth().height(7.dp), color = SafeGreen,
                    trackColor = SchoolLine)
            }
        }
        items(lessons.size) { index ->
            val lesson = lessons[index]
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = OrangePale, shape = RoundedCornerShape(14.dp)) {
                        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                            Icon(if (lesson.id in completed) Icons.Outlined.CheckCircle
                                else Icons.Outlined.School, null,
                                tint = if (lesson.id in completed) SafeGreen else OrangeDark)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("0${index + 1} • ${lesson.title}", fontWeight = FontWeight.Bold,
                            color = DeepSteel)
                        SubText(lesson.description)
                    }
                }
                OutlinedButton(onClick = { activeLesson = lesson },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                    Text(if (lesson.id in completed) "Review lesson" else "Start lesson")
                }
            }
        }
        item { SubText("Lesson content is for awareness and practice. Follow training provided by your employer, locator manufacturer and local utility-safety requirements.") }
    }

    val chosen = activeLesson
    if (chosen != null) {
        var selected by remember(chosen.id) { mutableIntStateOf(-1) }
        var checked by remember(chosen.id) { mutableStateOf(false) }
        val correct = checked && selected == chosen.answerIndex
        AlertDialog(
            onDismissRequest = { activeLesson = null },
            title = { Text(chosen.title) },
            text = {
                Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubText(chosen.teaching)
                    HorizontalDivider(color = SchoolLine)
                    Text(chosen.question, fontWeight = FontWeight.Bold)
                    chosen.choices.forEachIndexed { index, choice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selected == index, onClick = {
                                selected = index; checked = false
                            })
                            Text(choice, modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (checked) Text(if (correct) "Correct! ${chosen.explanation}"
                        else "Not quite — try again. ${chosen.explanation}",
                        color = if (correct) SafeGreen else OrangeDark)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (!correct) checked = true
                    else {
                        val updated = completed + chosen.id
                        prefs.edit().putStringSet("completed_lessons", updated).apply()
                        completionVersion++
                        activeLesson = null
                    }
                }, enabled = selected >= 0) { Text(if (correct) "Finish" else "Check answer") }
            },
            dismissButton = { TextButton(onClick = { activeLesson = null }) { Text("Close") } }
        )
    }
}

@Composable
fun TrainingProgressPage(bestScores: Map<String, Int>, onPractice: (Int) -> Unit) {
    val done = bestScores.values.count { it > 0 }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            Eyebrow("TRAINING RECORD")
            Text("Your progress", style = MaterialTheme.typography.headlineMedium,
                color = DeepSteel, fontWeight = FontWeight.Black)
            SubText("Personal training scores, saved locally on this phone.")
        }
        item {
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.EmojiEvents, null, tint = OrangeDark, modifier = Modifier.size(45.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Eyebrow("MISSIONS CLEARED")
                        Text("$done / ${trainingMissions.size}",
                            fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineMedium)
                    }
                }
                LinearProgressIndicator(progress = { done.toFloat() / trainingMissions.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp), color = OrangeDark,
                    trackColor = OrangePale)
            }
        }
        items(trainingMissions.size) { index ->
            val mission = trainingMissions[index]
            val best = bestScores[mission.id] ?: 0
            SchoolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("MISSION 0${index + 1}")
                        Text(mission.title.substringAfter(" • "), fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium)
                        SubText("${oneDecimal(mission.totalFeet)} ft simulated course")
                    }
                    Text(if (best == 0) "Not cleared" else "$best/100",
                        color = if (best == 0) SchoolMuted else SafeGreen,
                        fontWeight = FontWeight.Black)
                }
                OutlinedButton(onClick = { onPractice(index) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.PlayArrow, null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (best == 0) "Start mission" else "Practice again")
                }
            }
        }
        item {
            SchoolCard {
                Eyebrow("NEXT STAGES")
                Text("Future training bays", fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge)
                listOf("Dedicated locator role and reading simulations",
                    "3D bore alignment with turn radius and configurable soil",
                    "Pullback, reamer and drilling-fluid modules",
                    "Crew communication and guided assessments").forEach { entry ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.Schedule, null, tint = SchoolMuted,
                            modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(8.dp))
                        SubText(entry)
                    }
                }
            }
        }
        item { SubText("Training achievements are local game records, not operator certification.") }
    }
}
