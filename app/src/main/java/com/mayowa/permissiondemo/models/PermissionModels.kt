package com.mayowa.permissiondemo.models

import android.Manifest
import com.mayowa.permissiondemo.ui.screens.EntryScreenViewModel

val PERMISSION_RATIONALE = mapOf(
    Manifest.permission.CAMERA to "We need access to your camera to capture photos.",
    Manifest.permission.ACCESS_MEDIA_LOCATION to "We need access to your media location to organize your photos by location.",
)

sealed class PermissionAction {
    data class RequestPermission(val unGrantedPermissions: Set<String>) : PermissionAction()
    data class ShowRationale(val unGrantedPermissions: Set<String>) : PermissionAction()
    data class LaunchSettings(val unGrantedPermissions: Set<String>) : PermissionAction()
    data class ProceedWithIntent(val intent: EntryScreenViewModel.UiIntent?) : PermissionAction()
}