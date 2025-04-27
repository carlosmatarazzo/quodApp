package br.com.fiap.quodapp.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.fiap.quodapp.R

@Composable
fun QuodLogo() {
    Image(
        painter = painterResource(id = R.drawable.quod),
        contentDescription = "Logo Quod",
        modifier = Modifier
            .size(100.dp)
            .padding(bottom = 0.dp)
    )
}
