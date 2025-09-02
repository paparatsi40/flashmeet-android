#!/bin/bash

set -e

echo "📦 Reorganizando estructura del proyecto FlashMeet..."

# Crear módulos raíz
mkdir -p core/ui core/utils data/local data/remote data/repository domain/model domain/usecase domain/repository feature/events feature/flyers feature/settings feature/map feature/auth

# Mover código existente a la nueva estructura
mv app/src/main/java/com/carlitoswy/flashmeet/data/local/* data/local/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/data/remote/* data/remote/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/data/repository/* data/repository/ 2>/dev/null || true

mv app/src/main/java/com/carlitoswy/flashmeet/domain/model/* domain/model/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/domain/usecase/* domain/usecase/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/domain/repository/* domain/repository/ 2>/dev/null || true

mv app/src/main/java/com/carlitoswy/flashmeet/utils/* core/utils/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/ui/components/* core/ui/ 2>/dev/null || true

# Mover pantallas por feature
mv app/src/main/java/com/carlitoswy/flashmeet/presentation/event/* feature/events/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/presentation/flyer/* feature/flyers/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/presentation/settings/* feature/settings/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/presentation/map/* feature/map/ 2>/dev/null || true
mv app/src/main/java/com/carlitoswy/flashmeet/presentation/login/* feature/auth/ 2>/dev/null || true

# Eliminar duplicados innecesarios
rm -rf app/src/main/java/com/carlitoswy/flashmeet/MyEventsLegacyScreen.kt 2>/dev/null || true
rm -rf app/src/main/java/com/carlitoswy/flashmeet/util 2>/dev/null || true

echo "✅ Carpetas reorganizadas."

# Crear archivos build.gradle.kts básicos
for module in core/ui core/utils data/local data/remote data/repository domain feature/events feature/flyers feature/settings feature/map feature/auth; do
  mkdir -p $module/src/main/kotlin
  cat > $module/build.gradle.kts <<EOF
plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.flashmeet.${module//\//.}"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

dependencies {
    // Dependencias comunes o específicas del módulo
}
EOF
done

# Crear settings.gradle.kts
cat > settings.gradle.kts <<EOF
rootProject.name = "FlashMeet"

include(":app")
include(":core:ui", ":core:utils")
include(":data:local", ":data:remote", ":data:repository")
include(":domain")
include(":feature:events", ":feature:flyers", ":feature:settings", ":feature:map", ":feature:auth")
EOF

echo "✅ Archivos build.gradle.kts y settings.gradle.kts generados."

echo "🎉 Proyecto reorganizado correctamente. Puedes abrir Android Studio y sincronizar Gradle."
