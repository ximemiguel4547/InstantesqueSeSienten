package com.instantes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.instantes.app.ui.theme.AppInstantesqueSeSientenTheme
import com.instantes.app.ui.theme.Bienvenida
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppInstantesqueSeSientenTheme {
                var mostrarBienvenida by remember { mutableStateOf(true) }

                AppInstantesqueSeSientenTheme {
                    var pantalla by remember { mutableStateOf("bienvenida") }

                    when (pantalla) {
                        "bienvenida" -> Bienvenida(onComenzarClick = { pantalla = "inicio" })
                        "inicio" -> Inicio(onNuevoClick = { pantalla = "nuevo" })
                        "nuevo" -> NuevoInstante(onVolver = { pantalla = "inicio" })
                    }
                }
            }
        }
    }
}