package br.com.fiap.quodapp.screens

import FacialBiometricsScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen { destination ->
                navController.navigate(destination.lowercase().replace(" ", "_"))
            }
        }
        composable("biometria_facial") {
            FacialBiometricsScreen { destination ->
                navController.navigate(destination.lowercase().replace(" ", "_"))
            }
        }
        composable("biometria_digital") {
            DigitalBiometricsScreen { destination ->
                navController.navigate(destination.lowercase().replace(" ", "_"))
            }
        }
        composable("analise_documento") {
            DocumentAnalysisScreen { destination ->
                navController.navigate(destination.lowercase().replace(" ", "_"))
            }
        }
        composable("sim_swap") {
            SimSwapScreen { destination ->
                navController.navigate(destination.lowercase().replace(" ", "_"))
            }
        }
        composable("cadastro") {
            RegisterScreen { destination ->
                navController.navigate(destination.lowercase().replace(" ", "_"))
            }
        }
        composable("score") {
            ScoreScreen { destination ->
                navController.navigate(destination.lowercase().replace(" ", "_"))
            }
        }
        composable("final_score_screen") {
            FinalScoreScreen { destination ->
                navController.navigate(destination.lowercase().replace(" ", "_"))
            }
        }
    }
}

