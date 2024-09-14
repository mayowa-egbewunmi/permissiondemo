package com.mayowa.permissiondemo.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStorageUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createNewImageFile(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(Date())
        return File(context.filesDir, "IMG_${timestamp}.jpg").canonicalPath
    }
}