package com.mayowa.permissiondemo.cameramanager

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture

data class PhotoPreviewState(
    @ImageCapture.FlashMode val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    @CameraSelector.LensFacing val cameraLens: Int = CameraSelector.LENS_FACING_BACK,
    val size: Size
)

data class PhotoResult(val path: String?, val previewWidthInPixel: Int, val previewHeightInPixel: Int)

data class CameraLensFeatures(val flashSupported: Boolean)