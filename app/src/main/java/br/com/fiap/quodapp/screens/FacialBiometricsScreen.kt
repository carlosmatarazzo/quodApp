package br.com.fiap.quodapp.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.quodapp.components.Menu
import br.com.fiap.quodapp.components.QuodLogo

@Composable
fun FacialBiometricsScreen(navigateTo: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Transparent) }
    var cameraStatus by remember { mutableStateOf("Câmera") }
    var liveImage by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf(false) }
    var isInvalid by remember { mutableStateOf(false) }

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
            when {
                liveImage -> Text(
                    text = "Imagem ao Vivo",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                capturedImage -> Text(
                    text = "Imagem Capturada",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> Text(
                    text = "",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
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
                            liveImage = true
                            capturedImage = false
                            cameraStatus = "Capturar"
                            message = ""
                            messageColor = Color.Transparent
                        }

                        "Capturar" -> {
                            liveImage = false
                            capturedImage = true
                            cameraStatus = "Validar"
                        }

                        "Validar" -> {
                            if (isInvalid) {
                                message = "Imagem Inválida"
                                messageColor = Color.Red
                            } else {
                                message = "Imagem Válida"
                                messageColor = Color.Black
                            }
                            // Reiniciar fluxo
                            liveImage = false
                            capturedImage = false
                            cameraStatus = "Câmera"
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
