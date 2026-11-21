package com.forgecrew.drillschool

import kotlin.math.abs

private var passed = 0
private fun expect(name: String, condition: Boolean) {
    check(condition) { "FAILED: $name" }
    passed++
}

fun main() {
    val beginning = SimState(trainingMissions[0])
    expect("entry starts at twelve", beginning.lastClock == 12)
    val clockwise = TrainingEngine.orient(beginning, 1, 40)
    expect("clockwise wraps twelve to one", clockwise.lastClock == 1)
    expect("RPM reflected in model", clockwise.rotationRpm == 40)
    val back = TrainingEngine.orient(clockwise, -1, 55)
    expect("counterclockwise one to twelve", back.lastClock == 12)
    expect("rotation stop", TrainingEngine.stopRotation(back).rotationRpm == 0)
    expect("entry locator call", LocatorCoach.current(beginning).key == "entry")
    expect("locator target twelve", LocatorCoach.current(beginning).targetClock == 12)
    val steer = TrainingEngine.advance(back, SimMode.STEER, back.lastClock)
    expect("push increments station", steer.stationFeet == 5.0)
    expect("steering changes pitch", steer.pitchPercent < back.pitchPercent)
    expect("shot stored", steer.shots.size == 2)
    val pulled = TrainingEngine.pullBack(steer)
    expect("pullback decrements station", pulled.stationFeet == 0.0)
    expect("pullback decrements shots", pulled.shots.size == 1)
    expect("pullback restores pitch", abs(pulled.pitchPercent - 8.0) < 0.0001)
    expect("cannot pull back beyond pit", TrainingEngine.pullBack(pulled).shots.size == 1)
    val spinning = TrainingEngine.advance(beginning, SimMode.ROTATE, 3)
    expect("spin plus push maintains pitch in game", spinning.pitchPercent == beginning.pitchPercent)
    expect("mode recorded", spinning.shots.last().mode == SimMode.ROTATE)
    var middle = beginning
    repeat(8) { middle = TrainingEngine.advance(middle, SimMode.ROTATE, 12) }
    expect("locator progresses through phases", LocatorCoach.current(middle).key != "entry")
    val terminal = beginning.copy(phase = SimPhase.FAILED)
    expect("locator warns on failure", LocatorCoach.current(terminal).key == "stopped")
    expect("no rotation after failure", TrainingEngine.orient(terminal, 1, 50) == terminal)
    expect("no advancement after failure", TrainingEngine.advance(terminal, SimMode.STEER, 12) == terminal)
    expect("no pullback after failure", TrainingEngine.pullBack(terminal) == terminal)
    println("DrillSchool operator controls: $passed checks passed.")
}
