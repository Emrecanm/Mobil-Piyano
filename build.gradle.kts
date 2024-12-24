plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Google Services Plugin
        classpath("com.google.gms:google-services:4.4.2")

        // Firebase Crashlytics Plugin (opsiyonel)
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.8")
    }
}