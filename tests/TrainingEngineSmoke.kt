import com.forgecrew.drillschool.*

fun main() {
    check(trainingMissions.size == 3)
    val initial = SimState(trainingMissions[0])
    val rotate = TrainingEngine.advance(initial, SimMode.ROTATE, 12)
    check(rotate.stationFeet == 5.0)
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
    val near = SimState(trainingMissions[0]).copy(stationFeet=25.0, depthFeet=3.0,
        pitchPercent=0.0, lateralFeet=0.0)
    val fail = TrainingEngine.advance(near, SimMode.ROTATE, 12)
    check(fail.phase == SimPhase.FAILED)
    check(fail.report.contains("Water line"))
    check(TrainingEngine.advance(fail, SimMode.ROTATE, 12) == fail)

    // Easy fictional corridor completes when exit depth matches.
    val noHazards = DrillMission("test", "Test", "", 10.0, 0.75, 0.0, emptyList())
    val v0 = SimState(noHazards).copy(pitchPercent=0.0)
    val v1 = TrainingEngine.advance(v0, SimMode.ROTATE, 12)
    val win = TrainingEngine.advance(v1, SimMode.ROTATE, 12)
    check(win.phase == SimPhase.COMPLETED)
    check(win.score == 100)
    check(win.shots.size == 3)
    check(runCatching { TrainingEngine.advance(initial, SimMode.STEER, 13) }.isFailure)

    var playable = SimState(trainingMissions[0])
    repeat(3) { playable = TrainingEngine.advance(playable, SimMode.STEER, 12) }
    while (playable.phase == SimPhase.RUNNING) {
        playable = TrainingEngine.advance(playable, SimMode.ROTATE, 12)
    }
    check(playable.phase == SimPhase.COMPLETED)
    check(playable.score in 25..100)
    check(playable.stationFeet == trainingMissions[0].totalFeet)

    println("DrillSchool: all 18 domain checks passed, including a complete playable mission.")
}
