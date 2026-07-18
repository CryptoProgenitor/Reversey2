// Top-level build file. Keep vanilla: no extra plugins beyond Android + Kotlin + Compose.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
