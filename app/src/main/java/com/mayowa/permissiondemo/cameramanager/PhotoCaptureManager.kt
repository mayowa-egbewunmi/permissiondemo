package com.mayowa.permissiondemo.cameramanager

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.os.Build
import android.util.Rational
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class PhotoCaptureManager private constructor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val listener: PhotoListener,
    private val preferredLens: Int?,
) : DefaultLifecycleObserver {

    private var previewWidthInPixel: Int? = null
    private var previewHeightInPixel: Int? = null

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture: ImageCapture
    private lateinit var preview: Preview
    private lateinit var camera: Camera

    private val previewView by lazy {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            keepScreenOn = true
        }
    }

    private val cameraProviderListener = Runnable {
        val cameraProvider = cameraProviderFuture.get()
        queryCameraInfo(lifecycleOwner, cameraProvider)
    }

    init {
        getLifecycle().addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(cameraProviderListener, ContextCompat.getMainExecutor(context))
        observeOrientationChanges()
    }

    private fun observeOrientationChanges() {
        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation: Int = getRotation(orientation)
                if (::imageCapture.isInitialized) {
                    imageCapture.targetRotation = rotation
                }
                if (::preview.isInitialized) {
                    preview.targetRotation = rotation
                }
            }
        }
        orientationEventListener.enable()
    }

    private fun getRotation(orientation: Int) = when (orientation) {
        in 45..134 -> Surface.ROTATION_270
        in 135..224 -> Surface.ROTATION_180
        in 225..314 -> Surface.ROTATION_90
        else -> Surface.ROTATION_0
    }

    private fun getLifecycle() = lifecycleOwner.lifecycle

    /**
     * Queries the capabilities of the FRONT and BACK camera lens
     * The result is stored in an array map.
     *
     * With this, we can determine if a camera lens is available or not,
     * and what capabilities the lens can support e.g flash support
     */
    private fun queryCameraInfo(
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider,
    ) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return

        val cameraLensInfo = HashMap<Int, CameraLensFeatures>()
        arrayOf(CameraSelector.LENS_FACING_BACK, CameraSelector.LENS_FACING_FRONT).forEach { lens ->
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lens).build()
            if (cameraProvider.hasCamera(cameraSelector)) {
                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector)
                if (lens == CameraSelector.LENS_FACING_BACK) {
                    cameraLensInfo[CameraSelector.LENS_FACING_BACK] =
                        camera.cameraInfo.toCameraLensFeatures()
                } else if (lens == CameraSelector.LENS_FACING_FRONT) {
                    cameraLensInfo[CameraSelector.LENS_FACING_FRONT] =
                        camera.cameraInfo.toCameraLensFeatures()
                }
            }
            cameraProvider.unbindAll() //Unbind camera provider when query is completed
        }
        listener.onInitialised(cameraLensInfo)
    }

    /**
     * Takes a [photoPreviewState] argument to determine the camera options
     *
     * Create a Preview.
     * Create Image Capture use case
     * Bind the selected camera and any use cases to the lifecycle.
     * Connect the Preview to the PreviewView.
     */
    fun showPreview(photoPreviewState: PhotoPreviewState): View {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                previewHeightInPixel = photoPreviewState.size.height
                previewWidthInPixel = photoPreviewState.size.width

                val cameraProvider = cameraProviderFuture.await()
                cameraProvider.unbindAll()

                //Select a camera lens
                val cameraSelector: CameraSelector = CameraSelector.Builder()
                    .requireLensFacing(photoPreviewState.cameraLens)
                    .build()

                val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    context.display?.rotation ?: Surface.ROTATION_0
                } else {
                    (context.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
                }

                //Create Preview use case
                preview = Preview.Builder()
                    .setTargetRotation(rotation)
                    .build()
                    .apply { surfaceProvider = previewView.surfaceProvider }

                //Create Image Capture use case
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(rotation)
                    .setFlashMode(photoPreviewState.flashMode)
                    .build()

                val viewPort = ViewPort.Builder(Rational(photoPreviewState.size.width, photoPreviewState.size.height), rotation)
                    .setScaleType(ViewPort.FILL_CENTER)
                    .build()

                val useCaseGroupBuilder = UseCaseGroup.Builder()
                    .setViewPort(viewPort)
                    .addUseCase(preview)
                    .addUseCase(imageCapture)

                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    useCaseGroupBuilder.build()
                )
            }
        }
        return previewView
    }

    fun updatePreview(photoPreviewState: PhotoPreviewState) {
        showPreview(photoPreviewState)
    }

    fun takePhoto(filePath: String) {
        val file = File(filePath)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    Timber.e(error)
                    listener.onError(error)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val displayMetric = context.resources.displayMetrics
                    listener.onSuccess(
                        outputFileResults.toPhotoResult(
                            previewWidthInPixel = previewWidthInPixel ?: displayMetric.widthPixels,
                            previewHeightInPixel = previewHeightInPixel
                                ?: displayMetric.heightPixels
                        )
                    )
                }
            })
    }

    class Builder(private val context: Context) {
        private var lifecycleOwner: LifecycleOwner? = null
        private var preferredLens: Int? = null
        private var listener: PhotoListener? = null

        fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) = apply {
            this.lifecycleOwner = lifecycleOwner
        }

        fun setListener(listener: PhotoListener) = apply {
            this.listener = listener
        }

        fun setPreferredLens(lens: Int?) = apply {
            this.preferredLens = lens
        }

        fun build(): PhotoCaptureManager {
            requireNotNull(lifecycleOwner) { "Lifecycle owner is not set" }
            return PhotoCaptureManager(context, lifecycleOwner!!, listener!!, preferredLens)
        }
    }

    interface PhotoListener {
        fun onInitialised(cameraLensInfo: HashMap<Int, CameraLensFeatures>)
        fun onSuccess(imageResult: PhotoResult)
        fun onError(exception: Exception)
    }

    fun ImageCapture.OutputFileResults.toPhotoResult(previewWidthInPixel: Int, previewHeightInPixel: Int): PhotoResult {
        return PhotoResult(
            path = this.savedUri?.path,
            previewWidthInPixel = previewWidthInPixel,
            previewHeightInPixel = previewHeightInPixel
        )
    }

    private fun CameraInfo.toCameraLensFeatures() =
        CameraLensFeatures(flashSupported = this.hasFlashUnit())
}
