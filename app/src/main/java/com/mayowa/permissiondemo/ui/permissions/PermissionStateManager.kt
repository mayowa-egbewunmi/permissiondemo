package com.mayowa.permissiondemo.ui.permissions

import com.mayowa.permissiondemo.models.PermissionAction
import com.mayowa.permissiondemo.models.PermissionMeta
import com.mayowa.permissiondemo.utils.PermissionUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class PermissionStateManager @Inject constructor(
    private val permissionUtil: PermissionUtil,
) {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun requirePermissions(permissionIntent: PermissionIntent, callback: () -> Unit) {
        val permissionsToRequest = permissionIntent.requiredPermissions
            .mapNotNull { neededPermission -> state.value.unapprovedPermissions.firstOrNull { it.permission == neededPermission } }
        if (permissionsToRequest.isEmpty()) {
            callback()
        } else {
            _state.update { it.copy(permissionIntent = permissionIntent) }
            resolvePermissionIntent(permissionIntent)
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.OnScreenStarted -> onScreenStarted(event.unapprovedPermissions)
            is Event.PermissionStateUpdated -> onPermissionStateUpdated(event.requestedPermissions, event.unapprovedPermissions)
            is Event.PermissionIntentConsumed -> onPermissionIntentConsumed()
            Event.PermissionRequestCancelled -> onPermissionRequestCancelled()
        }
    }

    private fun onScreenStarted(unapprovedPermissions: Set<PermissionMeta>) {
        _state.update { it.copy(unapprovedPermissions = unapprovedPermissions) }
        val permissionIntent = _state.value.permissionIntent
        if (permissionIntent != null) {
            resolvePermissionIntent(permissionIntent)
        }
    }

    private fun onPermissionStateUpdated(requestedPermissions: Set<String>, unapprovedPermissions: Set<PermissionMeta>) {
        permissionUtil.cacheRequestedPermissions(requestedPermissions)
        _state.update { it.copy(unapprovedPermissions = unapprovedPermissions) }
        val permissionIntent = _state.value.permissionIntent
        if (permissionIntent != null) {
            resolvePermissionIntent(permissionIntent)
        }
    }

    private fun onPermissionRequestCancelled() {
        _state.update { it.copy(permissionIntent = null, permissionAction = null) }
    }

    private fun onPermissionIntentConsumed() {
        _state.update { it.copy(permissionIntent = null, permissionAction = null) }
    }

    private fun resolvePermissionIntent(permissionIntent: PermissionIntent) {
        val unapprovedPermissions = _state.value.unapprovedPermissions
        val permissionsToRequest = permissionIntent.requiredPermissions
            .mapNotNull { requiredPermission -> unapprovedPermissions.firstOrNull { it.permission == requiredPermission } }
            .toSet()
        when {
            permissionsToRequest.isEmpty() -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.Proceed(emptySet(), state.value.permissionIntent))
                }
            }
            permissionsToRequest.all { it.shouldShowRationale } -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.ShowRationale(permissionsToRequest, false))
                }
            }
            permissionsToRequest.any { it.requiresSettings } -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.ShowRationale(permissionsToRequest, true))
                }
            }
            else -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.RequestPermission(permissionsToRequest))
                }
            }
        }
    }

    data class State(
        val permissionAction: PermissionAction? = null,
        val unapprovedPermissions: Set<PermissionMeta> = emptySet(),
        val permissionIntent: PermissionIntent? = null,
    )

    sealed class Event {
        data object PermissionRequestCancelled : Event()
        data object PermissionIntentConsumed : Event()
        data class OnScreenStarted(val unapprovedPermissions: Set<PermissionMeta>) : Event()
        data class PermissionStateUpdated(
            val requestedPermissions: Set<String>,
            val unapprovedPermissions: Set<PermissionMeta>,
        ) : Event()
    }

    sealed class PermissionIntent {
        abstract val requiredPermissions: Set<String>

        data class LaunchCameraScreen(override val requiredPermissions: Set<String>) : PermissionIntent()
        data class FetchMediaPhotos(override val requiredPermissions: Set<String>) : PermissionIntent()
    }
}