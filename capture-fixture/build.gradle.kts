plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.veil.fixture"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.veil.fixture"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0-test-only"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
