package com.forgecrew.drillschool

/** Scripted calls for fictional practice only. Not live communication or real drilling guidance. */
data class LocatorCall(
    val key: String,
    val message: String,
    val targetClock: Int?,
    val objective: String
)

object LocatorCoach {
    fun current(state: SimState): LocatorCall {
        if (state.phase == SimPhase.COMPLETED) return LocatorCall(
            "complete", "Nice work! We reached the simulated exit. Let's review the shot.", null, "Mission complete"
        )
        if (state.phase == SimPhase.FAILED) return LocatorCall(
            "stopped", "Stop the simulation. Check the profile and reset before trying again.", null, "Safety stop"
        )
        val fraction = state.stationFeet / state.mission.totalFeet
        return when {
            fraction < 0.14 -> LocatorCall("entry", "Give me 12 o'clock. We're 10 feet out from the pit.", 12, "Orient to 12, then push")
            fraction < 0.38 -> LocatorCall("early", "Hold that line. Give me a clean reading before another push.", null, "Watch pitch and depth")
            fraction < 0.66 -> LocatorCall("middle", "Let's rotate and push. Keep an eye on the bore profile.", null, "Rotate while advancing")
            fraction < 0.83 -> LocatorCall("exit", "Check our exit approach. Give me 6 o'clock for a correction if needed.", 6, "Check exit alignment")
            else -> LocatorCall("finish", "Almost at the target. Watch the live bar shot before the last push.", null, "Finish accurately")
        }
    }
}
