package com.forgecrew.drillschool

import kotlin.math.abs

/**
 * Model-only readings for UI. These numbers come from the fictional training
 * trajectory, not DCI/Subsite equipment, telemetry, or a physical bore forecast.
 */
data class TrainingGuidanceReading(
    val clock: Int,
    val pitchPercent: Double,
    val depthFeet: Double,
    val lateralFeet: Double,
    val distanceToExitFeet: Double,
    val depthDifferenceAtExitFeet: Double,
    val lateralDifferenceAtExitFeet: Double,
    val projectedExitDepthFeet: Double,
    val targetX: Float,
    val targetY: Float
)

fun trainingGuidanceReading(state: SimState): TrainingGuidanceReading {
    val remaining = (state.mission.totalFeet - state.stationFeet).coerceAtLeast(0.0)
    val projected = state.depthFeet + remaining * state.pitchPercent / 100.0
    val deltaDepth = state.mission.exitDepthFeet - projected
    val deltaLateral = state.mission.exitLateralFeet - state.lateralFeet
    return TrainingGuidanceReading(
        clock = state.lastClock,
        pitchPercent = state.pitchPercent,
        depthFeet = state.depthFeet,
        lateralFeet = state.lateralFeet,
        distanceToExitFeet = remaining,
        depthDifferenceAtExitFeet = deltaDepth,
        lateralDifferenceAtExitFeet = deltaLateral,
        projectedExitDepthFeet = projected,
        targetX = (deltaLateral / 5.0).toFloat().coerceIn(-1f, 1f),
        targetY = (-deltaDepth / 5.0).toFloat().coerceIn(-1f, 1f)
    )
}

/** Human-readable plain units; avoid pretending estimated values are measured. */
fun feetAndInches(feet: Double): String {
    val negative = feet < 0
    val wholeInches = (abs(feet) * 12.0).toInt()
    val rem = (abs(feet) * 12.0 - wholeInches)
    val inches = if (rem >= 0.5) wholeInches + 1 else wholeInches
    return (if (negative) "-" else "") + (inches / 12) + "' " +
        (inches % 12).toString() + "\""
}
