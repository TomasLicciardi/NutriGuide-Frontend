package com.tesis.nutriguideapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.tesis.nutriguideapp.screens.HomeScreen
import com.tesis.nutriguideapp.ui.screens.*
import com.tesis.nutriguideapp.ui.theme.NutriGuideAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriGuideAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {

                        composable("login") {
                            LoginScreen(context = applicationContext) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }

                        composable("home") {
                            HomeScreen(navController = navController, context = applicationContext)
                        }

                        composable("upload") {
                            UploadScreen()
                        }

                        // Pasa restricciones como un string separado por comas
                        composable("history/{restrictions}",
                            arguments = listOf(navArgument("restrictions") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val restrictions = backStackEntry.arguments?.getString("restrictions") ?: ""
                            val selectedRestrictions = restrictions.split(",").filter { it.isNotBlank() }.toSet()
                            HistoryScreen(navController = navController, selectedRestrictions = selectedRestrictions)
                        }

                        composable("restricciones") {
                            RestriccionesScreen()
                        }

                        composable(
                            "product_detail/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
                            ProductDetailScreen(productId = productId)
                        }
                    }
                }
            }
        }
    }
}
