import com.forgecrew.drillschool.*
import kotlin.math.abs

fun main() {
    val mission = DrillMission("guidance-test", "Test", "", 20.0,
        1.0, 0.0, emptyList())
    val state = SimState(mission).copy(pitchPercent = 0.0)
    val readout = trainingGuidanceReading(state)
    check(readout.clock == 12)
    check(readout.depthFeet == 0.75)
    check(readout.distanceToExitFeet == 20.0)
    check(abs(readout.projectedExitDepthFeet - 0.75) < 1e-6)
    check(abs(readout.depthDifferenceAtExitFeet - 0.25) < 1e-6)
    check(feetAndInches(2.25) == "2' 3\"")
    check(feetAndInches(-1.0) == "-1' 0\"")

    val turned = TrainingEngine.orient(state, 1, 40)
    check(trainingGuidanceReading(turned).clock == 1)
    val pushed = TrainingEngine.advance(turned, SimMode.STEER, 1)
    check(trainingGuidanceReading(pushed).distanceToExitFeet == 18.0)
    check(trainingGuidanceReading(pushed).depthFeet != readout.depthFeet)
    check(trainingGuidanceReading(pushed).targetX in -1f..1f)
    check(trainingGuidanceReading(pushed).targetY in -1f..1f)
    println("DrillSchool V5 guidance readouts validated with fictional mission state.")
}
