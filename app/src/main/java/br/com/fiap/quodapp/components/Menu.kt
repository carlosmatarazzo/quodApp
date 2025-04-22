package br.com.fiap.quodapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Menu(navigateTo: (String) -> Unit) {
    val menuItems = mapOf(
        "Início" to "main",
        "Reconhecimento Facial" to "biometria_facial",
        "Biometria Digital" to "biometria_digital",
        "Documento" to "analise_documento",
        "Chip SIM" to "sim_swap",
        "Cadastro" to "cadastro",
        "Score" to "score"
    )

    menuItems.forEach { (label, route) ->
        Text(
            text = label,
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { navigateTo(route) }
                .padding(5.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
