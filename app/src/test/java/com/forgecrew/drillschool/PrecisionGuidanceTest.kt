package com.forgecrew.drillschool

import org.junit.Assert.*
import org.junit.Test

class PrecisionGuidanceTest {
    private val mission = DrillMission(
        id = "unit", title = "Training test", description = "",
        totalFeet = 60.0, exitDepthFeet = 0.75,
        exitLateralFeet = 0.0, utilities = emptyList()
    )

    @Test fun halfHourToolfaceIsControllableAndWraps() {
        var state = SimState(mission)
        repeat(7) { state = TrainingEngine.nudgeToolface(state, 1) }
        assertEquals("3:30", clockLabelFromTick(state.toolfaceTick))
        assertEquals(3, state.lastClock)
        val before = trainingGuidanceReading(state)
        state = TrainingEngine.nudgeToolface(state, -1)
        assertEquals("3:00", clockLabelFromTick(state.toolfaceTick))
        assertEquals(before.depthFeet, trainingGuidanceReading(state).depthFeet, 0.000001)
        val leftFromTwelve = TrainingEngine.nudgeToolface(SimState(mission), -1)
        assertEquals("11:30", clockLabelFromTick(leftFromTwelve.toolfaceTick))
    }

    @Test fun locatorHasSeparateMovableStationAndProjection() {
        val initial = SimState(mission)
        val first = trainingGuidanceReading(initial)
        assertEquals(10.0, first.locatorStationFeet, 0.000001)
        assertEquals(10.0, first.distanceToLocatorFeet, 0.000001)
        assertEquals(1.55, first.projectedDepthAtLocatorFeet, 0.000001)
        assertEquals(0.75, first.targetDepthAtLocatorFeet, 0.000001)

        val relocated = TrainingEngine.moveLocatorAhead(initial)
        val after = trainingGuidanceReading(relocated)
        assertEquals(20.0, after.locatorStationFeet, 0.000001)
        assertEquals(20.0, after.distanceToLocatorFeet, 0.000001)
        assertEquals(2.35, after.projectedDepthAtLocatorFeet, 0.000001)
    }

    @Test fun targetDoesNotShiftJustBecauseToolfaceRotates() {
        val original = SimState(mission)
        val a = trainingGuidanceReading(original)
        val nudge = TrainingEngine.nudgeToolface(original, 1)
        val b = trainingGuidanceReading(nudge)
        assertEquals(a.targetX, b.targetX, 0.000001f)
        assertEquals(a.targetY, b.targetY, 0.000001f)
        val pushed = TrainingEngine.advance(nudge, SimMode.STEER, nudge.lastClock)
        val c = trainingGuidanceReading(pushed)
        assertEquals(8.0, c.distanceToLocatorFeet, 0.000001)
        assertNotEquals(a.targetY, c.targetY)
    }

    @Test fun clockAndDepthFormattingAreReadable() {
        assertEquals("12:00", clockLabelFromTick(0))
        assertEquals("12:30", clockLabelFromTick(1))
        assertEquals("11:30", clockLabelFromTick(23))
        assertEquals("3' 2\"", feetAndInches(3.0 + 2.0 / 12.0))
        assertEquals("8' 10\"", feetAndInches(8.0 + 10.0 / 12.0))
        assertTrue(RotationSensitivity.PRECISION.stepDelayMs > RotationSensitivity.NORMAL.stepDelayMs)
        assertTrue(RotationSensitivity.NORMAL.stepDelayMs > RotationSensitivity.FAST.stepDelayMs)
        assertEquals(3, GuidanceStyle.entries.size)
    }
}
