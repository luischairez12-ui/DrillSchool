from pathlib import Path
import re
import zipfile
import xml.etree.ElementTree as ET
root = Path(__file__).parents[1]
main = root/'app/src/main'
kotlin = main/'java/com/forgecrew/drillschool'
for p in ['MainActivity.kt','OperatorSeatPage.kt','LiveBarShotPage.kt','MissionsPage.kt',
          'TrainingEngine.kt','RigProfiles.kt','TrainingFeedback.kt','LocatorCoach.kt','LearningPages.kt','DrillTheme.kt']:
    assert (kotlin/p).is_file(), f'missing {p}'
for path in (main/'res').rglob('*.xml'):
    ET.parse(path)
ET.parse(main/'AndroidManifest.xml')
man = (main/'AndroidManifest.xml').read_text()
assert 'android.permission.VIBRATE' in man
assert 'android.intent.action.TTS_SERVICE' in man
assert 'android.permission.INTERNET' not in man
assert 'android.permission.ACCESS_FINE_LOCATION' not in man
app = (root/'app/build.gradle.kts').read_text()
assert 'versionName = "0.3.0"' in app
assert 'applicationId = "com.forgecrew.drillschool"' in app
page = (kotlin/'OperatorSeatPage.kt').read_text()
assert 'Modifier.weight(1f)' in page and 'OperatorControlDock(' in page
assert 'detectDragGestures' in page and 'onAxis = onLeftAxis' in page
assert 'Peek at Live Bar Shot' in page and 'onHaptic' in page
for path in kotlin.glob('*.kt'):
    assert 'TODO(' not in path.read_text(), f'TODO left in {path.name}'
print('DrillSchool v0.2 source: screen, joystick, manifest, settings and resources checked.')
