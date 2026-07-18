# The Hunt

A one-off Android app: a QR-code treasure hunt birthday gift, in a Wednesday
Addams mood — near-black, off-white, purple accents, gently animated.

She scans printed QR codes (`WOE-01` … `WOE-10`) hidden around the house; each
correct scan snaps (Addams Family style) and flips over the next rhyming clue
like a tarot card. The tenth code triggers a trumpet-fanfare finale with
sparkles and slow black & white confetti.

## Quick start

1. Open this folder in Android Studio (it is a complete project).
2. Let Gradle sync, then run on the device — or from a terminal:
   `gradlew assembleDebug`
   The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.
3. See **TESTING.md** for sideloading, fake-scanning without printed codes,
   and the hidden progress reset.

## Layout

- `app/src/main/assets/hunt.json` — single source of truth: clues, code IDs,
  finale text, error messages. The `hideAt` field is used only by the printed
  sheet and is never shown in the app.
- `app/src/main/res/raw/` — `snap.wav` (successful scan), `fanfare.wav` (finale).
- `app/src/main/java/com/quokkalabs/thehunt/logic/HuntEngine.kt` — the pure
  sequence logic (unit-tested in `app/src/test`).
- `app/src/main/java/com/quokkalabs/thehunt/ui/` — Compose screens and the
  ambient effects (sparkles, fog, candle-glow, confetti).
- Fonts are bundled in `res/font` (Cinzel Decorative, Special Elite); the app
  uses no network at all — no INTERNET permission, no analytics.

## Font licences

Cinzel Decorative is under the SIL Open Font License and Special Elite under
Apache 2.0; both licence texts are in `licenses/`.
