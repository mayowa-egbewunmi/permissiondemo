package com.mayowa.permissiondemo.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.permissiondemo.models.Photo
import com.mayowa.permissiondemo.models.randomSizedPhotos
import com.mayowa.permissiondemo.ui.permissions.PermissionStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryScreenViewModel @Inject constructor(
    val permissionStateManager: PermissionStateManager,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: Event) {
        when (event) {
            is Event.TakePhotoTapped -> onTakePhotoTapped()
            is Event.OnPendingIntentInvoked -> onPendingIntentInvoked(event.pendingIntent)
        }
    }

    private fun onPendingIntentInvoked(pendingIntent: PermissionStateManager.PendingPermissionIntent) {
        when (pendingIntent) {
            PermissionStateManager.PendingPermissionIntent.LaunchCameraScreen -> emitEffect(Effect.LaunchCameraScreen)
        }
    }

    private fun onTakePhotoTapped() {
        emitEffect(Effect.LaunchCameraScreen)
    }

    data class State(
        val randomPhotos: List<Photo> = randomSizedPhotos,
    )

    sealed class Event {
        data object TakePhotoTapped : Event()
        data class OnPendingIntentInvoked(val pendingIntent: PermissionStateManager.PendingPermissionIntent) : Event()
    }

    sealed class Effect {
        data object LaunchCameraScreen : Effect()
    }

    private fun emitEffect(effect: Effect) = viewModelScope.launch {
        _effect.emit(effect)
    }
}