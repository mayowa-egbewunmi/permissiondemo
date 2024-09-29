@file:OptIn(ExperimentalMaterial3Api::class)

package com.mayowa.permissiondemo.ui.screens.photo

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector.LensFacing
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FlashMode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mayowa.permissiondemo.AppScaffold
import com.mayowa.permissiondemo.cameramanager.CameraLensFeatures
import com.mayowa.permissiondemo.cameramanager.PhotoCaptureManager
import com.mayowa.permissiondemo.cameramanager.PhotoPreviewState
import com.mayowa.permissiondemo.cameramanager.PhotoResult
import com.mayowa.permissiondemo.ui.composables.CameraCaptureIcon
import com.mayowa.permissiondemo.ui.composables.CameraCloseIcon
import com.mayowa.permissiondemo.ui.composables.CameraFlashIcon
import com.mayowa.permissiondemo.ui.composables.CameraFlipIcon
import com.mayowa.permissiondemo.ui.composables.CameraPermissionSettingsDialog
import com.mayowa.permissiondemo.ui.composables.PermissionRationaleDialog
import com.mayowa.permissiondemo.utils.PermissionUtil
import com.mayowa.permissiondemo.utils.getActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureScreen(
    navigationController: NavController,
    viewModel: PhotoCaptureViewModel,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val listener = remember(viewModel) {
        object : PhotoCaptureManager.PhotoListener {
            override fun onInitialised(cameraLensInfo: HashMap<Int, CameraLensFeatures>) {
                viewModel.onEvent(PhotoCaptureViewModel.Event.CameraInitialized(cameraLensInfo))
            }

            override fun onSuccess(imageResult: PhotoResult) {
                viewModel.onEvent(PhotoCaptureViewModel.Event.ImageCaptured(imageResult))
            }

            override fun onError(exception: Exception) {
                // TODO: pop up error snackbar
            }
        }
    }

    val captureManager = remember(context, lifecycleOwner, listener) {
        PhotoCaptureManager.Builder(context)
            .setLifecycleOwner(lifecycleOwner)
            .setListener(listener)
            .build()
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PhotoCaptureViewModel.Effect.CaptureImage -> captureManager.takePhoto(effect.path)
                is PhotoCaptureViewModel.Effect.CloseScreen -> navigationController.navigateUp()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.onEvent(PhotoCaptureViewModel.Event.Init)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AppScaffold(topBar = {}) { innerPadding ->
        PhotoCapturePreview(
            modifier = Modifier.padding(innerPadding),
            flashSupported = state.flashSupported(),
            flashMode = state.flashMode,
            flipSupported = state.flipSupported(),
            cameraLens = state.cameraLens,
            captureManager = captureManager,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun PhotoCapturePreview(
    modifier: Modifier,
    flashSupported: Boolean,
    @FlashMode flashMode: Int,
    flipSupported: Boolean,
    @LensFacing cameraLens: Int?,
    captureManager: PhotoCaptureManager,
    onEvent: (PhotoCaptureViewModel.Event) -> Unit,
) {
    val context = LocalContext.current.getActivity()
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        cameraLens?.let {
            val permissions = PermissionUtil.requiredPermissions(context, PhotoCaptureViewModel.requiredPermissions)
            when {
                permissions.isEmpty() -> {
                    PhotoCameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        captureManager = captureManager,
                        lens = it,
                        flashMode = flashMode
                    )
                }

                PermissionUtil.shouldShowRequestPermissionRationale(context, permissions) -> {
                    PermissionRationaleDialog(
                        requiredPermissions = permissions,
                        onClose = { onEvent(PhotoCaptureViewModel.Event.ClosePermissionDialogButtonTapped) },
                        onRequestPermission = {
                            permissionLauncher.launch(permissions.toTypedArray())
                        }
                    )
                }

                else -> {
                    CameraPermissionSettingsDialog(
                        requiredPermissions = permissions,
                        onClose = { onEvent(PhotoCaptureViewModel.Event.ClosePermissionDialogButtonTapped) },
                        onSettingsTapped = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", context.packageName, null)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
        CaptureHeader(
            modifier = Modifier
                .fillMaxWidth(1f)
                .align(Alignment.TopStart),
            flashSupported = flashSupported,
            flashMode = flashMode,
            onFlashTapped = { onEvent(PhotoCaptureViewModel.Event.FlashTapped) },
            onCloseTapped = { onEvent(PhotoCaptureViewModel.Event.CloseTapped) }
        )
        cameraLens?.let {
            CaptureFooter(
                flipSupported = flipSupported,
                onCaptureTapped = { /* TODO */ },
                onFlipTapped = { onEvent(PhotoCaptureViewModel.Event.FlipCameraLensTapped) },
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
private fun PhotoCameraPreview(
    modifier: Modifier,
    captureManager: PhotoCaptureManager,
    @LensFacing lens: Int,
    @FlashMode flashMode: Int,
) {
    BoxWithConstraints(modifier = Modifier.then(modifier)) {
        val size = Size(this.constraints.maxWidth, this.constraints.maxHeight)
        AndroidView(
            factory = { captureManager.showPreview(PhotoPreviewState(cameraLens = lens, flashMode = flashMode, size = size)) },
            modifier = Modifier.matchParentSize(),
            update = { captureManager.updatePreview(PhotoPreviewState(cameraLens = lens, flashMode = flashMode, size = size)) }
        )
    }
}

@Composable
private fun CaptureHeader(
    modifier: Modifier = Modifier,
    flashSupported: Boolean,
    @FlashMode flashMode: Int,
    onFlashTapped: () -> Unit,
    onCloseTapped: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, end = 16.dp)
            .then(modifier)
    ) {
        if (flashSupported) {
            CameraFlashIcon(flashEnabled = flashMode == ImageCapture.FLASH_MODE_ON, onTapped = onFlashTapped)
        }
        CameraCloseIcon(onTapped = onCloseTapped, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun CaptureFooter(
    modifier: Modifier,
    flipSupported: Boolean,
    onCaptureTapped: () -> Unit,
    onFlipTapped: () -> Unit,
) {
    val captureInProgress = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .then(modifier)
    ) {
        CameraCaptureIcon(modifier = Modifier.align(Alignment.Center)) {
            captureInProgress.value = true
            onCaptureTapped()
        }
        if (captureInProgress.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
            )
        }
        if (flipSupported) {
            CameraFlipIcon(modifier = Modifier.align(Alignment.CenterEnd), onTapped = onFlipTapped)
        }
    }
}