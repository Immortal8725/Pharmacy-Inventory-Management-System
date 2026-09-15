#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

mkdir -p build/classes build/fat dist
find src -name '*.java' | sort > build/sources.txt
javac --release 17 -encoding UTF-8 -cp "lib/*" -d build/classes @build/sources.txt
cp src/config.properties build/classes/config.properties

rm -rf build/fat
mkdir -p build/fat
python3 - <<'PY'
import zipfile, pathlib, shutil, os
root = pathlib.Path("build/fat")
for jar in pathlib.Path("lib").glob("*.jar"):
    with zipfile.ZipFile(jar) as zf:
        for info in zf.infolist():
            name = info.filename
            if name.endswith("/"):
                continue
            if name.startswith("META-INF/") and (
                name.endswith(".SF") or name.endswith(".DSA") or name.endswith(".RSA") or name == "META-INF/MANIFEST.MF"
            ):
                continue
            target = root / name
            target.parent.mkdir(parents=True, exist_ok=True)
            with zf.open(info) as src, open(target, "wb") as dst:
                shutil.copyfileobj(src, dst)
classes = pathlib.Path("build/classes")
for path in classes.rglob("*"):
    if path.is_file():
        dest = root / path.relative_to(classes)
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(path, dest)
PY

cat > build/MANIFEST.MF <<'EOF'
Manifest-Version: 1.0
Main-Class: com.healthfirst.pims.PIMSApplication
EOF

jar cfm dist/healthfirst_pims.jar build/MANIFEST.MF -C build/fat .
cp dist/healthfirst_pims.jar healthfirst_pims.jar
if command -v gcc >/dev/null 2>&1; then
    gcc -O2 -o healthfirst_pims launcher.c
fi
echo "Built dist/healthfirst_pims.jar"
