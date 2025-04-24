package br.com.fiap.quodapp.screens

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import br.com.fiap.quodapp.components.Menu
import br.com.fiap.quodapp.components.QuodLogo
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.utils.MatrixExt.postRotate
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import android.view.Surface // Importe a classe Surface do Android

@Composable
fun FacialBiometricsScreen(navigateTo: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        val permission = android.Manifest.permission.CAMERA
        val granted = androidx.core.app.ActivityCompat.checkSelfPermission(context, permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!granted && context is android.app.Activity) {
            androidx.core.app.ActivityCompat.requestPermissions(
                context,
                arrayOf(permission),
                1001
            )
        }
    }

    var preview by remember { mutableStateOf<Preview?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cameraStatus by remember { mutableStateOf("Câmera") }
    var message by remember { mutableStateOf("") }
    //var messageColor by remember { mutableStateOf(MaterialTheme.colorScheme.onBackground) }
    var isInvalid by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    var messageColor by remember(colorScheme) {
        mutableStateOf(colorScheme.onBackground)
    }
    val scrollState = rememberScrollState()
    val shouldShowCameraBox = previewView != null || imageBitmap != null



    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()
            val previewBuilder = Preview.Builder().build()

            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val rotation = windowManager.defaultDisplay.rotation

            val targetRotation = when (rotation) {
                Surface.ROTATION_0 -> Surface.ROTATION_0
                Surface.ROTATION_90 -> Surface.ROTATION_90
                Surface.ROTATION_180 -> Surface.ROTATION_180
                Surface.ROTATION_270 -> Surface.ROTATION_270
                else -> Surface.ROTATION_0 // Valor padrão
            }

            val captureBuilder = ImageCapture.Builder()
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

            previewBuilder.setSurfaceProvider(previewViewInstance.surfaceProvider)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                previewBuilder,
                captureBuilder
            )

            preview = previewBuilder
            previewView = previewViewInstance
            imageCapture = captureBuilder
        }, ContextCompat.getMainExecutor(context))
    }

    fun capturePhoto() {
        val imageCaptureInstance = imageCapture ?: return
        val executor: Executor = ContextCompat.getMainExecutor(context)
        imageCaptureInstance.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    imageBitmap = bitmap
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
        val rotationDegrees = imageInfo.rotationDegrees.toFloat()

        // 1. Aplicar a rotação
        matrix.postRotate(rotationDegrees)

        // 2. Criar uma nova bitmap com a rotação aplicada
        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )

        // 3. Espelhar horizontalmente o bitmap rotacionado
        val matrixMirror = Matrix()
        matrixMirror.postScale(-1f, 1f, rotatedBitmap.width / 2f, rotatedBitmap.height / 2f)

        return Bitmap.createBitmap(
            rotatedBitmap,
            0,
            0,
            rotatedBitmap.width,
            rotatedBitmap.height,
            matrixMirror,
            true
        )
    }

    fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuodLogo()

        Text(
            text = "Reconhecimento Facial",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (shouldShowCameraBox) {
            Box(
                modifier = Modifier
                    .height(360.dp) // altura generosa
                    .fillMaxWidth(0.6f) // largura equivalente a 60% da tela
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp)) // opcional: visual moderno

            ) {
                when {
                    imageBitmap != null -> {
                        androidx.compose.foundation.Image(
                            bitmap = imageBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    previewView != null -> {
                        AndroidView(
                            factory = { previewView!! },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = messageColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Button(onClick = {
                when (cameraStatus) {
                    "Câmera" -> {
                        startCamera()
                        imageBitmap = null
                        cameraStatus = "Capturar"
                        message = ""
                    }

                    "Capturar" -> {
                        capturePhoto()
                        previewView = null
                        cameraStatus = "Validar"
                    }

                    "Validar" -> {
                        if (isInvalid) {
                            message = "Imagem Inválida"
                            messageColor = colorScheme.error
                        } else {
                            message = "Imagem Válida"
                            messageColor = colorScheme.primary
                        }

                        // TODO: Enviar imagem em formato Base64 para API
                        // val base64Image = imageBitmap?.toBase64()
                        cameraStatus = "Câmera"
                        imageBitmap = null
                        previewView = null
                    }
                }
            }) {
                Text(cameraStatus)
            }

            Button(onClick = {
                message = ""
                navigateTo("biometria_digital")
            }) {
                Text("Avançar")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Checkbox(
                checked = isInvalid,
                onCheckedChange = { isInvalid = it }
            )
            Text("Invalidar")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Esse menu agora será visível, pois o conteúdo rola
        Menu(navigateTo = navigateTo)
    }
}