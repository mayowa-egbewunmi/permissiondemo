package com.mayowa.permissiondemo.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.permissiondemo.models.PermissionAction
import com.mayowa.permissiondemo.models.PermissionAction.Proceed
import com.mayowa.permissiondemo.models.PermissionAction.RequestPermission
import com.mayowa.permissiondemo.models.PermissionAction.ShowRationale
import com.mayowa.permissiondemo.models.Photo
import com.mayowa.permissiondemo.models.randomSizedPhotos
import com.mayowa.permissiondemo.utils.PermissionUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryScreenViewModel @Inject constructor(
    private val permissionUtil: PermissionUtil,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: Event) {
        when (event) {
            Event.OnPermissionRequestCancelled -> {
                onPermissionRequestCancelled()
            }

            is Event.PermissionStateUpdated -> {
                onPermissionStateUpdated(event.unapprovedPermissions, event.isRationaleRequired)
            }

            is Event.TakePhotoTapped -> {
                onTakePhotoTapped(event.unapprovedPermissions, event.rationaleRequired)
            }

            is Event.OnPendingIntentConsumed -> {
                onPendingIntentConsumed()
            }

            is Event.OnScreenLaunch -> {
                onScreenLaunch(event.unapprovedPermissions, event.rationaleRequired)
            }

            Event.OnCustomRationaleDisplayed -> {
                onCustomRationaleDisplayed()
            }
        }
    }

    private fun onTakePhotoTapped(
        unapprovedPermissions: Set<String>,
        isRationaleRequired: Boolean,
    ) {
        _state.update { it.copy(pendingUiIntent = UiIntent.LaunchCameraScreen) }
        resolvePermissionAction(unapprovedPermissions, isRationaleRequired)
    }

    private fun onScreenLaunch(unapprovedPermissions: Set<String>, isRationaleRequired: Boolean) {
        resolvePermissionAction(unapprovedPermissions, isRationaleRequired)
    }

    private fun onPermissionStateUpdated(
        unapprovedPermissions: Set<String>,
        isRationaleRequired: Boolean,
    ) {
        permissionUtil.cacheDeniedPermissions(unapprovedPermissions)
        resolvePermissionAction(unapprovedPermissions, isRationaleRequired)
    }

    private fun onPendingIntentConsumed() {
        _state.update { it.copy(pendingUiIntent = null, customRationaleDisplayed = false) }
    }

    private fun resolvePermissionAction(
        unapprovedPermissions: Set<String>,
        isRationaleRequired: Boolean,
    ) {
        when {
            unapprovedPermissions.isEmpty() -> {
                _state.update { it.copy(permissionAction = Proceed(state.value.pendingUiIntent)) }
            }

            isRationaleRequired -> {
                _state.update { it.copy(permissionAction = ShowRationale(unapprovedPermissions, false)) }
            }

            permissionUtil.isAnyPreviouslyDenied(unapprovedPermissions) -> {
                _state.update { it.copy(permissionAction = ShowRationale(unapprovedPermissions, true)) }
            }

            else -> {
                _state.update { it.copy(permissionAction = RequestPermission(unapprovedPermissions)) }
            }
        }
    }

    private fun onPermissionRequestCancelled() {
        _state.update { it.copy(pendingUiIntent = null, customRationaleDisplayed = false) }
    }

    private fun onCustomRationaleDisplayed() {
        _state.update { it.copy(customRationaleDisplayed = true) }
    }

    data class State(
        val cameraPermissionGranted: Boolean = false,
        val permissionAction: PermissionAction? = null,
        val randomPhotos: List<Photo> = randomSizedPhotos,
        val pendingUiIntent: UiIntent? = null,
        val customRationaleDisplayed: Boolean = false,
    )

    sealed class UiIntent {
        data object LaunchCameraScreen : UiIntent()
    }

    sealed class Event {
        data object OnPermissionRequestCancelled : Event()
        data object OnPendingIntentConsumed : Event()
        data object OnCustomRationaleDisplayed : Event()
        data class OnScreenLaunch(val unapprovedPermissions: Set<String>, val rationaleRequired: Boolean) : Event()
        data class PermissionStateUpdated(val unapprovedPermissions: Set<String>, val isRationaleRequired: Boolean) : Event()
        data class TakePhotoTapped(val unapprovedPermissions: Set<String>, val rationaleRequired: Boolean) : Event()
    }

    sealed class Effect

    private fun emitEffect(effect: Effect) = viewModelScope.launch {
        _effect.emit(effect)
    }
}