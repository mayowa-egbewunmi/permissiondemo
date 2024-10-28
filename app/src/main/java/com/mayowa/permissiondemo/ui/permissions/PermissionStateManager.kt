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
        val permissionsToRequest = pendingPermissionIntent.neededPermissions
            .mapNotNull {
                neededPermission -> state.value.unapprovedScreenPermissions
                    .firstOrNull { it.permission == neededPermission
                }
            }
        if (permissionsToRequest.isEmpty()) {
            callback()
        } else {
            _state.update { it.copy(pendingPermissionIntent = pendingPermissionIntent) }
            resolvePendingPermissionIntent(pendingPermissionIntent)
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.OnScreenLaunch -> onScreenLaunch(event.unapprovedScreenPermissions)
            is Event.PermissionStateUpdated -> onPermissionStateUpdated(event.unapprovedScreenPermissions)
            is Event.OnPendingIntentConsumed -> onPendingIntentConsumed()
            Event.OnPermissionRequestCancelled -> onPermissionRequestCancelled()
        }
    }

    private fun onScreenLaunch(unapprovedPermissions: Set<PermissionMeta>) {
        _state.update { it.copy(unapprovedScreenPermissions = unapprovedPermissions) }
        val pendingPermissionIntent = _state.value.pendingPermissionIntent
        if (pendingPermissionIntent != null) {
            resolvePendingPermissionIntent(pendingPermissionIntent)
        }
    }

    private fun onPermissionStateUpdated(unapprovedPermissions: Set<PermissionMeta>) {
        permissionUtil.cacheDeniedPermissions(unapprovedPermissions.map { it.permission }.toSet())
        _state.update { it.copy(unapprovedScreenPermissions = unapprovedPermissions) }
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
        val unapprovedPermissions = _state.value.unapprovedScreenPermissions
        val permissionsToRequest = pendingPermissionIntent.neededPermissions
            .mapNotNull { neededPermission -> unapprovedPermissions.firstOrNull { it.permission == neededPermission } }
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
        val unapprovedScreenPermissions: Set<PermissionMeta> = emptySet(),
        val pendingPermissionIntent: PendingPermissionIntent? = null,
    )

    sealed class Event {
        data object OnPermissionRequestCancelled : Event()
        data object OnPendingIntentConsumed : Event()
        data class OnScreenLaunch(val unapprovedScreenPermissions: Set<PermissionMeta>) : Event()
        data class PermissionStateUpdated(val unapprovedScreenPermissions: Set<PermissionMeta>) : Event()
    }

    sealed class PendingPermissionIntent {
        abstract val neededPermissions: List<String>
        data class LaunchCameraScreen(override val neededPermissions: List<String>) : PendingPermissionIntent()
        data class FetchMediaPhotos(override val neededPermissions: List<String>) : PendingPermissionIntent()
    }
}