package com.mayowa.permissiondemo.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionUtil @Inject constructor(
    private val sharedPreferenceUtil: SharedPreferenceUtil,
) {

    fun filterNotGranted(context: Activity, permissions: List<String>): List<String> {
        return permissions.filter { permission ->
            when (permission) {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                -> {
                    !isReadMediaStoragePermissionGranted(permission, context)
                }
                else -> {
                    ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                }
            }
        }
    }

    fun shouldShowRationale(context: Activity, permissions: List<String>): Boolean {
        return permissions.isNotEmpty() && permissions.all { ActivityCompat.shouldShowRequestPermissionRationale(context, it) }
    }

    fun isAnyPreviouslyDenied(permissions: Set<String>): Boolean {
        return sharedPreferenceUtil.containsAny(PrefKey.DENIED_PERMISSIONS, permissions.toSet())
    }

    fun cacheDeniedPermissions(permissions: Set<String>) {
        sharedPreferenceUtil.put(PrefKey.DENIED_PERMISSIONS, permissions)
    }

    private fun isReadMediaStoragePermissionGranted(permission: String, context: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val isVisualSelectorGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED || isVisualSelectorGranted
        } else {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

val LocalPermissionUtil = staticCompositionLocalOf<PermissionUtil> { error("No PermissionUtil provided") }