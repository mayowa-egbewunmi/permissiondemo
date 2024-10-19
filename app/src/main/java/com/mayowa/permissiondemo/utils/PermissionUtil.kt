package com.mayowa.permissiondemo.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {

    fun filterPermissionsNotGranted(context: Activity, permissions: List<String>): List<String> {
        return permissions.filter { permission ->
            when (permission) {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                -> {
                    !isReadMediaStoragePermissionOnApi34Granted(permission, context)
                }

                else -> {
                    ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                }
            }
        }
    }

    fun shouldShowRequestPermissionRationale(context: Activity, permissions: List<String>): Boolean {
        return permissions.isNotEmpty() && permissions.all { ActivityCompat.shouldShowRequestPermissionRationale(context, it) }
    }

    private fun isReadMediaStoragePermissionOnApi34Granted(permission: String, context: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val alternatePermissionOptionGranted = when (permission) {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                -> ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED

                else -> ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED || alternatePermissionOptionGranted
        } else {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}