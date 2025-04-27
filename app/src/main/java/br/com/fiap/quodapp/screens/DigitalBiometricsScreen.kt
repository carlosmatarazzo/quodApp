package br.com.fiap.quodapp.screens

import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import br.com.fiap.quodapp.components.Menu
import br.com.fiap.quodapp.components.QuodLogo
import br.com.fiap.quodapp.screens.utils.findActivity
import br.com.fiap.quodapp.screens.utils.getCurrentUtcDateTimeIso
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.lifecycle.lifecycleScope

@Serializable
data class DigitalBiometriaRequest(
    val tipoBiometria: String = "digital",
    val status: String,
    val dataCaptura: String,
    val dispositivo: Dispositivo
)

@Composable
fun DigitalBiometricsScreen(navigateTo: (String) -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity() as? FragmentActivity

    var message by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme
    var messageColor by remember(colorScheme) {
        mutableStateOf(colorScheme.onBackground)
    }
    var isInvalid by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var biometricStatus by remember { mutableStateOf<String?>(null) }
    var buttonLabel by remember { mutableStateOf("Capturar") }
    val scrollState = rememberScrollState()

    val executor = remember { ContextCompat.getMainExecutor(context) }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirme sua Identidade")
            .setSubtitle("Toque o sensor de digital")
            .setNegativeButtonText("Cancelar")
            .build()
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuodLogo()

        Text(
            text = "Biometria Digital",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = messageColor,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Button(
                onClick = {
                    when (buttonLabel) {
                        "Capturar" -> {
                            val biometricManager = BiometricManager.from(context)
                            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                                BiometricManager.BIOMETRIC_SUCCESS -> {
                                    activity?.let { safeActivity ->
                                        val biometricPrompt = BiometricPrompt(
                                            safeActivity,
                                            executor,
                                            object : BiometricPrompt.AuthenticationCallback() {
                                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                    super.onAuthenticationSucceeded(result)
                                                    biometricStatus = "sucesso_autenticacao"
                                                    buttonLabel = "Validar"
                                                    message = "Biometria capturada com sucesso. Clique em Validar."
                                                    messageColor = colorScheme.primary
                                                    Log.d("BIOMETRIA", "Captura bem-sucedida.")
                                                }

                                                override fun onAuthenticationFailed() {
                                                    super.onAuthenticationFailed()
                                                    biometricStatus = "falha_autenticacao"
                                                    buttonLabel = "Validar"
                                                    message = "Falha ao capturar biometria. Clique em Validar."
                                                    messageColor = colorScheme.error
                                                    Log.d("BIOMETRIA", "Falha ao capturar.")
                                                }

                                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                    super.onAuthenticationError(errorCode, errString)
                                                    biometricStatus = "falha_autenticacao"
                                                    buttonLabel = "Validar"
                                                    message = "Erro ao capturar biometria. Clique em Validar."
                                                    messageColor = colorScheme.error
                                                    Log.e("BIOMETRIA", "Erro $errorCode: $errString")
                                                }
                                            }
                                        )
                                        biometricPrompt.authenticate(promptInfo)
                                    } ?: run {
                                        message = "Erro: Activity inválida"
                                        messageColor = colorScheme.error
                                    }
                                }
                                else -> {
                                    message = "Biometria não disponível no dispositivo."
                                    messageColor = colorScheme.error
                                }
                            }
                        }

                        "Validar" -> {
                            biometricStatus?.let { statusCaptured ->
                                activity?.lifecycleScope?.launch {
                                    isLoading = true
                                    try {
                                        val dispositivo = Dispositivo(
                                            fabricante = Build.MANUFACTURER,
                                            modelo = Build.MODEL,
                                            sistemaOperacional = Build.VERSION.RELEASE,
                                            dataDispositivo = getCurrentUtcDateTimeIso(),
                                            latitude = -23.55052,
                                            longitude = -46.633308,
                                            ipOrigem = "179.234.56.12"
                                        )

                                        val payload = DigitalBiometriaRequest(
                                            status = statusCaptured,
                                            dataCaptura = getCurrentUtcDateTimeIso(),
                                            dispositivo = dispositivo
                                        )

                                        Log.d("API_REQUEST", "Payload enviado: $payload")

                                        val response = httpClient.post("http://192.168.56.1:8080/api/biometria/receber") {
                                            contentType(ContentType.Application.Json)
                                            setBody(payload)
                                        }

                                        if (statusCaptured == "sucesso_autenticacao") {
                                            message = "Status: Sucesso"
                                            messageColor = colorScheme.primary
                                        } else {
                                            message = "Status: Fraude"
                                            messageColor = colorScheme.error
                                        }

                                        buttonLabel = "Capturar"
                                        biometricStatus = null

                                        Log.d("API_SUCCESS", "Resposta da API: ${response.status}")
                                    } catch (e: Exception) {
                                        message = "Erro ao validar biometria: ${e.localizedMessage}"
                                        messageColor = colorScheme.error
                                        Log.e("API_ERROR", "Erro ao enviar biometria digital", e)
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } ?: run {
                                message = "Nenhuma biometria capturada ainda."
                                messageColor = colorScheme.error
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(buttonLabel)
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }

            Button(
                onClick = {
                    message = ""
                    navigateTo("analise_documento")
                }
            ) {
                Text("Avançar")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Menu(navigateTo = navigateTo)
    }
}
