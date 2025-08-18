package com.carlitoswy.flashmeet.localization

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

val LocalAppLanguage = compositionLocalOf<MutableState<String>> {
    error("No App Language Provided")
}
