# Material 3 Calculator

A simple, minimal calculator for Android with a Material 3 look. No AndroidX, no
third-party libraries — the whole UI is built in plain Java on top of the
framework, so the APK is about **29 KB**.

![Material 3](https://img.shields.io/badge/design-Material%203-6750A4)

## Features

- Material 3 baseline colour roles (light **and** dark theme, follows the system setting)
- Tonal pill keys with proper state-layer ripples and haptic feedback
- Expression line above the result, live result preview while typing
- Correct operator precedence (`2 + 3 × 4 = 14`)
- Percent, backspace, clear, chained calculations after `=`
- Clean divide-by-zero handling
- Auto-sizing display text, up to 80sp for short numbers
- `minSdk 21` (Android 5.0) · `targetSdk 34`

## Keys

| Row | Keys |
| --- | --- |
| 1 | `AC` `⌫` `%` `÷` |
| 2 | `7` `8` `9` `×` |
| 3 | `4` `5` `6` `−` |
| 4 | `1` `2` `3` `+` |
| 5 | `0` `.` `=` |

## Install

Download `calculator.apk` from this repository and open it on your phone
(allow "install unknown apps" for your file manager / browser). The APK is
signed with a debug key, so it installs fine but is not Play Store ready.

## Build from source

You only need a JDK 17 and the Android SDK (build-tools 34 + platform 35).
No Gradle required — `build.sh` drives `aapt2`, `javac`, `d8`, `zipalign` and
`apksigner` directly.

```bash
export ANDROID_HOME=/path/to/android-sdk   # build-tools/34.0.0 + platforms/android-35
bash build.sh
```

Output: `build/calculator.apk`.

## Layout

```
AndroidManifest.xml
src/com/vishnu/calc/MainActivity.java     the whole Material 3 UI, built in code
src/com/vishnu/calc/CalculatorEngine.java expression state + evaluator
res/values, res/values-night              colour roles for light / dark
res/drawable, res/mipmap-*                adaptive + legacy launcher icon
```

## Design tokens used

| Role | Light | Dark |
| --- | --- | --- |
| surface | `#FEF7FF` | `#141218` |
| onSurface | `#1D1B20` | `#E6E0E9` |
| onSurfaceVariant | `#49454F` | `#CAC4D0` |
| surfaceContainer (digits) | `#F3EDF7` | `#211F26` |
| secondaryContainer (operators) | `#E8DEF8` | `#4A4458` |
| tertiaryContainer (AC, ⌫) | `#FFD8E4` | `#633B48` |
| primary (= key) | `#6750A4` | `#D0BCFF` |

## License

MIT
