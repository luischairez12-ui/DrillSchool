import com.forgecrew.drillschool.*

fun main() {
    check(trainingMissions.size == 3)
    val initial = SimState(trainingMissions[0])
    val rotate = TrainingEngine.advance(initial, SimMode.ROTATE, 12)
    check(rotate.stationFeet == 2.0)
    check(rotate.pitchPercent == initial.pitchPercent)
    check(rotate.depthFeet > initial.depthFeet)

    val up = TrainingEngine.advance(initial, SimMode.STEER, 12)
    val down = TrainingEngine.advance(initial, SimMode.STEER, 6)
    val left = TrainingEngine.advance(initial, SimMode.STEER, 9)
    val right = TrainingEngine.advance(initial, SimMode.STEER, 3)
    check(up.pitchPercent < initial.pitchPercent)
    check(down.pitchPercent > initial.pitchPercent)
    check(left.lateralFeet < 0.0)
    check(right.lateralFeet > 0.0)
    check(up.shots.size == 2)
    check(TrainingEngine.advance(up, SimMode.ROTATE, 1).shots.size == 3)
    check(TrainingEngine.nearestHazard(initial)?.label == "Water line")

    // Utility collision in this *game model*, not a real-world safety clearance.
    val near = SimState(trainingMissions[0]).copy(stationFeet=28.0, depthFeet=3.0, rodNumber=3,
        pitchPercent=0.0, lateralFeet=0.0)
    val fail = TrainingEngine.advance(near, SimMode.ROTATE, 12)
    check(fail.phase == SimPhase.FAILED)
    check(fail.report.contains("Water line"))
    check(TrainingEngine.advance(fail, SimMode.ROTATE, 12) == fail)

    // Easy fictional corridor completes when exit depth matches.
    val noHazards = DrillMission("test", "Test", "", 10.0, 0.75, 0.0, emptyList())
    val v0 = SimState(noHazards).copy(pitchPercent=0.0)
    var win = v0
    repeat(5) { win = TrainingEngine.advance(win, SimMode.ROTATE, 12) }
    check(win.phase == SimPhase.COMPLETED)
    check(win.score == 100)
    check(win.shots.size == 6)
    check(runCatching { TrainingEngine.advance(initial, SimMode.STEER, 13) }.isFailure)

    var rodCycle = SimState(trainingMissions[0]).copy(pitchPercent = 0.0)
    repeat(5) { rodCycle = TrainingEngine.advance(rodCycle, SimMode.ROTATE, 12) }
    check(rodCycle.stationFeet == 10.0 && rodCycle.awaitingNextRod)
    check(TrainingEngine.advance(rodCycle, SimMode.ROTATE, 12).stationFeet == 10.0)
    val loaded = TrainingEngine.addRod(rodCycle)
    check(loaded.rodNumber == 2 && !loaded.awaitingNextRod)
    check(TrainingEngine.advance(loaded, SimMode.ROTATE, 12).stationFeet == 12.0)
    check(rigProfiles.size == 4)
    check(rigProfiles.all { it.rodFeet == 10.0 && it.maxFluidPsi > 0 })
    check(getRigProfile("jt20").controlMode == RigControlMode.JT20_COMBINED)
    check(getRigProfile("d23x30s3").maxSpindleRpm == 219)

    var playable = SimState(trainingMissions[0])
    repeat(7) {
        if (playable.awaitingNextRod) playable = TrainingEngine.addRod(playable)
        playable = TrainingEngine.advance(playable, SimMode.STEER, 12)
    }
    var attempts = 0
    while (playable.phase == SimPhase.RUNNING && attempts++ < 100) {
        if (playable.awaitingNextRod) playable = TrainingEngine.addRod(playable)
        playable = TrainingEngine.advance(playable, SimMode.ROTATE, 12)
    }
    check(playable.phase == SimPhase.COMPLETED)
    check(playable.score in 25..100)
    check(playable.stationFeet == trainingMissions[0].totalFeet)

    println("DrillSchool: rod-cycle and rig reference checks passed, including a complete playable mission.")
}
