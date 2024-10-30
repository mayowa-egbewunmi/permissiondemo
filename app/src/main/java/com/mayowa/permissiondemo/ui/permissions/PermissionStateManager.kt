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

    fun requirePermissions(pendingPermissionIntent: PendingPermissionIntent, callback: () -> Unit) {
        val permissionsToRequest = pendingPermissionIntent.requiredPermissions
            .mapNotNull { neededPermission -> state.value.unapprovedPermissions.firstOrNull { it.permission == neededPermission } }
        if (permissionsToRequest.isEmpty()) {
            callback()
        } else {
            _state.update { it.copy(pendingPermissionIntent = pendingPermissionIntent) }
            resolvePendingPermissionIntent(pendingPermissionIntent)
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.OnScreenStarted -> onScreenStarted(event.unapprovedPermissions)
            is Event.PermissionStateUpdated -> onPermissionStateUpdated(event.requestedPermissions, event.unapprovedPermissions)
            is Event.OnPendingIntentConsumed -> onPendingIntentConsumed()
            Event.OnPermissionRequestCancelled -> onPermissionRequestCancelled()
        }
    }

    private fun onScreenStarted(unapprovedPermissions: Set<PermissionMeta>) {
        _state.update { it.copy(unapprovedPermissions = unapprovedPermissions) }
        val pendingPermissionIntent = _state.value.pendingPermissionIntent
        if (pendingPermissionIntent != null) {
            resolvePendingPermissionIntent(pendingPermissionIntent)
        }
    }

    private fun onPermissionStateUpdated(requestedPermissions: Set<String>, unapprovedPermissions: Set<PermissionMeta>) {
        permissionUtil.cacheRequestedPermissions(requestedPermissions)
        _state.update { it.copy(unapprovedPermissions = unapprovedPermissions) }
        val pendingPermissionIntent = _state.value.pendingPermissionIntent
        if (pendingPermissionIntent != null) {
            resolvePendingPermissionIntent(pendingPermissionIntent)
        }
    }

    private fun onPermissionRequestCancelled() {
        _state.update { it.copy(pendingPermissionIntent = null, permissionAction = null) }
    }

    private fun onPendingIntentConsumed() {
        _state.update { it.copy(pendingPermissionIntent = null, permissionAction = null) }
    }

    private fun resolvePendingPermissionIntent(pendingPermissionIntent: PendingPermissionIntent) {
        val unapprovedPermissions = _state.value.unapprovedPermissions
        val permissionsToRequest = pendingPermissionIntent.requiredPermissions
            .mapNotNull { requiredPermission -> unapprovedPermissions.firstOrNull { it.permission == requiredPermission } }
            .toSet()
        when {
            permissionsToRequest.isEmpty() -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.Proceed(emptySet(), state.value.pendingPermissionIntent))
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
        val pendingPermissionIntent: PendingPermissionIntent? = null,
    )

    sealed class Event {
        data object OnPermissionRequestCancelled : Event()
        data object OnPendingIntentConsumed : Event()
        data class OnScreenStarted(val unapprovedPermissions: Set<PermissionMeta>) : Event()
        data class PermissionStateUpdated(
            val requestedPermissions: Set<String>,
            val unapprovedPermissions: Set<PermissionMeta>,
        ) : Event()
    }

    sealed class PendingPermissionIntent {
        abstract val requiredPermissions: Set<String>

        data class LaunchCameraScreen(override val requiredPermissions: Set<String>) : PendingPermissionIntent()
        data class FetchMediaPhotos(override val requiredPermissions: Set<String>) : PendingPermissionIntent()
    }
}