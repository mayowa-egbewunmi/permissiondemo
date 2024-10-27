package com.mayowa.permissiondemo.ui.permissions

import com.mayowa.permissiondemo.models.PermissionAction
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
        if (_state.value.permissionAction is PermissionAction.Proceed) {
            callback()
        } else {
            _state.update { it.copy(pendingPermissionIntent = pendingPermissionIntent) }
            val isRationaleRequired = _state.value.permissionAction is PermissionAction.ShowRationale
            val unGrantedPermissions = _state.value.permissionAction?.unapprovedPermissions ?: emptySet()
            resolvePermissionStatus(unGrantedPermissions, isRationaleRequired)
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            Event.OnPermissionRequestCancelled -> onPermissionRequestCancelled()
            is Event.PermissionStateUpdated -> onPermissionStateUpdated(
                event.unapprovedPermissions, event.isRationaleRequired)
            is Event.OnPendingIntentConsumed -> onPendingIntentConsumed()
            is Event.OnScreenLaunch -> onScreenLaunch(event.unapprovedPermissions, event.rationaleRequired)
        }
    }

    private fun onScreenLaunch(unapprovedPermissions: Set<String>, isRationaleRequired: Boolean) {
        resolvePermissionStatus(unapprovedPermissions, isRationaleRequired)
    }

    private fun onPermissionStateUpdated(unapprovedPermissions: Set<String>, isRationaleRequired: Boolean) {
        permissionUtil.cacheDeniedPermissions(unapprovedPermissions)
        resolvePermissionStatus(unapprovedPermissions, isRationaleRequired)
    }

    private fun onPendingIntentConsumed() {
        _state.update { it.copy(pendingPermissionIntent = null) }
    }

    private fun resolvePermissionStatus(unGrantedPermissions: Set<String>, isRationaleRequired: Boolean) {
        when {
            unGrantedPermissions.isEmpty() -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.Proceed(unGrantedPermissions, state.value.pendingPermissionIntent))
                }
            }

            isRationaleRequired -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.ShowRationale(unGrantedPermissions, permissionUtil.isAnyPreviouslyDenied(unGrantedPermissions)))
                }
            }

            else -> {
                _state.update {
                    it.copy(
                        permissionAction = PermissionAction.RequestPermission(unGrantedPermissions),
                        ungrantedPermissions = unGrantedPermissions
                    )
                }
            }
        }
    }

    private fun onPermissionRequestCancelled() {
        _state.update { it.copy(pendingPermissionIntent = null) }
    }

    data class State(
        val permissionAction: PermissionAction? = null,
        val ungrantedPermissions: Set<String> = emptySet(),
        val pendingPermissionIntent: PendingPermissionIntent? = null,
    )

    sealed class Event {
        data object OnPermissionRequestCancelled : Event()
        data object OnPendingIntentConsumed : Event()
        data class OnScreenLaunch(val unapprovedPermissions: Set<String>, val rationaleRequired: Boolean) : Event()
        data class PermissionStateUpdated(val unapprovedPermissions: Set<String>, val isRationaleRequired: Boolean) : Event()
    }

    sealed class PendingPermissionIntent {
        data object LaunchCameraScreen : PendingPermissionIntent()
    }
}