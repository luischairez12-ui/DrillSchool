from pathlib import Path
import xml.etree.ElementTree as ET
from zipfile import ZipFile

root = Path(__file__).resolve().parents[1]
xmls = list(root.glob('app/src/main/**/*.xml'))
for p in xmls:
    ET.parse(p)
assert len(xmls) >= 3, xmls
required = [
    'settings.gradle.kts', 'build.gradle.kts', 'gradle.properties',
    'app/build.gradle.kts', 'app/src/main/AndroidManifest.xml',
    'app/src/main/java/com/forgecrew/drillschool/MainActivity.kt',
    'app/src/main/java/com/forgecrew/drillschool/TrainingEngine.kt',
    'app/src/main/java/com/forgecrew/drillschool/DrillTheme.kt',
    'app/src/main/java/com/forgecrew/drillschool/SimulatorPage.kt',
    'app/src/main/java/com/forgecrew/drillschool/LearningPages.kt',
]
for f in required:
    assert (root / f).is_file(), f
model = (root / required[6]).read_text()
assert 'android.' not in model
assert 'fun advance(' in model
assert 'SimMode.STEER' in model and 'SimMode.ROTATE' in model
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
assert 'INTERNET' not in manifest and 'ACCESS_FINE_LOCATION' not in manifest
print(f'DrillSchool source check: {len(required)} files and {len(xmls)} XML resources verified.')
