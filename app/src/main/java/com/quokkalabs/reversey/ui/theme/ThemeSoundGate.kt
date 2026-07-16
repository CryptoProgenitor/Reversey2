package com.quokkalabs.reversey.ui.theme

/**
 * 🔇 ONE SWITCH TO RULE THEM ALL
 *
 * Global mute gate for theme-specific decoration sounds (SnowyOwl hoot,
 * StrangePlanet/WeirdWorld creature sounds, Christmas ho-ho-ho, Guitar
 * strum). Checked at each theme sound manager's play method, so it works
 * from plain classes and gesture callbacks without composition access.
 *
 * Kept in sync with the persisted "theme_sounds_enabled" setting by
 * ThemeViewModel. Core app audio (recording playback) is unaffected.
 */
object ThemeSoundGate {
    @Volatile
    var enabled: Boolean = true
}
