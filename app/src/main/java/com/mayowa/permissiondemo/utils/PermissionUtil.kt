package com.mayowa.permissiondemo.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {

    fun filterPermissionsNotGranted(context: Activity, permissions: List<String>): List<String> {
        return permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
    }

    fun shouldShowRequestPermissionRationale(context: Activity, permissions: List<String>): Boolean {
        return permissions.isNotEmpty() && permissions.all { ActivityCompat.shouldShowRequestPermissionRationale(context, it) }
    }
}