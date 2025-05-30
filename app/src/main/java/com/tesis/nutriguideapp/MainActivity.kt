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
import com.tesis.nutriguideapp.ui.screens.*
import com.tesis.nutriguideapp.ui.theme.NutriGuideAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriGuideAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {                        composable("login") {
                            LoginScreen(
                                context = applicationContext,
                                onLoginSuccess = {
                                    try {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("MainActivity", "Error navegando desde login: ${e.message}", e)
                                        // Si la navegación falla, asegurarse de que la app no se cierre
                                    }
                                },
                                onNavigateToRegister = {
                                    try {
                                        navController.navigate("register")
                                    } catch (e: Exception) {
                                        android.util.Log.e("MainActivity", "Error navegando a registro: ${e.message}", e)
                                    }
                                }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                context = applicationContext,
                                onRegisterSuccess = {
                                    try {
                                        navController.navigate("login") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("MainActivity", "Error navegando desde registro: ${e.message}", e)
                                    }
                                },
                                onBackToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                onBackToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("forgot_password") { inclusive = true }
                                    }
                                },
                                onResetEmailSent = { token ->
                                    navController.navigate("reset_password/$token") 
                                }
                            )
                        }

                        composable(
                            "reset_password/{token}",
                            arguments = listOf(navArgument("token") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            ResetPasswordScreen(
                                token = token,
                                onPasswordReset = {
                                    navController.navigate("login") {
                                        popUpTo("reset_password/{token}") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(navController = navController, context = applicationContext)
                        }
                          composable("upload") {
                            UploadScreen(navController = navController)
                        }

                        composable("camera") {
                            CameraScreen(navController = navController)
                        }

                        composable("restricciones") {
                            RestriccionesScreen(
                                navController = navController,
                                context = applicationContext
                            )
                        }

                        composable(
                            "history/{restrictions}",
                            arguments = listOf(navArgument("restrictions") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val restrictions = backStackEntry.arguments?.getString("restrictions") ?: ""
                            val selectedRestrictions = restrictions.split(",").filter { it.isNotBlank() }.toSet()
                            HistoryScreen(
                                navController = navController,
                                selectedRestrictions = selectedRestrictions
                            )
                        }

                        composable(
                            "product_detail/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
                            ProductDetailScreen(
                                productId = productId,
                                navController = navController
                            )                        }

                        composable("profile") {
                            ProfileScreen(
                                navController = navController,
                                context = applicationContext
                            )
                        }

                        composable("edit_profile") {
                            EditProfileScreen(
                                navController = navController,
                                context = applicationContext
                            )
                        }
                    }
                }
            }
        }
    }
}
