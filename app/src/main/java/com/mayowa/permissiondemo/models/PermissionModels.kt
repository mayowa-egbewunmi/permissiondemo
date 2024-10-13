package com.mayowa.permissiondemo.models

import android.Manifest
import android.annotation.SuppressLint
import com.mayowa.permissiondemo.R
import com.mayowa.permissiondemo.ui.screens.EntryScreenViewModel

@SuppressLint("InlinedApi")
val PERMISSION_RATIONALE = mapOf(
    Manifest.permission.CAMERA to "We need access to your camera to capture photos.",
    Manifest.permission.ACCESS_MEDIA_LOCATION to "We need access to your media location to organize your photos by location.",
)

val PERMISSION_TITLE = mapOf(
    Manifest.permission.CAMERA to "Camera Permission", // TODO: Use resource
    Manifest.permission.ACCESS_MEDIA_LOCATION to "Media Location Permission", //TODO: Add icon
)

val PERMISSION_ICONS = mapOf(
    Manifest.permission.CAMERA to R.drawable.ic_art_camera,
    Manifest.permission.ACCESS_MEDIA_LOCATION to R.drawable.ic_art_camera, //TODO: Add icon
)

sealed class PermissionAction {
    data class RequestPermission(val unGrantedPermissions: Set<String>) : PermissionAction()
    data class ShowRationale(val unGrantedPermissions: Set<String>) : PermissionAction()
    data class LaunchSettings(val unGrantedPermissions: Set<String>) : PermissionAction()
    data class ProceedWithIntent(val intent: EntryScreenViewModel.UiIntent?) : PermissionAction()
}