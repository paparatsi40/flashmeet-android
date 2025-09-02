package com.carlitoswy.flashmeet.utils

import android.content.Context
import java.io.File

fun createImageFile(context: Context): File {
    val dir = File(context.cacheDir, "images")
    if (!dir.exists()) dir.mkdirs()
    return File.createTempFile("event_", ".jpg", dir)
}
