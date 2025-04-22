package br.com.fiap.quodapp.screens

import androidx.compose.foundation.layout.*
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
import kotlin.random.Random

@Composable
fun FinalScoreScreen(navigateTo: (String) -> Unit) {

    val randomScore = remember { Random.nextInt(0, 1001) }

    val scoreCategory = when {
        randomScore in 0..250 -> "Ruim"
        randomScore in 251..500 -> "Razoável"
        randomScore in 501..750 -> "Bom"
        else -> "Ótimo"
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
            text = "Score Atual",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "$randomScore ($scoreCategory)",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "0 a 250 (Ruim)\n251 a 500 (Razoável)\n501 a 750 (Bom)\n751 a 1000 (Ótimo)",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 2.dp,
            color = Color.Gray
        )

        Menu(navigateTo = navigateTo)
    }
}
