package com.instantes.app.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.instantes.app.InstantesApplication
import com.instantes.app.R
import com.instantes.app.SinTecladoCompleto
import com.instantes.app.domain.model.Etiqueta
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onVolver: () -> Unit = {},
    onInicioClick: () -> Unit = {},
    onFavoritosClick: () -> Unit = {},
    onCitasClick: () -> Unit = {},
    viewModel: PerfilViewModel = rememberPerfilViewModel()
) {
    SinTecladoCompleto {
        val context = LocalContext.current
        val totalInstantes by viewModel.totalInstantes.collectAsState()
        val totalFavoritos by viewModel.totalFavoritos.collectAsState()
        val totalCitas by viewModel.totalCitas.collectAsState()
        val etiquetaMasUsada by viewModel.etiquetaMasUsada.collectAsState()
        val etiquetas by viewModel.todasLasEtiquetas.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            viewModel.eventoUi.collectLatest { mensaje ->
                snackbarHostState.showSnackbar(mensaje)
            }
        }

        var mostrarDialogoAgregar by rememberSaveable { mutableStateOf(false) }
        var nuevoNombreEtiqueta by rememberSaveable { mutableStateOf("") }

        var etiquetaParaEditar by remember { mutableStateOf<Etiqueta?>(null) }
        var mostrarDialogoEditar by rememberSaveable { mutableStateOf(false) }
        var nombreEditado by rememberSaveable { mutableStateOf("") }

        var etiquetaParaEliminar by remember { mutableStateOf<Etiqueta?>(null) }
        var mostrarDialogoEliminar by rememberSaveable { mutableStateOf(false) }

        val appName = stringResource(R.string.app_name)
        val versionNombre = remember(context) {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "1.0"
            } catch (_: Exception) {
                "1.0"
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.perfil_titulo)) },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.volver)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = false,
                        onClick = onInicioClick,
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_inicio)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onFavoritosClick,
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_favoritos)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onCitasClick,
                        icon = { Icon(Icons.Default.Star, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_citas)) }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_perfil)) }
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.perfil_coleccion),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.perfil_instantes_guardados, totalInstantes),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.perfil_favoritos_count, totalFavoritos),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.perfil_citas_count, totalCitas),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        val etiquetaStr: String = if (etiquetaMasUsada.isNullOrBlank()) "—" else etiquetaMasUsada!!
                        Text(
                            text = stringResource(R.string.perfil_etiqueta_mas_usada, etiquetaStr),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.perfil_mis_etiquetas),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (etiquetas.isEmpty()) {
                            Text(
                                text = stringResource(R.string.perfil_sin_etiquetas),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            etiquetas.forEach { etiqueta ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = etiqueta.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row {
                                        IconButton(
                                            onClick = {
                                                etiquetaParaEditar = etiqueta
                                                nombreEditado = etiqueta.nombre
                                                mostrarDialogoEditar = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                etiquetaParaEliminar = etiqueta
                                                mostrarDialogoEliminar = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (etiquetas.size >= 10) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Máximo 10 etiquetas")
                                        }
                                    } else {
                                        nuevoNombreEtiqueta = ""
                                        mostrarDialogoAgregar = true
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.perfil_agregar_etiqueta))
                            }
                            Text(
                                text = "${etiquetas.size} de 10",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.perfil_aviso_privacidad),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.perfil_texto_privacidad),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.perfil_acerca_de),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.perfil_aplicacion, appName),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.perfil_version, versionNombre),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        if (mostrarDialogoAgregar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoAgregar = false },
                title = { Text(stringResource(R.string.perfil_nueva_etiqueta)) },
                text = {
                    OutlinedTextField(
                        value = nuevoNombreEtiqueta,
                        onValueChange = { nuevoNombreEtiqueta = it },
                        label = { Text(stringResource(R.string.perfil_nombre_etiqueta)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.agregarEtiqueta(nuevoNombreEtiqueta)
                            nuevoNombreEtiqueta = ""
                            mostrarDialogoAgregar = false
                        }
                    ) {
                        Text(stringResource(R.string.perfil_agregar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoAgregar = false }) {
                        Text(stringResource(R.string.perfil_cancelar))
                    }
                }
            )
        }

        if (mostrarDialogoEditar && etiquetaParaEditar != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoEditar = false },
                title = { Text(stringResource(R.string.perfil_editar_etiqueta)) },
                text = {
                    OutlinedTextField(
                        value = nombreEditado,
                        onValueChange = { nombreEditado = it },
                        label = { Text(stringResource(R.string.perfil_nombre_etiqueta)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val et = etiquetaParaEditar
                            if (et != null) {
                                viewModel.actualizarEtiqueta(et.id, nombreEditado)
                            }
                            mostrarDialogoEditar = false
                        }
                    ) {
                        Text(stringResource(R.string.perfil_guardar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoEditar = false }) {
                        Text(stringResource(R.string.perfil_cancelar))
                    }
                }
            )
        }

        if (mostrarDialogoEliminar && etiquetaParaEliminar != null) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoEliminar = false },
                title = { Text(stringResource(R.string.perfil_eliminar_etiqueta)) },
                text = { Text("¿Eliminar la etiqueta '${etiquetaParaEliminar?.nombre}'? Se quitará de los instantes que la usan, pero los instantes no se borran.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val et = etiquetaParaEliminar
                            mostrarDialogoEliminar = false
                            if (et != null) {
                                viewModel.eliminarEtiqueta(et)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.perfil_eliminar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoEliminar = false }) {
                        Text(stringResource(R.string.perfil_cancelar))
                    }
                }
            )
        }
    }
}

@Composable
fun rememberPerfilViewModel(): PerfilViewModel {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    return remember(activity) {
        val repo = (context.applicationContext as InstantesApplication).repository
        if (activity != null) {
            ViewModelProvider(activity, PerfilViewModel.Factory(repo))[PerfilViewModel::class.java]
        } else {
            PerfilViewModel(repo)
        }
    }
}
