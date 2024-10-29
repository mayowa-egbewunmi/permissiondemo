package com.mayowa.permissiondemo.ui.screens

import android.Manifest
import android.app.Application
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.permissiondemo.models.Photo
import com.mayowa.permissiondemo.ui.permissions.PermissionStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryScreenViewModel @Inject constructor(
    private val application: Application,
    val permissionStateManager: PermissionStateManager,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: Event) {
        when (event) {
            is Event.TakePhotoTapped -> takePhoto()
            is Event.OnPendingIntentInvoked -> onPendingIntentInvoked(event.pendingIntent)
            Event.GetStartedButtonTapped -> onGetStartedButtonTapped()
            Event.SelectedPhotosUpdated -> onSelectedPhotosUpdated()
        }
    }

    private fun onPendingIntentInvoked(pendingIntent: PermissionStateManager.PendingPermissionIntent) {
        when (pendingIntent) {
            is PermissionStateManager.PendingPermissionIntent.LaunchCameraScreen -> takePhoto()
            is PermissionStateManager.PendingPermissionIntent.FetchMediaPhotos -> displayPhotos()
        }
    }

    private fun onGetStartedButtonTapped() {
        displayPhotos()
    }

    private fun onSelectedPhotosUpdated() {
        displayPhotos()
    }

    private fun displayPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            val photoUris = mutableListOf<Photo>()
            val contentResolver = application.contentResolver
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID)

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(uri, id.toString())
                    photoUris.add(Photo(contentUri))
                }
            }
            _state.update { it.copy(photoUris = photoUris) }
        }
    }

    private fun takePhoto() {
        emitEffect(Effect.LaunchCameraScreen)
    }

    data class State(
        val photoUris: List<Photo> = emptyList(),
    )

    sealed class Event {
        data object TakePhotoTapped : Event()
        data object GetStartedButtonTapped : Event()
        data object SelectedPhotosUpdated : Event()
        data class OnPendingIntentInvoked(val pendingIntent: PermissionStateManager.PendingPermissionIntent) : Event()
    }

    sealed class Effect {
        data object LaunchCameraScreen : Effect()
    }

    private fun emitEffect(effect: Effect) = viewModelScope.launch {
        _effect.emit(effect)
    }

    companion object {
        val MEDIA_PERMISSIONS = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        val CAMERA_PERMISSIONS = listOf(Manifest.permission.CAMERA)
    }
}