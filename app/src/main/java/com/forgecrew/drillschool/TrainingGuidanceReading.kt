package com.forgecrew.drillschool

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Fictional geometry to teach what numbers mean on a monitor.
 * NOT a physical HDD steering or depth prediction, and NOT instrument data.
 */
data class TrainingGuidanceReading(
    val clock: Int,
    val toolfaceLabel: String,
    val pitchPercent: Double,
    val depthFeet: Double,
    val lateralFeet: Double,
    val locatorStationFeet: Double,
    val distanceToLocatorFeet: Double,
    val projectedDepthAtLocatorFeet: Double,
    val targetDepthAtLocatorFeet: Double,
    val verticalErrorAtLocatorFeet: Double,
    val lateralErrorAtLocatorFeet: Double,
    val rodNumber: Int,
    val distanceToExitFeet: Double,
    val depthDifferenceAtExitFeet: Double,
    val lateralDifferenceAtExitFeet: Double,
    val projectedExitDepthFeet: Double,
    val targetX: Float,
    val targetY: Float
)

fun trainingGuidanceReading(state: SimState): TrainingGuidanceReading {
    val remaining = (state.mission.totalFeet - state.stationFeet).coerceAtLeast(0.0)
    val locatorStation = state.locatorStationFeet.coerceIn(0.0, state.mission.totalFeet)
    val toLocator = (locatorStation - state.stationFeet).coerceAtLeast(0.0)

    // A deliberately simple teaching model. Do not derive operating angles from it.
    val projectedToLocator = state.depthFeet + toLocator * state.pitchPercent / 100.0
    val targetDepth = 0.75 +
        (state.mission.exitDepthFeet - 0.75) *
        (locatorStation / state.mission.totalFeet.coerceAtLeast(1.0))
    val targetLateral = state.mission.exitLateralFeet *
        (locatorStation / state.mission.totalFeet.coerceAtLeast(1.0))
    val deltaDepthToLocator = targetDepth - projectedToLocator
    val deltaLateralToLocator = targetLateral - state.lateralFeet

    val projectedExit = state.depthFeet + remaining * state.pitchPercent / 100.0
    val deltaDepthToExit = state.mission.exitDepthFeet - projectedExit
    val deltaLateralToExit = state.mission.exitLateralFeet - state.lateralFeet

    return TrainingGuidanceReading(
        clock = state.lastClock,
        toolfaceLabel = clockLabelFromTick(state.toolfaceTick),
        pitchPercent = state.pitchPercent,
        depthFeet = state.depthFeet,
        lateralFeet = state.lateralFeet,
        locatorStationFeet = locatorStation,
        distanceToLocatorFeet = toLocator,
        projectedDepthAtLocatorFeet = projectedToLocator,
        targetDepthAtLocatorFeet = targetDepth,
        verticalErrorAtLocatorFeet = deltaDepthToLocator,
        lateralErrorAtLocatorFeet = deltaLateralToLocator,
        rodNumber = state.rodNumber,
        distanceToExitFeet = remaining,
        depthDifferenceAtExitFeet = deltaDepthToExit,
        lateralDifferenceAtExitFeet = deltaLateralToExit,
        projectedExitDepthFeet = projectedExit,
        // Target is moved by head position, pitch and simulated thrust,
        // not merely by rotating the stationary toolface.
        targetX = (deltaLateralToLocator / 3.0).toFloat().coerceIn(-1f, 1f),
        targetY = (-deltaDepthToLocator / 3.0).toFloat().coerceIn(-1f, 1f)
    )
}

fun feetAndInches(feet: Double): String {
    val total = (abs(feet) * 12.0).roundToInt()
    return (if (feet < 0) "-" else "") +
        (total / 12) + "' " + (total % 12) + "\""
}
