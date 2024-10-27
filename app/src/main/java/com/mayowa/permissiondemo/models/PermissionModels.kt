package com.mayowa.permissiondemo.models

import android.Manifest
import android.annotation.SuppressLint
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.ui.permissions.PermissionStateManager

@SuppressLint("InlinedApi")
val PERMISSION_RATIONALE = mapOf(
    Manifest.permission.CAMERA to "We need access to your camera to capture photos.",
    Manifest.permission.ACCESS_MEDIA_LOCATION to "We need access to your media location to organize your photos by location.",
    Manifest.permission.READ_MEDIA_IMAGES to "We need access to your media images to display them in the app.",
    Manifest.permission.READ_EXTERNAL_STORAGE to "We need access to your media images to display them in the app.",
)

val PERMISSION_TITLE = mapOf(
    Manifest.permission.CAMERA to "Camera Permission",
    Manifest.permission.ACCESS_MEDIA_LOCATION to "Media Location Permission",
    Manifest.permission.READ_MEDIA_IMAGES to "Media Images Permission",
    Manifest.permission.READ_EXTERNAL_STORAGE to "Media Images Permission",
)

val PERMISSION_ICONS = mapOf(
    Manifest.permission.CAMERA to R.drawable.ic_art_camera,
    Manifest.permission.ACCESS_MEDIA_LOCATION to R.drawable.img_media_location,
    Manifest.permission.READ_MEDIA_IMAGES to R.drawable.img_media,
    Manifest.permission.READ_EXTERNAL_STORAGE to R.drawable.img_media,
)

sealed class PermissionAction {
    abstract val unapprovedPermissions: Set<String>
    data class RequestPermission(override val unapprovedPermissions: Set<String>) : PermissionAction()
    data class ShowRationale(override val unapprovedPermissions: Set<String>, val requiresSettings: Boolean) : PermissionAction()
    data class Proceed(override val unapprovedPermissions: Set<String>, val intent: PermissionStateManager.PendingPermissionIntent?) : PermissionAction()
}