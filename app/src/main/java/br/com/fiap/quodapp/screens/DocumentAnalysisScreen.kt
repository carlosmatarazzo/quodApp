package br.com.fiap.quodapp.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import br.com.fiap.quodapp.components.Menu
import br.com.fiap.quodapp.components.QuodLogo
import br.com.fiap.quodapp.screens.camera.capturePhoto
import br.com.fiap.quodapp.screens.camera.startCamera
import br.com.fiap.quodapp.screens.utils.toBase64
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.camera.core.ImageCapture
import androidx.compose.ui.draw.clip
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import br.com.fiap.quodapp.screens.utils.getCurrentUtcDateTimeIso

@Composable
fun DocumentAnalysisScreen(navigateTo: (String) -> Unit) {
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
    var isLoading by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    var messageColor by remember(colorScheme) {
        mutableStateOf(colorScheme.onBackground)
    }
    val scrollState = rememberScrollState()
    val shouldShowCameraBox = previewView != null || imageBitmap != null

    val httpClient = remember {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                imageBitmap = bitmap
                cameraStatus = "Validar"
                previewView = null
            } catch (e: Exception) {
                Log.e("GallerySelect", "Erro ao selecionar imagem da galeria", e)
            }
        }
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
            text = "Análise de Documento",
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
            Button(
                onClick = {
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
                            imageBitmap?.let { bitmap ->
                                val base64Image = bitmap.toBase64()

                                val requestPayload = BiometriaRequest(
                                    tipoBiometria = "documento",
                                    dispositivo = Dispositivo(
                                        fabricante = Build.MANUFACTURER,
                                        modelo = Build.MODEL,
                                        sistemaOperacional = Build.VERSION.RELEASE,
                                        dataDispositivo = getCurrentUtcDateTimeIso(),
                                        latitude = -23.1000,
                                        longitude = -46.7500,
                                        ipOrigem = "192.168.1.10"
                                    ),
                                    nomeImagem = "documento.jpg",
                                    imagemBase64 = base64Image
                                )

                                Log.d("API_REQUEST_OBJECT", "Payload Object: $requestPayload")
                                val jsonPayload: String = Json.encodeToString(BiometriaRequest.serializer(), requestPayload)
                                Log.d("API_REQUEST_JSON", "Payload JSON: $jsonPayload")

                                lifecycleOwner.lifecycleScope.launch {
                                    isLoading = true
                                    try {
                                        val response: BiometriaResponse = httpClient.post("http://192.168.56.1:8080/api/biometria/receber") {
                                            contentType(ContentType.Application.Json)
                                            setBody(requestPayload)
                                        }.body()

                                        val statusLower = response.status.lowercase()

                                        when {
                                            statusLower.startsWith("inválido") -> {
                                                message = "Status: Fraude"
                                                messageColor = colorScheme.error
                                                Log.d("API_SUCCESS", "Status de fraude detectado: ${response.status}")
                                            }
                                            statusLower.startsWith("válido") -> {
                                                message = "Status: Sucesso"
                                                messageColor = colorScheme.primary
                                                Log.d("API_SUCCESS", "Status de sucesso detectado: ${response.status}")
                                            }
                                            else -> {
                                                message = "Status desconhecido: ${response.status}"
                                                messageColor = colorScheme.onBackground
                                                Log.w("API_WARNING", "Status inesperado recebido: ${response.status}")
                                            }
                                        }

                                    } catch (e: Exception) {
                                        message = "Erro inesperado ao chamar a API: ${e.localizedMessage}"
                                        messageColor = colorScheme.error
                                        Log.e("API_ERROR", "Erro inesperado na chamada da API", e)
                                    } finally {
                                        isLoading = false
                                        cameraStatus = "Câmera"
                                        imageBitmap = null
                                        previewView = null
                                    }
                                }
                            } ?: run {
                                message = "Nenhuma imagem selecionada para validar."
                                messageColor = colorScheme.error
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(cameraStatus)
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }

            Button(
                onClick = {
                    galleryLauncher.launch("image/*")
                }
            ) {
                Text("Galeria")
            }

            Button(
                onClick = {
                    message = ""
                    navigateTo("sim_swap")
                }
            ) {
                Text("Avançar")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Menu(navigateTo = navigateTo)
    }
}
