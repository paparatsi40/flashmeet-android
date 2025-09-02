package com.carlitoswy.flashmeet.presentation.event

import android.net.Uri
import com.carlitoswy.flashmeet.domain.model.AdOption

data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val city: String = "",
    val date: String = "",

    val adOption: AdOption = AdOption.NONE,

    val flyerTextColor: String? = null,
    val flyerBackgroundColor: String? = null,
    val flyerFontFamily: String? = null,

    val showFlyerTextColorPicker: Boolean = false,
    val showFlyerBackgroundColorPicker: Boolean = false,
    val showFlyerFontPicker: Boolean = false,

    val imageUri: Uri? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val newEventId: String? = null
)
