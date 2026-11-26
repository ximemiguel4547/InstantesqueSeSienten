package com.instantes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.instantes.app.data.local.PreferencesManager
import com.instantes.app.ui.screen.CitasScreen
import com.instantes.app.ui.screen.DetalleInstanteScreen
import com.instantes.app.ui.screen.FavoritosScreen
import com.instantes.app.ui.screen.PerfilScreen
import com.instantes.app.ui.theme.AppInstantesqueSeSientenTheme
import com.instantes.app.ui.theme.Bienvenida
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsManager = PreferencesManager(applicationContext)
        val primeraVez = runBlocking { prefsManager.esPrimeraVez.first() }

        setContent {
            AppInstantesqueSeSientenTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "bienvenida") {
                    composable("bienvenida") {
                        Bienvenida(
                            esPrimeraVez = primeraVez,
                            onComenzarClick = {
                                runBlocking {
                                    prefsManager.setPrimeraVez(false)
                                }
                                navController.navigate("inicio") {
                                    popUpTo("bienvenida") { inclusive = true }
                                }
                            },
                            onAutoNavegar = {
                                navController.navigate("inicio") {
                                    popUpTo("bienvenida") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("inicio") {
                        Inicio(
                            onNuevoClick = { navController.navigate("nuevo") },
                            onEditarClick = { id -> navController.navigate("nuevo?id=$id") },
                            onItemClick = { id -> navController.navigate("detalle/$id") },
                            onCitasClick = { navController.navigate("citas") },
                            onFavoritosClick = { navController.navigate("favoritos") },
                            onPerfilClick = { navController.navigate("perfil") }
                        )
                    }
                    composable("favoritos") {
                        FavoritosScreen(
                            onVolver = { navController.popBackStack() },
                            onInicioClick = {
                                navController.navigate("inicio") {
                                    popUpTo("inicio") { inclusive = true }
                                }
                            },
                            onCitasClick = { navController.navigate("citas") },
                            onPerfilClick = { navController.navigate("perfil") },
                            onEditarClick = { id -> navController.navigate("nuevo?id=$id") },
                            onItemClick = { id -> navController.navigate("detalle/$id") }
                        )
                    }
                    composable("perfil") {
                        PerfilScreen(
                            onVolver = { navController.popBackStack() },
                            onInicioClick = {
                                navController.navigate("inicio") {
                                    popUpTo("inicio") { inclusive = true }
                                }
                            },
                            onFavoritosClick = { navController.navigate("favoritos") },
                            onCitasClick = { navController.navigate("citas") }
                        )
                    }
                    composable(
                        route = "nuevo?id={id}",
                        arguments = listOf(navArgument("id") {
                            type = NavType.LongType
                            defaultValue = -1L
                        })
                    ) { backStackEntry ->
                        val idArg = backStackEntry.arguments?.getLong("id") ?: -1L
                        val instanteId = if (idArg != -1L) idArg else null
                        NuevoInstante(
                            onVolver = { navController.popBackStack() },
                            instanteId = instanteId
                        )
                    }
                    composable(
                        route = "detalle/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getLong("id") ?: 0L
                        DetalleInstanteScreen(
                            instanteId = id,
                            onVolver = { navController.popBackStack() },
                            onEditarClick = { editId -> navController.navigate("nuevo?id=$editId") }
                        )
                    }
                    composable("citas") {
                        CitasScreen(
                            onVolver = { navController.popBackStack() },
                            onInicioClick = {
                                navController.navigate("inicio") {
                                    popUpTo("inicio") { inclusive = true }
                                }
                            },
                            onFavoritosClick = { navController.navigate("favoritos") },
                            onPerfilClick = { navController.navigate("perfil") }
                        )
                    }
                }
            }
        }
    }
}
