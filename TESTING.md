# Testing The Hunt

Everything below assumes Windows 11 with adb at
`C:\android_sdk\platform-tools\adb` and the phone (Samsung S25+) connected
with USB debugging enabled (Settings → Developer options → USB debugging;
tap "Build number" 7 times in Settings → About phone → Software information
if Developer options is hidden).

## 1. Build

In Android Studio: open this folder, wait for Gradle sync, press Run.

Or from a terminal in the project folder:

```
gradlew assembleDebug
```

APK output: `app\build\outputs\apk\debug\app-debug.apk`

Unit tests (sequence logic — correct / duplicate / skipped / unknown codes):

```
gradlew test
```

## 2. Sideload onto the phone

```
C:\android_sdk\platform-tools\adb devices
C:\android_sdk\platform-tools\adb install -r app\build\outputs\apk\debug\app-debug.apk
```

`-r` reinstalls while keeping app data (i.e. hunt progress survives).
First install: accept the "allow USB debugging" prompt on the phone.

## 3. Fake-scanning (no printed codes needed)

### Option A — adb broadcast (debug builds only)

With the app **open on screen**, each of these behaves exactly like scanning
that QR code (snap sound, clue reveal, error cards, finale):

```
C:\android_sdk\platform-tools\adb shell am broadcast -a com.quokkalabs.thehunt.FAKE_SCAN --es code WOE-01
```

Change `WOE-01` to any payload — try `WOE-05` early to see the out-of-order
message, `WOE-01` twice for the already-scanned card, or `BANANA` for the
unknown-code card.

Fast-forward the whole hunt from a Command Prompt:

```
for /L %i in (1,1,10) do C:\android_sdk\platform-tools\adb shell am broadcast -a com.quokkalabs.thehunt.FAKE_SCAN --es code WOE-0%i & timeout /t 2
```

(The `WOE-010` produced for i=10 is fine — the app reads it as code 10.)

This receiver only exists in debug builds; the release APK does not contain it.

### Option B — QR codes on your monitor

Generate a QR with any generator (e.g. search "QR code generator", content
type "plain text") containing exactly `WOE-01` (etc.), display it on your
monitor, and scan the screen with the app. The printed sheet PDF works the
same way.

## 4. Hidden progress reset (for repeated testing)

Long-press the "THE HUNT" title — on the welcome screen or the main screen —
and **hold for 5 seconds**. A confirm dialog appears ("Erase all suffering and
begin again?"). Confirming wipes progress back to the welcome screen. It is
deliberately hard to trigger by accident.

Alternatively, nuke everything from adb:

```
C:\android_sdk\platform-tools\adb shell pm clear com.quokkalabs.thehunt
```

## 5. What to verify before the big day

1. Fresh install → welcome screen → Begin → Clue I card flips in.
2. Scan WOE-01 → snap sound, coffin 1 glows, Clue II revealed.
3. Scan WOE-01 again → "You've already endured this one. Move along."
4. Scan WOE-03 → out-of-order message naming the clue she actually needs.
5. Scan any random QR (a supermarket one, say) → "not part of your ordeal".
6. Kill the app, reopen → progress and current clue are still there.
7. Scan WOE-10 after 1–9 → fanfare, sparkle burst, confetti, beating heart.
8. Kill and reopen after the finale → it opens on the finale screen with the
   skull replay button.
9. Deny the camera permission once → "Very well. Enjoy scanning nothing."
