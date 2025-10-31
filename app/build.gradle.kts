plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    //alias(libs.plugins.ksp)
    //alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")              // No version - inherits from top-level
    id("com.google.dagger.hilt.android")       // No version - inherits from top-level
}

android {
    namespace = "com.example.reversey"
    compileSdk = 36
    //compileSdk {
    //    version = release(36)// was version = release(36)
    //}

    defaultConfig {
        applicationId = "com.example.reversey"
        minSdk = 26
        targetSdk = 36
        versionCode = 70
        versionName = "12.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions { //old
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
    buildFeatures {
        compose = true
        // This line enables the BuildConfig.DEBUG variable used in SettingsScreen.kt
        buildConfig = true
    }
}

dependencies {


    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material) // For Material 1 components if needed
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.core) // Use the alias for the core glance library
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.volley)
    implementation("com.github.wendykierp:JTransforms:3.1")  // For FFT
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //HILT - https://developer.android.com/training/dependency-injection/hilt-android#kts
    // Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.57.1")
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.text)
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // For visualization (optional but recommended)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("be.tarsos.dsp:core:2.5")
    implementation("be.tarsos.dsp:jvm:2.5")


}
