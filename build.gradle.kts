// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.google.gms.google.services) apply false
//    alias(libs.plugins.jetbrains.kotlin.android) apply false
//
//
//
//}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
//    dependencies {
//        // This ensures we use the correct Android Gradle Plugin version
//        classpath("com.android.tools.build:gradle:9.0.0")  // Updated Android Gradle Plugin version
//    }
}

allprojects {
    repositories {
       // google()
//        mavenCentral()
    }
}
