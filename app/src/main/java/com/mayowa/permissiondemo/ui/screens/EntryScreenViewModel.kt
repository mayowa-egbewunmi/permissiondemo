package com.mayowa.permissiondemo.ui.screens

import android.Manifest
import android.app.Application
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.permissiondemo.models.PermissionAction
import com.mayowa.permissiondemo.models.PermissionAction.Proceed
import com.mayowa.permissiondemo.models.PermissionAction.RequestPermission
import com.mayowa.permissiondemo.models.PermissionAction.ShowRationale
import com.mayowa.permissiondemo.models.PermissionMeta
import com.mayowa.permissiondemo.models.Photo
import com.mayowa.permissiondemo.utils.PermissionUtil
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
                onPermissionStateUpdated(event.requestedPermissions, event.unapprovedPermissions)
            }

            is Event.TakePhotoTapped -> {
                onTakePhotoTapped(event.requiredPermissions, event.unapprovedPermissions)
            }

            is Event.OnPendingIntentConsumed -> {
                onPendingIntentConsumed()
            }

            is Event.OnScreenLaunched -> {
                onScreenLaunch(event.unapprovedPermissions)
            }

            Event.GetStartedButtonTapped -> onGetStartedButtonTapped()
            Event.RefreshRequested -> onSelectedPhotosUpdated()
        }
    }

    private fun onTakePhotoTapped(
        requiredPermissions: Set<String>,
        unapprovedPermissions: Set<PermissionMeta>,
    ) {
        val pendingPermissionIntent = UiIntent.LaunchCameraScreen(requiredPermissions)
        _state.update { it.copy(pendingPermissionIntent = pendingPermissionIntent) }
        resolvePendingPermissionIntent(pendingPermissionIntent, unapprovedPermissions)
    }

    private fun onScreenLaunch(unapprovedPermissions: Set<PermissionMeta>) {
        val pendingPermissionIntent = _state.value.pendingPermissionIntent
        if (pendingPermissionIntent != null) {
            resolvePendingPermissionIntent(pendingPermissionIntent, unapprovedPermissions)
        }
    }

    private fun onPermissionStateUpdated(
        requestedPermissions: Set<String>,
        unapprovedPermissions: Set<PermissionMeta>,
    ) {
        permissionUtil.cacheRequestedPermissions(requestedPermissions)
        val pendingPermissionIntent = _state.value.pendingPermissionIntent
        if (pendingPermissionIntent != null) {
            resolvePendingPermissionIntent(pendingPermissionIntent, unapprovedPermissions)
        }
    }

    private fun onPendingIntentConsumed() {
        _state.update { it.copy(pendingPermissionIntent = null) }
    }

    private fun resolvePendingPermissionIntent(
        pendingUiIntent: UiIntent,
        unapprovedPermissions: Set<PermissionMeta>,
    ) {

        val permissionsToRequest = pendingUiIntent.requiredPermissions
            .mapNotNull { neededPermission -> unapprovedPermissions.firstOrNull { it.permission == neededPermission } }
            .toSet()

        when {
            permissionsToRequest.isEmpty() -> {
                _state.update {
                    it.copy(permissionAction = Proceed(emptySet(), state.value.pendingPermissionIntent))
                }
            }

            permissionsToRequest.all { it.shouldShowRationale } -> {
                _state.update {
                    it.copy(permissionAction = ShowRationale(permissionsToRequest, false))
                }
            }

            permissionsToRequest.any { it.requiresSettings } -> {
                _state.update {
                    it.copy(permissionAction = ShowRationale(permissionsToRequest, true))
                }
            }

            else -> {
                _state.update {
                    it.copy(permissionAction = RequestPermission(permissionsToRequest))
                }
            }
        }
    }

    private fun onPermissionRequestCancelled() {
        _state.update { it.copy(pendingPermissionIntent = null) }
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


    data class State(
        val pendingPermissionIntent: UiIntent? = null,
        val permissionAction: PermissionAction? = null,
        val photoUris: List<Photo> = emptyList(),
    )

    sealed class UiIntent {
        abstract val requiredPermissions: Set<String>

        data class LaunchCameraScreen(override val requiredPermissions: Set<String>) : UiIntent()
    }

    sealed class Event {
        data object OnPermissionRequestCancelled : Event()
        data object OnPendingIntentConsumed : Event()
        data class OnScreenLaunched(val unapprovedPermissions: Set<PermissionMeta>) : Event()
        data class PermissionStateUpdated(val requestedPermissions: Set<String>, val unapprovedPermissions: Set<PermissionMeta>) : Event()
        data object GetStartedButtonTapped : Event()
        data object RefreshRequested : Event()
        data class TakePhotoTapped(val requiredPermissions: Set<String>, val unapprovedPermissions: Set<PermissionMeta>) : Event()
    }

    sealed class Effect

    private fun emitEffect(effect: Effect) = viewModelScope.launch {
        _effect.emit(effect)
    }

    companion object {
        val MEDIA_PERMISSIONS = buildSet {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        val CAMERA_PERMISSIONS = setOf(Manifest.permission.CAMERA)

        val ALL_PERMISSIONS = MEDIA_PERMISSIONS + CAMERA_PERMISSIONS
    }
}