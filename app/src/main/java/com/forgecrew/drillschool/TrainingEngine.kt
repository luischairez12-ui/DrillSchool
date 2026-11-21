package com.forgecrew.drillschool

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Fictional game geometry. Values are NOT real HDD steering predictions or safe clearances. */
data class SimUtility(
    val label: String,
    val stationFeet: Double,
    val depthFeet: Double,
    val lateralFeet: Double,
    val gameZoneRadiusFeet: Double
)

data class DrillMission(
    val id: String,
    val title: String,
    val description: String,
    val totalFeet: Double,
    val exitDepthFeet: Double,
    val exitLateralFeet: Double,
    val utilities: List<SimUtility>
)

val trainingMissions = listOf(
    DrillMission(
        id = "yard", title = "Mission 01 • Yard Fiber",
        description = "Practice an entry, a level run, and a controlled exit.",
        totalFeet = 75.0, exitDepthFeet = 0.75, exitLateralFeet = 0.0,
        utilities = listOf(
            SimUtility("Water line", 30.0, 3.0, 0.0, 1.25),
            SimUtility("Existing service", 55.0, 2.5, 0.0, 1.1)
        )
    ),
    DrillMission(
        id = "street", title = "Mission 02 • Road Crossing",
        description = "Maintain a steady bore while avoiding modeled conflicts.",
        totalFeet = 100.0, exitDepthFeet = 1.0, exitLateralFeet = 0.0,
        utilities = listOf(
            SimUtility("Storm drain", 40.0, 3.4, 0.0, 1.4),
            SimUtility("Existing conduit", 75.0, 2.3, -0.6, 1.3)
        )
    ),
    DrillMission(
        id = "corridor", title = "Mission 03 • Utility Corridor",
        description = "Manage pitch, lateral direction, and exit accuracy.",
        totalFeet = 125.0, exitDepthFeet = 1.0, exitLateralFeet = 0.0,
        utilities = listOf(
            SimUtility("Gas service", 35.0, 2.8, 0.0, 1.1),
            SimUtility("Water main", 65.0, 4.6, 0.8, 1.5),
            SimUtility("Conduit bank", 100.0, 2.5, -0.5, 1.5)
        )
    )
)

enum class SimMode { ROTATE, STEER }
enum class SimPhase { RUNNING, COMPLETED, FAILED }

data class DrillShot(
    val stationFeet: Double,
    val depthFeet: Double,
    val lateralFeet: Double,
    val pitchPercent: Double,
    val clock: Int,
    val mode: SimMode
)

data class SimState(
    val mission: DrillMission,
    val stationFeet: Double = 0.0,
    val depthFeet: Double = 0.75,
    val lateralFeet: Double = 0.0,
    val pitchPercent: Double = 8.0,
    val lastClock: Int = 12,
    val rotationRpm: Int = 0,
    val rodNumber: Int = 1,
    val awaitingNextRod: Boolean = false,
    val thrusts: Int = 0,
    val steerThrusts: Int = 0,
    val phase: SimPhase = SimPhase.RUNNING,
    val report: String = "Use the clock position to select a steering direction.",
    val shots: List<DrillShot> = listOf(DrillShot(0.0, 0.75, 0.0, 8.0, 12, SimMode.ROTATE)),
    val score: Int = 0
)

/** Intentionally exaggerated steering so players can learn cause/effect on a phone screen. */
object TrainingEngine {
    const val stepFeet = 2.0
    const val rodFeet = 10.0 // Nominal rod length for the V3 model profiles.

    /** Fictional clock orientation. Positive = clockwise, negative = counterclockwise. */
    fun orient(state: SimState, direction: Int, rpm: Int): SimState {
        if (state.phase != SimPhase.RUNNING || direction == 0) return state
        require(direction in -1..1)
        val nextClock = ((state.lastClock - 1 + direction + 12) % 12) + 1
        return state.copy(
            lastClock = nextClock,
            rotationRpm = rpm.coerceIn(0, 300),
            report = "Toolface at ${nextClock} o'clock • simulated rotation ${rpm.coerceIn(0, 300)} RPM."
        )
    }

    fun stopRotation(state: SimState): SimState = state.copy(rotationRpm = 0)

    /**
     * Simulated rod connection only. This does NOT depict the real machine's
     * pipe-loading, vise, isolation or safety procedure.
     */
    fun addRod(state: SimState): SimState {
        if (state.phase != SimPhase.RUNNING || !state.awaitingNextRod) return state
        return state.copy(
            rodNumber = state.rodNumber + 1,
            awaitingNextRod = false,
            rotationRpm = 0,
            report = "Simulated rod ${state.rodNumber + 1} connected. Ready for the next ${oneDecimal(rodFeet)} ft stroke."
        )
    }

    /** Rewind one game step. Never rewinds through an impact/failure. */
    fun pullBack(state: SimState): SimState {
        if (state.phase != SimPhase.RUNNING || state.shots.size <= 1) return state
        val remaining = state.shots.dropLast(1)
        val last = remaining.last()
        return state.copy(
            stationFeet = last.stationFeet,
            depthFeet = last.depthFeet,
            lateralFeet = last.lateralFeet,
            pitchPercent = last.pitchPercent,
            lastClock = last.clock,
            thrusts = (state.thrusts - 1).coerceAtLeast(0),
            steerThrusts = remaining.count { it.mode == SimMode.STEER },
            shots = remaining,
            rotationRpm = 0,
            awaitingNextRod = false,
            report = "Pulled back ${oneDecimal(stepFeet)} ft in this training game."
        )
    }

    fun advance(state: SimState, mode: SimMode, clock: Int): SimState {
        require(clock in 1..12) { "Toolface must be 1 through 12." }
        if (state.phase != SimPhase.RUNNING) return state
        if (state.awaitingNextRod) return state.copy(
            rotationRpm = 0,
            report = "Rod ${state.rodNumber} reached its 10 ft limit. Connect the next simulated rod first."
        )

        val radians = (if (clock == 12) 0 else clock) * PI / 6.0
        val pitchChange = if (mode == SimMode.STEER) -1.2 * cos(radians) else 0.0
        val lateralChange = if (mode == SimMode.STEER) 0.34 * sin(radians) else 0.0
        val pitch = (state.pitchPercent + pitchChange).coerceIn(-16.0, 16.0)
        val nextRodBoundary = rodFeet * state.rodNumber
        val station = (state.stationFeet + stepFeet).coerceAtMost(minOf(state.mission.totalFeet, nextRodBoundary))
        val atRodEnd = station >= nextRodBoundary - 0.0001 && station < state.mission.totalFeet
        val depth = state.depthFeet + (station - state.stationFeet) * pitch / 100.0
        val lateral = state.lateralFeet + lateralChange
        val shot = DrillShot(station, depth, lateral, pitch, clock, mode)
        val steps = state.shots + shot
        val steerCount = state.steerThrusts + (if (mode == SimMode.STEER) 1 else 0)

        val hit = state.mission.utilities.firstOrNull { utility ->
            val dx = station - utility.stationFeet
            val dy = depth - utility.depthFeet
            val dz = lateral - utility.lateralFeet
            sqrt(dx * dx + dy * dy + dz * dz) <= utility.gameZoneRadiusFeet
        }
        if (hit != null) return state.copy(
            stationFeet = station, depthFeet = depth, lateralFeet = lateral,
            pitchPercent = pitch, lastClock = clock, thrusts = state.thrusts + 1,
            steerThrusts = steerCount, shots = steps, phase = SimPhase.FAILED,
            report = "Simulation stopped: entered the ${hit.label} game hazard zone. Reset and try another path."
        )
        if (depth < 0.3 || depth > 12.0 || abs(lateral) > 9.0) return state.copy(
            stationFeet = station, depthFeet = depth, lateralFeet = lateral,
            pitchPercent = pitch, lastClock = clock, thrusts = state.thrusts + 1,
            steerThrusts = steerCount, shots = steps, phase = SimPhase.FAILED,
            report = "Out of simulated training corridor. Reset and manage pitch or lateral drift."
        )
        val next = state.copy(
            stationFeet = station, depthFeet = depth, lateralFeet = lateral,
            pitchPercent = pitch, lastClock = clock, thrusts = state.thrusts + 1,
            steerThrusts = steerCount, shots = steps,
            awaitingNextRod = atRodEnd,
            report = if (atRodEnd) "Rod ${state.rodNumber} complete at ${oneDecimal(station)} ft. Connect rod ${state.rodNumber + 1}."
            else if (mode == SimMode.STEER) "Steered at ${clock} o’clock. Check your simulated locator readout."
            else "Rotating and advancing. Pitch is held steady in this simplified game."
        )
        if (station >= state.mission.totalFeet) {
            val depthError = abs(depth - state.mission.exitDepthFeet)
            val lateralError = abs(lateral - state.mission.exitLateralFeet)
            if (depthError <= 1.25 && lateralError <= 2.0) {
                val score = (100 - (depthError * 17.0 + lateralError * 7.0).roundToInt() - steerCount)
                    .coerceIn(25, 100)
                return next.copy(phase = SimPhase.COMPLETED, score = score,
                    report = "Training mission complete! Exit reached within the game's target zone.")
            }
            return next.copy(phase = SimPhase.FAILED,
                report = "Missed simulated exit target: depth error ${oneDecimal(depthError)} ft, lateral error ${oneDecimal(lateralError)} ft.")
        }
        return next
    }

    fun nearestHazard(state: SimState): SimUtility? =
        state.mission.utilities.filter { it.stationFeet > state.stationFeet }
            .minByOrNull { it.stationFeet - state.stationFeet }
}

fun oneDecimal(number: Double): String = "%.1f".format(java.util.Locale.US, number)
fun clockName(clock: Int): String = "${clock} o'clock"
