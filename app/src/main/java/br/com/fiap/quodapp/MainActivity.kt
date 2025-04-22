package br.com.fiap.quodapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.fiap.quodapp.screens.AppNavigator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigator()
        }
    }
}

//@Composable
//fun FacialBiometricsScreen(navigateTo: (String) -> Unit) {
//    var message by remember { mutableStateOf("") }
//    var messageColor by remember { mutableStateOf(Color.Transparent) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        Text(
//            text = "Biometria Facial",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        // Frame simulando a câmera
//        Box(
//            modifier = Modifier
//                .size(200.dp)
//                .background(Color.Gray)
//        ) {
//            Text(
//                text = "Câmera Ativada",
//                color = Color.White,
//                modifier = Modifier.align(Alignment.Center)
//            )
//        }
//
//        if (message.isNotEmpty()) {
//            Text(
//                text = message,
//                color = messageColor,
//                style = MaterialTheme.typography.titleSmall
//            )
//        }
//
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            Button(onClick = {
//                message = "Imagem validada"
//                messageColor = Color.Green
//            }) {
//                Text("Válido")
//            }
//            Button(onClick = {
//                message = "Imagem invalidada"
//                messageColor = Color.Red
//            }) {
//                Text("Inválido")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        val menuItems = listOf(
//            "Biometria Facial", "Biometria Digital",
//            "Análise Documento", "SIM SWAP",
//            "Cadastro", "Score"
//        )
//
//        menuItems.forEach { item ->
//            Text(
//                text = item,
//                modifier = Modifier
//                    .clickable { navigateTo(item) }
//                    .padding(8.dp),
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//}
