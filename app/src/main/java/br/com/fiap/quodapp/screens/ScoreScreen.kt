package br.com.fiap.quodapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun ScoreScreen(navigateTo: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Transparent) }
    var cpf by remember { mutableStateOf("") }
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
            text = "Score",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo CPF
        OutlinedTextField(
            value = cpf,
            onValueChange = { cpf = it },
            label = { Text("CPF") },
            modifier = Modifier.fillMaxWidth()
        )

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = messageColor,
                fontSize = 18.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Button(
                onClick = {
                    if (cpf.isBlank()) {
                        message = "Por favor, preencha o CPF antes de consultar."
                        messageColor = Color.Red
                    } else {
                        if (isInvalid) {
                            message = "CPF Não Encontrado"
                            messageColor = Color.Red
                        } else {
                            message = ""
                            messageColor = Color.Transparent
                            navigateTo("final_score_screen")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Consultar")
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

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 2.dp,
            color = Color.Gray
        )

        Menu(navigateTo = navigateTo)
    }
}
