#!/bin/bash

echo "🚧 Reestructurando proyecto FlashMeet..."

BASE="app/src/main/java/com/carlitoswy/flashmeet"

# Capa data
mkdir -p $BASE/data/repository
mkdir -p $BASE/data/remote
mkdir -p $BASE/data/local
git mv $BASE/data/preferences $BASE/data/local/preferences
git mv $BASE/data/repository/*.kt $BASE/data/repository/

# Capa domain
mkdir -p $BASE/domain/model
mkdir -p $BASE/domain/usecase
mkdir -p $BASE/domain/repository
# Puedes mover modelos si aún no están en domain
# git mv $BASE/data/model/*.kt $BASE/domain/model/

# Capa presentation
mkdir -p $BASE/presentation/features
mkdir -p $BASE/presentation/common/components
mkdir -p $BASE/presentation/common/utils
mkdir -p $BASE/presentation/navigation

# Mover pantallas a features
for feature in event flyer payment search camera myevents home
do
  if [ -d "$BASE/presentation/$feature" ]; then
    git mv "$BASE/presentation/$feature" "$BASE/presentation/features/$feature"
  fi
done

# Mover componentes comunes
git mv $BASE/presentation/components $BASE/presentation/common/components
git mv $BASE/utils $BASE/presentation/common/utils
git mv $BASE/ui/navigation $BASE/presentation/navigation

# Archivos de entrada
mkdir -p $BASE/app
git mv $BASE/MainActivity.kt $BASE/app/
git mv $BASE/FlashMeetApp.kt $BASE/app/

# Temas
git mv $BASE/ui/theme $BASE/presentation/common/theme

echo "✅ Estructura reorganizada con éxito."
