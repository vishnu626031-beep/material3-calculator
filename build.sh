#!/usr/bin/env bash
# Builds calculator.apk with the raw Android SDK tools (no Gradle).
#
# Requirements:
#   JDK 17 on PATH (or JAVA_HOME set)
#   ANDROID_HOME pointing at an SDK with:
#       build-tools/34.0.0
#       platforms/android-35
#
# Usage: bash build.sh
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
BT="${BUILD_TOOLS:-$ANDROID_HOME/build-tools/34.0.0}"
PLAT="${PLATFORM:-$ANDROID_HOME/platforms/android-35/android.jar}"
APP="$(cd "$(dirname "$0")" && pwd)"
OUT="$APP/build"

rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex"

echo "== 1/7 compile resources"
"$BT/aapt2" compile --dir "$APP/res" -o "$OUT/res.zip"

echo "== 2/7 link resources + manifest"
"$BT/aapt2" link -o "$OUT/base.apk" \
  -I "$PLAT" \
  --manifest "$APP/AndroidManifest.xml" \
  "$OUT/res.zip" \
  --min-sdk-version 21 \
  --target-sdk-version 34 \
  --version-code 1 \
  --version-name 1.0 \
  --auto-add-overlay

echo "== 3/7 javac"
javac --release 11 -nowarn -cp "$PLAT" -d "$OUT/classes" \
  $(find "$APP/src" -name "*.java")

echo "== 4/7 d8"
"$BT/d8" --lib "$PLAT" --min-api 21 --release \
  --output "$OUT/dex" $(find "$OUT/classes" -name "*.class")

echo "== 5/7 package classes.dex"
python3 - "$OUT" <<'PY'
import shutil, sys, zipfile, os
out = sys.argv[1]
shutil.copyfile(os.path.join(out, "base.apk"), os.path.join(out, "unaligned.apk"))
with zipfile.ZipFile(os.path.join(out, "unaligned.apk"), "a", zipfile.ZIP_DEFLATED) as z:
    z.write(os.path.join(out, "dex", "classes.dex"), "classes.dex")
PY

echo "== 6/7 zipalign"
"$BT/zipalign" -f -p 4 "$OUT/unaligned.apk" "$OUT/aligned.apk"

echo "== 7/7 sign (debug key)"
if [ ! -f "$OUT/debug.keystore" ]; then
  keytool -genkeypair -keystore "$OUT/debug.keystore" -storepass android -keypass android \
    -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1
fi
"$BT/apksigner" sign --ks "$OUT/debug.keystore" --ks-pass pass:android --key-pass pass:android \
  --out "$OUT/calculator.apk" "$OUT/aligned.apk"

"$BT/apksigner" verify --print-certs "$OUT/calculator.apk"
ls -lh "$OUT/calculator.apk"
