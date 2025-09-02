plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.carlitoswy.flashmeet"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/flashmeet-upload-key.jks") // Ruta al .jks
            storePassword = project.findProperty("KEYSTORE_PASSWORD") as String
            keyAlias = "flashmeetkey"
            keyPassword = project.findProperty("KEY_PASSWORD") as String
        }
    }

    defaultConfig {
        applicationId = "com.carlitoswy.flashmeet"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "STRIPE_PUBLISHABLE_KEY",
            "\"${project.findProperty("STRIPE_PK_DEBUG") ?: "pk_test_xxx"}\""
        )
        buildConfigField(
            "String",
            "PAYMENTS_BASE_URL",
            "\"${project.findProperty("PAYMENTS_BASE_URL_DEBUG") ?: "https://dev-backend.example.com"}\""
        )
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "STRIPE_PUBLISHABLE_KEY",
                "\"${project.findProperty("STRIPE_PK_RELEASE") ?: ""}\""
            )
            buildConfigField(
                "String",
                "PAYMENTS_BASE_URL",
                "\"${project.findProperty("PAYMENTS_BASE_URL_RELEASE") ?: ""}\""
            )
        }

        getByName("debug") {
            // configs debug si hace falta
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation ("androidx.appcompat:appcompat:1.7.1")

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.googleid)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.media3.common.ktx)
    implementation("com.google.firebase:firebase-functions")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Firebase (BoM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-messaging")
    implementation (libs.firebase.firestore.ktx)


    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // Google Maps / Places
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose.v2150)
    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation("com.google.maps.android:android-maps-utils:3.9.0")

    // Coil
    implementation(libs.coil.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Retrofit / OkHttp
    implementation(libs.retrofit)
    implementation(libs.moshi)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.gson) // Si no lo usas, puedes quitarlo
    implementation(libs.gson)

    // Room (ajusta si usas KSP/annotationProcessor)
    implementation(libs.androidx.room.runtime)

    // Fonts / Icons
    implementation(libs.androidx.compose.google.fonts)
    implementation(libs.androidx.material.icons.extended)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    implementation(libs.play.services.location)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.play.services.auth.v2100)

    // Accompanist
    implementation(libs.accompanist.permissions)
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.34.0")

    // ✅ Stripe — usa SOLO PaymentSheet (alineado)
    // Si tienes alias libs.paymentsheet ya configurado, puedes usarlo:
    implementation(libs.paymentsheet)

    // ❌ Elimina estos si no necesitas flujos avanzados:
    // implementation(libs.stripe.android.v21210)
    // implementation(libs.payments.ui.core)
    // implementation(libs.financial.connections)

    implementation("com.airbnb.android:lottie-compose:6.4.0")
    implementation("com.google.android.material:material:1.12.0")
}
