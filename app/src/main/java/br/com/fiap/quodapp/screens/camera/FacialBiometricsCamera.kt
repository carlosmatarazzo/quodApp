package br.com.fiap.quodapp.screens.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.YuvImage
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

fun startCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onPreviewCreated: (Preview) -> Unit,
    onPreviewViewCreated: (PreviewView) -> Unit,
    onImageCaptureCreated: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({

        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        val rotation = windowManager.defaultDisplay.rotation

        val targetRotation = when (rotation) {
            Surface.ROTATION_0 -> Surface.ROTATION_0
            Surface.ROTATION_90 -> Surface.ROTATION_90
            Surface.ROTATION_180 -> Surface.ROTATION_180
            Surface.ROTATION_270 -> Surface.ROTATION_270
            else -> Surface.ROTATION_0
        }

        val capture = ImageCapture.Builder()
            .setTargetRotation(targetRotation)
            .build()

        val previewViewInstance = PreviewView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        preview.setSurfaceProvider(previewViewInstance.surfaceProvider)

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            capture
        )

        onPreviewCreated(preview)
        onPreviewViewCreated(previewViewInstance)
        onImageCaptureCreated(capture)
    }, ContextCompat.getMainExecutor(context))
}

fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onBitmapCaptured: (Bitmap) -> Unit
) {
    val imageCaptureInstance = imageCapture ?: return
    val executor: Executor = ContextCompat.getMainExecutor(context)
    imageCaptureInstance.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                onBitmapCaptured(bitmap)
                image.close()
                Log.d("ROTATION_DEBUG", "rotationDegrees = ${image.imageInfo.rotationDegrees}")
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraCapture", "Erro ao capturar imagem", exception)
            }
        }
    )
}

fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    val originalBitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    val matrix = Matrix()
    matrix.postRotate(imageInfo.rotationDegrees.toFloat())

    val rotatedBitmap = Bitmap.createBitmap(
        originalBitmap,
        0,
        0,
        originalBitmap.width,
        originalBitmap.height,
        matrix,
        true
    )

    val mirrorMatrix = Matrix()
    mirrorMatrix.postScale(-1f, 1f, rotatedBitmap.width / 2f, rotatedBitmap.height / 2f)

    return Bitmap.createBitmap(
        rotatedBitmap,
        0,
        0,
        rotatedBitmap.width,
        rotatedBitmap.height,
        mirrorMatrix,
        true
    )
}
