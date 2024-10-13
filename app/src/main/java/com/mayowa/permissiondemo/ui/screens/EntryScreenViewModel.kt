package com.mayowa.permissiondemo.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.permissiondemo.models.PermissionAction
import com.mayowa.permissiondemo.models.Photo
import com.mayowa.permissiondemo.models.randomSizedPhotos
import com.mayowa.permissiondemo.utils.PrefKey
import com.mayowa.permissiondemo.utils.SharedPreferenceUtil
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
    private val sharedPreferenceUtil: SharedPreferenceUtil,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: Event) {
        when (event) {
            Event.OnPermissionRequestCancelled -> onPermissionRequestCancelled()
            is Event.PermissionRequirementUpdated -> onPermissionRequirementUpdated(event.unGrantedPermissions, event.isRationaleRequired)
            is Event.TakePhotoTapped -> onTakePhotoTapped(event.requiredPermissions, event.rationaleRequired)
            is Event.OnPendingIntentConsumed -> onPendingIntentConsumed()
            is Event.OnScreenLaunch -> onScreenLaunch(event.requiredPermissions, event.rationaleRequired)
        }
    }

    private fun onTakePhotoTapped(unGrantedPermissions: Set<String>, isRationaleRequired: Boolean) {
        _state.update { it.copy(pendingUiIntent = UiIntent.LaunchCameraScreen) }
        updatePermissionAction(unGrantedPermissions, isRationaleRequired)
    }

    private fun onScreenLaunch(unGrantedPermissions: Set<String>, isRationaleRequired: Boolean) {
        updatePermissionAction(unGrantedPermissions, isRationaleRequired)
    }

    private fun onPermissionRequirementUpdated(unGrantedPermissions: Set<String>, isRationaleRequired: Boolean) {
        sharedPreferenceUtil.put(PrefKey.DENIED_PERMISSIONS, unGrantedPermissions)
        updatePermissionAction(unGrantedPermissions, isRationaleRequired)
    }

    private fun onPendingIntentConsumed() {
        _state.update { it.copy(pendingUiIntent = null) }
    }

    private fun updatePermissionAction(unGrantedPermissions: Set<String>, isRationaleRequired: Boolean) {
        when {
            unGrantedPermissions.isEmpty() -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.ProceedWithIntent(state.value.pendingUiIntent))
                }
            }

            isRationaleRequired -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.ShowRationale(unGrantedPermissions))
                }
            }

            sharedPreferenceUtil.containsAny(PrefKey.DENIED_PERMISSIONS, unGrantedPermissions.toSet()) -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.LaunchSettings(unGrantedPermissions))
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
        _state.update { it.copy(pendingUiIntent = null) }
    }

    data class State(
        val cameraPermissionGranted: Boolean = false,
        val permissionAction: PermissionAction? = null,
        val ungrantedPermissions: Set<String> = emptySet(),
        val randomPhotos: List<Photo> = randomSizedPhotos,
        val pendingUiIntent: UiIntent? = null,
    )

    sealed class UiIntent {
        data object LaunchCameraScreen : UiIntent()
    }

    sealed class Event {
        data object OnPermissionRequestCancelled : Event()
        data object OnPendingIntentConsumed : Event()
        data class OnScreenLaunch(val requiredPermissions: Set<String>, val rationaleRequired: Boolean) : Event()
        data class PermissionRequirementUpdated(val unGrantedPermissions: Set<String>, val isRationaleRequired: Boolean) : Event()
        data class TakePhotoTapped(val requiredPermissions: Set<String>, val rationaleRequired: Boolean) : Event()
    }

    sealed class Effect

    private fun emitEffect(effect: Effect) = viewModelScope.launch {
        _effect.emit(effect)
    }
}