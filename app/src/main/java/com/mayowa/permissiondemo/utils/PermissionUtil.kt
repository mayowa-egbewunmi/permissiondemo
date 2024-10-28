package com.mayowa.permissiondemo.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mayowa.permissiondemo.models.PermissionMeta
import javax.inject.Inject

class PermissionUtil @Inject constructor(
    private val sharedPreferenceUtil: SharedPreferenceUtil,
) {

    fun cacheDeniedPermissions(permissions: Set<String>) {
        sharedPreferenceUtil.put(PrefKey.DENIED_PERMISSIONS, permissions)
    }

    fun filterNotGranted(context: Activity, permissions: List<String>): List<PermissionMeta> {
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
        }.map {
            PermissionMeta(
                permission = it,
                shouldShowRationale = shouldShowRationale(context, it),
                requiresSettings = isPreviouslyDenied(it)
            )
        }
    }

    private fun shouldShowRationale(context: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permission)
    }

    private fun isPreviouslyDenied(permission: String): Boolean {
        return sharedPreferenceUtil.containsAny(PrefKey.DENIED_PERMISSIONS, setOf(permission))
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