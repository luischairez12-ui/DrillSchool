package com.forgecrew.drillschool

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { DrillSchoolTheme { DrillSchoolApp() } }
    }
}

@Composable
fun DrillSchoolApp() {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.applicationContext.getSharedPreferences("drillschool_training", Context.MODE_PRIVATE)
    }
    var tab by remember { mutableStateOf("Simulator") }
    var barShotOpen by remember { mutableStateOf(false) }
    var missionIndex by remember { mutableIntStateOf(0) }
    var session by remember { mutableStateOf(SimState(trainingMissions[0])) }
    var lastWinKey by remember { mutableStateOf("") }
    var scoresVersion by remember { mutableIntStateOf(0) }
    var settingsOpen by remember { mutableStateOf(false) }
    var hapticLevel by remember { mutableIntStateOf(prefs.getInt("haptics_level", 2).coerceIn(0, 3)) }
    var voiceEnabled by remember { mutableStateOf(prefs.getBoolean("locator_voice", true)) }
    var rigId by remember { mutableStateOf(prefs.getString("rig_id", "d24x40s3") ?: "d24x40s3") }
    val rig = getRigProfile(rigId)
    var ttsReady by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val locator = LocatorCoach.current(session)
    val scores = remember(scoresVersion) {
        trainingMissions.associate { it.id to prefs.getInt("best_${it.id}", 0) }
    }

    DisposableEffect(context) {
        val speaker = TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
        tts = speaker
        onDispose {
            speaker.stop(); speaker.shutdown()
            tts = null; ttsReady = false
        }
    }
    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            tts?.language = Locale.US
            tts?.setSpeechRate(0.93f)
        }
    }
    fun haptic(cue: FeedbackCue) = TrainingFeedback.play(context, hapticLevel, cue)
    fun speak(message: String, force: Boolean = false) {
        if ((!force && !voiceEnabled) || !ttsReady) return
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "drillschool_locator")
    }
    LaunchedEffect(tab, barShotOpen, locator.key, session.mission.id, ttsReady, voiceEnabled) {
        if (tab == "Simulator" && !barShotOpen && ttsReady && voiceEnabled) {
            speak("Locator Juan. ${locator.message}")
        }
    }
    LaunchedEffect(locator.key, session.mission.id) {
        if (locator.key != "entry") haptic(FeedbackCue.LOCATOR)
    }
    LaunchedEffect(session.phase) {
        when (session.phase) {
            SimPhase.COMPLETED -> haptic(FeedbackCue.COMPLETE)
            SimPhase.FAILED -> haptic(FeedbackCue.ALERT)
            else -> Unit
        }
    }
    fun selectMission(index: Int) {
        missionIndex = index
        session = SimState(trainingMissions[index])
        barShotOpen = false
        lastWinKey = ""
    }
    fun move(mode: SimMode, clock: Int) {
        if (session.phase != SimPhase.RUNNING) return
        session = TrainingEngine.advance(session, mode, clock)
        if (session.phase == SimPhase.COMPLETED) {
            val key = "${session.mission.id}_${session.thrusts}"
            if (lastWinKey != key) {
                lastWinKey = key
                val saveKey = "best_${session.mission.id}"
                if (session.score > prefs.getInt(saveKey, 0)) {
                    prefs.edit().putInt(saveKey, session.score).apply()
                    scoresVersion++
                }
            }
        }
    }
    fun resetMission() {
        session = SimState(trainingMissions[missionIndex])
        lastWinKey = ""
    }

    Scaffold(
        containerColor = SchoolBg,
        topBar = {
            Row(Modifier.fillMaxWidth().background(Color.White)
                .statusBarsPadding().padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(color = OrangeDark, shape = RoundedCornerShape(12.dp)) {
                    Box(Modifier.size(43.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Construction, null, tint = Color.White,
                            modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("DrillSchool", fontSize = 24.sp, fontWeight = FontWeight.Black, color = DeepSteel)
                    Text("FORGECREW STUDIOS • HDD TRAINING", fontSize = 9.sp,
                        letterSpacing = 1.sp, color = OrangeDark, fontWeight = FontWeight.Bold)
                }
                Surface(color = OrangePale, shape = RoundedCornerShape(10.dp)) {
                    Text("V2", modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                        color = OrangeDark, fontWeight = FontWeight.Black)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 2.dp,
                modifier = Modifier.navigationBarsPadding()) {
                listOf(
                    Triple("Simulator", Icons.Outlined.SportsEsports, "Simulator"),
                    Triple("Lessons", Icons.Outlined.MenuBook, "Lessons"),
                    Triple("Missions", Icons.Outlined.Explore, "Missions"),
                    Triple("Progress", Icons.Outlined.EmojiEvents, "Progress")
                ).forEach { (label, icon, destination) ->
                    NavigationBarItem(
                        selected = tab == destination,
                        onClick = { tab = destination; if (destination != "Simulator") barShotOpen = false },
                        icon = { Icon(icon, null) }, label = { Text(label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangeDark, selectedTextColor = OrangeDark,
                            indicatorColor = OrangePale, unselectedIconColor = SchoolMuted,
                            unselectedTextColor = SchoolMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                "Simulator" -> if (barShotOpen) LiveBarShotPage(
                    session = session, rig = rig,
                    onBack = { barShotOpen = false }, onReset = ::resetMission
                ) else OperatorSeatPage(
                    session = session, rig = rig, missionIndex = missionIndex, onMission = ::selectMission,
                    onRotate = { direction, rpm ->
                        session = TrainingEngine.orient(session, direction, rpm)
                    },
                    onStopRotation = {
                        if (session.rotationRpm != 0) session = TrainingEngine.stopRotation(session)
                    },
                    onMove = ::move,
                    onPullBack = { session = TrainingEngine.pullBack(session) },
                    onAddRod = { session = TrainingEngine.addRod(session) },
                    onReset = ::resetMission,
                    onPeek = { barShotOpen = true },
                    onReplayLocator = { speak("Locator Juan. ${locator.message}", force = true) },
                    onSettings = { settingsOpen = true },
                    onHaptic = ::haptic,
                    voiceEnabled = voiceEnabled
                )
                "Lessons" -> LessonsPage(prefs)
                "Missions" -> MissionsPage(scores, onPlay = { index ->
                    selectMission(index); tab = "Simulator"
                })
                else -> TrainingProgressPage(scores, onPractice = { index ->
                    selectMission(index); tab = "Simulator"
                })
            }
        }
    }

    if (settingsOpen) AlertDialog(
        onDismissRequest = { settingsOpen = false },
        title = { Text("Simulator settings", fontWeight = FontWeight.Black) },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("Training rig profile", fontWeight = FontWeight.Bold)
                rigProfiles.forEach { profile ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = rig.id == profile.id, onClick = {
                            rigId = profile.id
                            prefs.edit().putString("rig_id", profile.id).apply()
                            resetMission()
                        })
                        Text(profile.displayName, fontSize = 13.sp)
                    }
                }
                Text("Published maximum ratings, not live measurements or recommended operating settings.",
                    fontSize = 11.sp, color = SchoolMuted)
                HorizontalDivider()
                Text("Haptic feedback", fontWeight = FontWeight.Bold)
                listOf("Off", "Light", "Normal", "Strong").forEachIndexed { index, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = hapticLevel == index, onClick = {
                            hapticLevel = index
                            prefs.edit().putInt("haptics_level", index).apply()
                            haptic(FeedbackCue.ENGAGE)
                        })
                        Text(label)
                    }
                }
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Locator voice commands", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Switch(checked = voiceEnabled, onCheckedChange = {
                        voiceEnabled = it
                        prefs.edit().putBoolean("locator_voice", it).apply()
                        if (!it) tts?.stop()
                    })
                }
                SubText("Voice requires an available Android text-to-speech engine. " +
                    "Vibration intensity depends on your phone.")
            }
        },
        confirmButton = { TextButton(onClick = { settingsOpen = false }) { Text("Done") } }
    )
}
