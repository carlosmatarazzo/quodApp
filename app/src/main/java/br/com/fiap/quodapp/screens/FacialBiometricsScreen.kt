package br.com.fiap.quodapp.screens

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.fiap.quodapp.components.Menu
import br.com.fiap.quodapp.components.QuodLogo
import br.com.fiap.quodapp.screens.camera.capturePhoto
import br.com.fiap.quodapp.screens.camera.startCamera
import br.com.fiap.quodapp.screens.utils.toBase64
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.camera.core.ImageCapture
import androidx.compose.ui.draw.clip

@Composable
fun FacialBiometricsScreen(navigateTo: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.CAMERA
        val granted = ActivityCompat.checkSelfPermission(context, permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!granted && context is ComponentActivity) {
            ActivityCompat.requestPermissions(
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
    var isInvalid by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    var messageColor by remember(colorScheme) {
        mutableStateOf(colorScheme.onBackground)
    }
    val scrollState = rememberScrollState()
    val shouldShowCameraBox = previewView != null || imageBitmap != null

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
                    .height(360.dp)
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                when {
                    imageBitmap != null -> {
                        Image(
                            bitmap = imageBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
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
                        startCamera(context, lifecycleOwner, { preview = it }, { previewView = it }, { imageCapture = it })
                        imageBitmap = null
                        cameraStatus = "Capturar"
                        message = ""
                    }
                    "Capturar" -> {
                        capturePhoto(context, imageCapture, { imageBitmap = it })
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
        Menu(navigateTo = navigateTo)
    }
}