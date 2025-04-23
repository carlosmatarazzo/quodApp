import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.util.concurrent.Executor
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import androidx.lifecycle.LifecycleOwner
import br.com.fiap.quodapp.components.Menu
import br.com.fiap.quodapp.components.QuodLogo

@Composable
fun FacialBiometricsScreen(navigateTo: (String) -> Unit) {

    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Transparent) }
    var cameraStatus by remember { mutableStateOf("Câmera") }
    var isInvalid by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to hold the captured image URI (for display, if needed)
    val capturedImageUri = remember { mutableStateOf<Uri?>(null) }

    // CameraX setup
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, set up camera
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    imageCapture = setupCamera(context, lifecycleOwner, cameraProvider)
                }, ContextCompat.getMainExecutor(context))
            } else {
                message = "Permissão de câmera negada"
                messageColor = Color.Red
            }
        }
    )

// Function to take the picture
    val takePhoto: () -> Unit = {
        val currentImageCapture = imageCapture // Store in a local variable
        if (currentImageCapture != null) {
            capturedImageUri.value = takePhotoAndGetUri(context, currentImageCapture)
            if (capturedImageUri.value != null) {
                cameraStatus = "Validar"
            }
        } else {
            Log.e("Camera", "imageCapture is null")
            // Optionally mostrar uma mensagem de erro ao usuário
        }
    }

    // Function to request camera permission
    val requestCameraPermission = {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        QuodLogo()

        Text(
            text = "Reconhecimento Facial",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.Gray)
        ) {
            // Live Camera feed
            if (cameraStatus == "Câmera" || cameraStatus == "Capturar") {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    cameraProviderFuture = cameraProviderFuture
                )
            }

            // Captured Image (URI)
            capturedImageUri.value?.let { uri ->
                Text(
                    text = "Imagem Capturada",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                //Exibir a imagem capturada, se necessário
                //Image(
                //  painter = rememberAsyncImagePainter(uri),
                //  contentDescription = "Captured Image",
                //  modifier = Modifier.fillMaxSize()
                //)
            }
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = messageColor,
                fontSize = 18.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 0.dp)
        ) {
            Button(
                onClick = {
                    when (cameraStatus) {
                        "Câmera" -> {
                            requestCameraPermission()
                            cameraStatus = "Capturar"
                            message = ""
                            messageColor = Color.Transparent
                        }

                        "Capturar" -> {
                            takePhoto()
                            //cameraStatus = "Validar" // Agora tratado em takePhoto
                        }

                        "Validar" -> {
                            if (isInvalid) {
                                message = "Imagem Inválida"
                                messageColor = Color.Red
                            } else {
                                message = "Imagem Válida"
                                messageColor = Color.Black
                                // Aqui você irá enviar a imagem para a API
                                // Use capturedImageUri.value para obter a URI da imagem capturada
                                // Implemente a lógica de envio (e.g., usando Retrofit, Ktor, etc.)
                                Log.d("FacialBiometrics", "URI da imagem capturada: ${capturedImageUri.value}")
                                // Exemplo de como obter os bytes da imagem (se precisar enviar bytes):
                                // val imageBytes = context.contentResolver.openInputStream(capturedImageUri.value!!)?.readBytes()

                                // Após o envio (ou tentativa de envio), você pode resetar o fluxo:
                                // Reiniciar fluxo
                                //liveImage = false
                                //capturedImage = false
                                cameraStatus = "Câmera"
                                capturedImageUri.value = null // Limpar a URI
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(cameraStatus)
            }

            Button(
                onClick = {
                    message = ""
                    messageColor = Color.Transparent
                    navigateTo("biometria_digital")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(top = 0.dp)
            ) {
                Text("Avançar")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Checkbox(
                checked = isInvalid,
                onCheckedChange = { isInvalid = it }
            )
            Text("Invalidar")
        }

        Spacer(modifier = Modifier.height(0.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 2.dp,
            color = Color.Gray
        )

        Menu(navigateTo = navigateTo)
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
) {
    val context = LocalContext.current // Get context outside remember
    val preview = remember { Preview.Builder().build() }
    val previewView = remember { PreviewView(context) } // Use the obtained context
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner, cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview
            )
        } catch (exc: Exception) {
            Log.e("CameraPreview", "Use case binding failed", exc)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier
            .size(150.dp) // MESMO TAMANHO DO BOX CINZA
    )
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider
): ImageCapture {
    val preview = Preview.Builder().build().apply {
        //this.setSurfaceProvider(previewView.surfaceProvider)
    }

    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    val imageCapture = ImageCapture.Builder().build()

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    } catch (e: Exception) {
        Log.e("setupCamera", "CameraX binding failed", e)
    }

    return imageCapture
}

private fun takePhotoAndGetUri(context: Context, imageCapture: ImageCapture): Uri? {
    val photoFile = File.createTempFile("captured_image", ".jpg", context.cacheDir)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    var capturedImageUri: Uri? = null

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                capturedImageUri = Uri.fromFile(photoFile)
                Log.d("Camera", "Foto tirada: ${capturedImageUri}")
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "Erro ao tirar foto: ${exception.message}", exception)
            }
        })
    return capturedImageUri
}