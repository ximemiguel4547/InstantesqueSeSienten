package com.instantes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.instantes.app.ui.screen.CitasScreen
import com.instantes.app.ui.screen.DetalleInstanteScreen
import com.instantes.app.ui.theme.AppInstantesqueSeSientenTheme
import com.instantes.app.ui.theme.Bienvenida

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppInstantesqueSeSientenTheme {
                var pantalla by remember { mutableStateOf("bienvenida") }
                var editarInstanteId by remember { mutableStateOf<Long?>(null) }
                var detalleInstanteId by remember { mutableStateOf<Long?>(null) }

                when (pantalla) {
                    "bienvenida" -> Bienvenida(onComenzarClick = { pantalla = "inicio" })
                    "inicio" -> Inicio(
                        onNuevoClick = {
                            editarInstanteId = null
                            pantalla = "nuevo"
                        },
                        onEditarClick = { id ->
                            editarInstanteId = id
                            pantalla = "nuevo"
                        },
                        onItemClick = { id ->
                            detalleInstanteId = id
                            pantalla = "detalle"
                        },
                        onCitasClick = { pantalla = "citas" }
                    )
                    "nuevo" -> NuevoInstante(
                        onVolver = {
                            editarInstanteId = null
                            pantalla = "inicio"
                        },
                        instanteId = editarInstanteId
                    )
                    "detalle" -> {
                        if (detalleInstanteId != null) {
                            DetalleInstanteScreen(
                                instanteId = detalleInstanteId!!,
                                onVolver = { pantalla = "inicio" },
                                onEditarClick = { id ->
                                    editarInstanteId = id
                                    pantalla = "nuevo"
                                }
                            )
                        } else {
                            pantalla = "inicio"
                        }
                    }
                    "citas" -> CitasScreen(
                        onVolver = { pantalla = "inicio" }
                    )
                }
            }
        }
    }
}
