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


## Rig Reference and Rod-Change Training — v0.3.0

DrillSchool has selectable equipment **reference** profiles: Vermeer D24x40 S3, Vermeer D23x30 S3, Ditch Witch JT20 and JT21. Each profile lists documented **maximum ratings**, including nominal 10-foot rods, rated thrust/pullback (lb), maximum spindle torque (ft-lb), maximum spindle RPM, and maximum drilling-fluid PSI and GPM. The displayed maxima are **not actual live pressure, torque, force readings, recommended working settings or safety thresholds**. Hydraulic pressure and drilling-fluid pressure are different measurements.

The game now advances a simulated 2 feet per input increment within a 10-foot nominal rod, stops for a simulated rod connection and requires the player to tap Add Rod to continue. This is an educational game cycle, not a replica of a real rig's rod-loader, vise and safety procedure. Tool path and hazards still use fictional physics, not engineering data.

The Vermeer profiles retain the existing *dual training-joystick* user interface. The JT20 profile switches to one combined thrust/rotation joystick inspired by the manufacturer's operator manual (up/down thrust/pull, left/right rotation). Other joystick directions and behavior are not represented as an official rig-control replica.

### Equipment references
- Vermeer D24x40 S3 2026 spec: https://www.vermeer.com/getmedia/e6628b54-49cc-42cf-8456-f215c7a54485/vermeer-d24x40-s3-spec-sheet-may-2026.pdf?ext=.pdf
- Vermeer D23x30 S3 2022 spec: https://www.vermeer.com/getmedia/475562df-ec1f-41c5-ab91-9d3e7c67d944/d23x30-s3-utility-drill-spec-sheet-updated-2022.pdf?ext=.pdf
- JT20 product specs: https://www.ditchwitchwest.com/new-equipment/all-directional-drills/directional-drills/jt20-directional-drill/
- JT20 operator manual: https://www.ditchwitchmidwest.com/fckimages/pdfs/manuals/Directional%20Drills/JT20%20operator_s%20manual%20%28ID0318836_09_SVC%29.pdf
- JT21 manufacturer brochure: https://www.ditchwitch.com/wp-content/uploads/2024/11/JT21-Brochure.pdf


## BoreLab-style Operator Cockpit — v0.4.0

The app opens at a Training Academy home screen, never in an active mission. Simulator launches through a setup screen with Free Practice (no automatic locator voice) or Guided Mission (scripted locator voice). Mission, rig and display choices are available before launch.

The operator seat uses the approved orange-and-white BoreLab concept: a single dark rig-side instrument panel with depth/pitch/clock/distance and live *game* path, an illustrative 2D/3D toggle, mission/rod status, locator message, left Rotate and right Push/Pull controls. Multitouch supports rotating while holding thrust. The actual manufacturer's display interfaces are not reproduced; DigiTrak-inspired and Subsite-inspired choices are independent of the selected drill rig.

All telemetry is simulated. Real beacon temperature, mud PSI, fluid flow and link strength are explicitly not available. The game doesn't estimate hydraulic thrust force, downhole pressure, or safe operating conditions. Real equipment procedures and utility clearances require manufacturer training and local safety rules.


## Driller and Locator Display Upgrade — V0.5.0

Reworked operator view around field photos supplied by the project owner. The app retains BoreLab-inspired white/orange cards, and an original simulated instrument design with a prominent roll clock, pitch and depth, center target-steering reticle, game model exit-offset summary, and a narrow right-side telemetry reference column. DCI/DigiTrak-inspired and Subsite-inspired themes are distinct visual variants; 2D and perspective-style 3D are selectable.

**Peek Locator** opens a separate handheld-receiver training view with the same simulated clock, pitch and depth, and a simplified bore illustration. **Bar Shot** still opens the game profile. Rotate and Push/Pull remain simultaneously operable. The mission view hides bottom navigation to give the instruments room, but retains a back control.

The simulated guidance calculation displays projected exit-depth and lateral differences based on the *fictional* game state; this is not a true HDD steering estimate. No real hardware, beacon, battery, temperature, or drilling-fluid sensors are connected. Unavailable readings show an em dash instead of fabricated values. The application does not provide actual locate measurements, machine operating instructions, utility clearances, or verified target steering. It is not affiliated with equipment manufacturers.

The simulator still uses simplified stepping, rod changes, optional Juan TTS, offline score storage and haptic feedback. User-submitted photos and concept artwork served as interface references; no OEM artwork, logos, or firmware have been embedded.


## Version 0.6.0: Distinct Rig-Side Display Layouts

The v0.5 design incorrectly drew DigiTrak- and Subsite-inspired displays using the same clock-and-target composition, differing mostly in color. V0.6 fixes this with **two genuinely different Compose screens**:

- DigiTrak-inspired: dedicated large toolface dial, prominent pitch and depth, circular target reticle, right telemetry-status strip, and horizontal/vertical model offsets.
- Subsite-inspired: dedicated drill-to bore line/grade profile (or simplified perspective view), prominent pitch and depth, compact toolface indicator, and independent station/grade/side measurements.

Use the dropdown **inside the Operator Seat** to change between these profiles; the choice persists locally. Each has 2D/3D *training* variations. A larger, fixed-height bottom measurement strip prevents the previous clipped readouts.

This is an original teaching interface informed by user-supplied equipment photographs. **It is not exact OEM firmware and is not live locating equipment**. Readouts are fictional game values, and unavailable transmitter battery, temperature, and fluid pressure show dashes. Never use DrillSchool for actual steering decisions or utility clearances.


## v0.7.0 — Locator guidance logic and precision toolface

The training model now has **24 half-hour toolface positions** (e.g., 3:30). Rotate's left/right arrow taps advance 30 minutes. Holding the dial repeats at adjustable intervals: Precision 900 ms (default), Normal 600 ms, Fast 350 ms. Each step provides short optional haptics. These are educational clock nudge speeds, **not real spindle RPM or steering commands**.

The fictional locator has its own independent station (starting 10 ft ahead), with a **Move locator +10 ft** action when the head approaches. The monitor distinguishes **head depth**, **distance to locator**, **projected depth at locator**, **target depth at locator**, and a target-centering indicator. Pitch and trajectory updates follow simulated push; rotating the stationary toolface cannot change the guidance target.

Filler battery, transmitter temperature, PSI, and wireless status are removed from guidance screens. The distinct DigiTrak-inspired 2D/3D layout includes large toolface, pitch, current depth and crosshair, and the training-only Subsite view has a profile and readouts. A new separately designed **MAG 9-inspired** profile shows toolface, pitch, depth/projected depth, locator distance, rod count and target centering. No UI claims to be OEM firmware or a real locator. No real instrumentation or calibrated physical drilling calculations exist in this app.

Automated unit tests cover 30-minute precision, clock wrapping, separate locator movement, target unchanged by stationary rotation, depth projections, and rendering units. APK build runs unit tests before packaging.
