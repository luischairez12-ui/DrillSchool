# DrillSchool — Operator Seat (v0.2.0)

**ForgeCrew Studios** | BoreLab-inspired orange-and-white Android training simulation for horizontal directional drilling.

## What's new in v0.2

- **Operator-seat interface:** two independently draggable virtual thumbsticks in a **fixed dock** at the bottom of the simulator screen.
- **Left thumbstick (horizontal):** clock/toolface orientation and simulated rotation RPM. Clock cycles between 1 and 12. Release = rotation stops.
- **Right thumbstick (vertical):** push to advance or pull back to rewind one training step. Holding push advances in repeating 5-ft **fictional game** increments, not real rod lengths.
- **Steer vs rotate:** advance without moving the rotation stick to steer toward the last selected clock, or hold both sticks to rotate and advance (holds inclination in this simplified model).
- **Locator Juan:** station-based scripted commands ("Give me 12 o'clock. We're 10 feet out from the pit"), voice narration using Android's locally available TextToSpeech engine, and Replay/Play control. Voice can be disabled.
- **Live Bar Shot:** a dedicated expanded bore profile (illustrative design vs measured game path), simulated depth, pitch, clock and position, and fictional hazard positions. Always one tap away from the dock.
- **Haptics:** permission-based, short vibrations on engage, matching requested clock, new locator command, hazard stop and mission completion. Settings Off / Light / Normal / Strong, saved privately on device.
- **Scenarios tab:** a card for each of the three fictional missions, linked to their best training scores. Keeps original lessons, quizzes, and progress.
- **Original simulation engine preserved**, expanded with rotation orientation and pullback. Existing saved scores and lesson completions continue to use the same private preference keys and package name.
- **Buttons** for Rotate, Push 5 ft, and Pull in addition to thumbsticks; useful when touch gestures are difficult or for debugging.

## Install via BuildDeck

1. Download `DrillSchool_Operator_Seat_v020_Source.zip` from the chat.
2. Open **BuildDeck → Projects → Import source project ZIP** and choose the file.
3. Open the newly imported DrillSchool project and select **Build APK**. No need to import into an unrelated GitHub repository.
4. Install the APK produced by your **locally signed** BuildDeck/Termux process and test Mission 01.
5. To test the joysticks: drag the **left stick horizontally** to orient the clock, release to stop rotation; drag the **right stick upward** to steer/push. Hold **both sticks** for rotate + push. Drag the right stick downward to pull back.
6. Tap **Live Bar Shot** from the control dock to inspect the profile, then **Return to Simulator**.
7. Open the gear in the Simulator mission panel for **haptics intensity and locator voice**.

**Build status:** Kotlin domain logic tests and source structure checks ran in this workspace. **A full Android compilation, APK signing, and on-device joystick/TTS/haptic checks have not been performed here.** If BuildDeck reports a compilation error, share its full build log for precise correction.

## Important limitations

This is a **fictional, deliberately simplified training game**. Input speeds, steering sensitivity, RPM labels, depth changes, utility positions, safe zones, design line, challenge distances, commands, and bore outcomes are not calibrated to any drilling machine or locator, including Vermeer equipment. **Do not use DrillSchool to plan or authorize real bores, establish underground utility clearance, or replace any manufacturer or safety training.** The game does not award professional certification.

The Android text-to-speech engine must exist and support a usable voice. No paid speech server or Internet permission is included. Haptic strength is device-dependent and Android may limit vibration; no constant vibration while spinning.

## Build details

- Application ID: `com.forgecrew.drillschool` (same as v0.1.0).
- Version: `0.2.0` (`versionCode 2`).
- Kotlin 2.0.21, Jetpack Compose, Android Gradle Plugin 8.7.3, Gradle 8.9, JDK 17, compile/target SDK 35, min SDK 29.
- Uses **only** `android.permission.VIBRATE`; no Internet, camera, GPS, full-file-access or tracking permissions.
- Local scores + quiz completions and control preferences are saved in `SharedPreferences`.
- Screens: `OperatorSeatPage.kt`, `LiveBarShotPage.kt`, `MissionsPage.kt`, existing `LearningPages.kt` and `TrainingProgressPage`.
- `TrainingEngine.kt` and `LocatorCoach.kt` are pure Kotlin and can be tested without Android SDK.

## Offline tests

```bash
kotlinc app/src/main/java/com/forgecrew/drillschool/TrainingEngine.kt app/src/main/java/com/forgecrew/drillschool/LocatorCoach.kt tests/TrainingEngineSmoke.kt -include-runtime -d /tmp/drillschool-base.jar
java -jar /tmp/drillschool-base.jar
kotlinc app/src/main/java/com/forgecrew/drillschool/TrainingEngine.kt app/src/main/java/com/forgecrew/drillschool/LocatorCoach.kt tests/OperatorControlsSmoke.kt -include-runtime -d /tmp/drillschool-controls.jar
java -jar /tmp/drillschool-controls.jar
python3 tests/check_source.py
```

## First phone test checklist

- [ ] Import ZIP; BuildDeck completes `assembleDebug` and generates APK.
- [ ] App opens and the fixed two-stick dock is usable in Portrait on a OnePlus-class phone.
- [ ] Left stick turns toolface clock; releasing stops rotation.
- [ ] Right stick up advances; right stick down rewinds; buttons work too.
- [ ] Both sticks together use rotation-plus-thrust in the simplified game.
- [ ] "Live Bar Shot" opens; bore profile changes when the drill advances.
- [ ] Juan speaks through installed Android TTS, or text-only mode works without voice.
- [ ] Haptics Off works and Light/Normal/Strong provide appropriately short feedback.
- [ ] Missions, lessons, quizzes, and progress remain accessible.
- [ ] Existing progress persists on an in-place local update signed with the same key.

## Next steps after real phone testing

Adjust thumbstick sensitivity, UI size, and call timing from actual user feedback. More precise steering physics, rig-specific control layouts, and a dedicated locator-player mode would require explicit design and validation; do not infer them from this game model.
