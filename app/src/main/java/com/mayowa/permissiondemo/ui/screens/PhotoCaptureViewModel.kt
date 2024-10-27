package com.mayowa.permissiondemo.ui.screens

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.permissiondemo.cameramanager.CameraLensFeatures
import com.mayowa.permissiondemo.cameramanager.PhotoResult
import com.mayowa.permissiondemo.di.ioDispatcher
import com.mayowa.permissiondemo.models.PermissionAction
import com.mayowa.permissiondemo.models.PermissionAction.ShowRationale
import com.mayowa.permissiondemo.utils.MediaStorageUtil
import com.mayowa.permissiondemo.utils.PermissionUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PhotoCaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaStorageUtil: MediaStorageUtil,
    private val permissionUtil: PermissionUtil,
    @ioDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: Event) {
        when (event) {
            Event.CaptureTapped -> onCaptureTapped()
            Event.CloseTapped -> onCloseTapped()
            Event.FlipCameraLensTapped -> onFlipCameraLensTapped()
            Event.FlashTapped -> onFlashTapped()
            Event.RetakeTapped -> onRetakeTapped()
            Event.BackTapped -> onBackTapped()
            Event.ClosePermissionDialogButtonTapped -> onClosePermissionDialogButtonTapped()
            Event.SubmitTapped -> onSubmitTapped()
            is Event.ImageCaptured -> onImageCaptured(event.imageResult)
            is Event.CameraInitialized -> onCameraInitialized(event.cameraLensInfo)
            is Event.PermissionRequirementUpdated -> onPermissionRequirementUpdated(event.unGrantedPermissions, event.isRationaleRequired)
            is Event.OnScreenLaunch -> onScreenLaunch(event.unGrantedPermissions, event.isRationaleRequired)
        }
    }

    private fun onScreenLaunch(unGrantedPermissions: Set<String>, isRationaleRequired: Boolean) {
        updatePermissionState(unGrantedPermissions, isRationaleRequired)
    }

    private fun onPermissionRequirementUpdated(unGrantedPermissions: Set<String>, isRationaleRequired: Boolean) {
        permissionUtil.cacheDeniedPermissions(unGrantedPermissions)
        updatePermissionState(unGrantedPermissions, isRationaleRequired)
    }

    private fun updatePermissionState(unapprovedPermissions: Set<String>, isRationaleRequired: Boolean) {
        when {
            unapprovedPermissions.isEmpty() -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.Proceed(null))
                }
            }

            isRationaleRequired -> {
                _state.update {
                    it.copy(permissionAction = PermissionAction.ShowRationale(unapprovedPermissions, false))
                }
            }
            permissionUtil.isAnyPreviouslyDenied(unapprovedPermissions) -> {
                _state.update { it.copy(permissionAction = ShowRationale(unapprovedPermissions, true)) }
            }

            else -> {
                _state.update {
                    it.copy(
                        permissionAction = PermissionAction.RequestPermission(unapprovedPermissions),
                        ungrantedPermissions = unapprovedPermissions
                    )
                }
            }
        }
    }

    private fun onRetakeTapped() {
        returnToPreview()
    }

    private fun onFlashTapped() {
        val flashMode = if (state.value.flashMode == ImageCapture.FLASH_MODE_OFF) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
        if (state.value.flashSupported()) {
            _state.update { it.copy(flashMode = flashMode) }
        }
    }

    private fun onFlipCameraLensTapped() {
        val lens = if (_state.value.cameraLens == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        //Check if the lens is supported
        if (_state.value.cameraLensInfo[lens] != null) {
            _state.update { it.copy(cameraLens = lens, flashMode = getDefaultFlashMode(lens)) }
        }
    }

    private fun onImageCaptured(imageResult: PhotoResult) {
        requireNotNull(imageResult.path)
        _state.update { it.copy(cameraState = CameraState.PHOTO_CAPTURED, filePath = imageResult.path) }
    }

    private fun onCameraInitialized(lensInfo: Map<Int, CameraLensFeatures>) {
        val lens = getDefaultLens(lensInfo)
        _state.update { it.copy(cameraLensInfo = lensInfo, cameraLens = lens) }
    }

    private fun onCloseTapped() {
        emitEffect(Effect.CloseScreen())
    }


    private fun onClosePermissionDialogButtonTapped() {
        emitEffect(Effect.CloseScreen())
    }

    private fun onBackTapped() {
        if (state.value.cameraState == CameraState.PHOTO_PREVIEW) {
            emitEffect(Effect.CloseScreen())
        } else {
            returnToPreview()
        }
    }

    private fun onSubmitTapped() {
        emitEffect(Effect.CloseScreen(state.value.filePath))
    }

    private fun onCaptureTapped() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val filePath = mediaStorageUtil.createNewImageFile()
                emitEffect(Effect.CaptureImage(filePath))
            } catch (exception: IllegalArgumentException) {
                Timber.e(exception)
            }
        }
    }

    private fun returnToPreview() {
        viewModelScope.launch(ioDispatcher) {
            _state.value.filePath?.let {
                val file = File(it)
                if (file.exists()) {
                    file.delete()
                }
            }
            _state.update { it.copy(filePath = null, cameraState = CameraState.PHOTO_PREVIEW) }
        }
    }

    private fun getDefaultLens(lensInfo: Map<Int, CameraLensFeatures>): Int? {
        val hasFrontLens = lensInfo[CameraSelector.LENS_FACING_FRONT] != null
        val hasBackLens = lensInfo[CameraSelector.LENS_FACING_BACK] != null

        return when {
            hasBackLens -> CameraSelector.LENS_FACING_BACK
            hasFrontLens -> CameraSelector.LENS_FACING_FRONT
            else -> null
        }
    }

    private fun getDefaultFlashMode(lens: Int?): Int {
        return if (_state.value.cameraLensInfo[lens]?.flashSupported == true) {
            _state.value.flashMode
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    data class State(
        val cameraPermissionGranted: Boolean = false,
        val cameraState: CameraState = CameraState.PHOTO_PREVIEW,
        val permissionAction: PermissionAction? = null,
        val ungrantedPermissions: Set<String> = emptySet(),
        val filePath: String? = null,
        val cameraLensInfo: Map<Int, CameraLensFeatures> = mapOf(),
        @CameraSelector.LensFacing val cameraLens: Int? = null,
        @ImageCapture.FlashMode val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
        val processing: Boolean = false,
    )

    enum class CameraState {
        PHOTO_PREVIEW,
        PHOTO_CAPTURED,
    }

    sealed class Event {
        data object CaptureTapped : Event()
        data object CloseTapped : Event()
        data object RetakeTapped : Event()
        data object FlashTapped : Event()
        data object BackTapped : Event()
        data class PermissionRequirementUpdated(val unGrantedPermissions: Set<String>, val isRationaleRequired: Boolean) : Event()
        data class OnScreenLaunch(val unGrantedPermissions: Set<String>, val isRationaleRequired: Boolean) : Event()
        data object ClosePermissionDialogButtonTapped : Event()

        data object SubmitTapped : Event()
        data class ImageCaptured(val imageResult: PhotoResult) : Event()
        data class CameraInitialized(val cameraLensInfo: Map<Int, CameraLensFeatures>) : Event()
        data object FlipCameraLensTapped : Event()
    }

    sealed class Effect {
        data class CloseScreen(val filePath: String? = null) : Effect()
        data class CaptureImage(val path: String) : Effect()
    }

    private fun emitEffect(effect: Effect) = viewModelScope.launch {
        _effect.emit(effect)
    }
}

fun PhotoCaptureViewModel.State.flashSupported(): Boolean {
    if (this.cameraLens == null) return false
    return this.cameraLensInfo[cameraLens]?.flashSupported ?: false
}

fun PhotoCaptureViewModel.State.flipSupported(): Boolean {
    return this.cameraLensInfo.size > 1
}