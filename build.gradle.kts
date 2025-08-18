plugins {
    id("com.android.application") version "8.6.1" apply false
    id("com.google.devtools.ksp") version "2.1.21-2.0.1" apply false
    id("com.google.gms.google-services") version("4.4.1") apply false
    id("org.jetbrains.kotlin.kapt") version "2.1.21" apply false
    alias(libs.plugins.android.hilt) apply false

       // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.5" apply false
}
