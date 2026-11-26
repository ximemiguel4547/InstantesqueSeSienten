package com.instantes.app.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instantes.app.InstantesApplication
import com.instantes.app.R
import com.instantes.app.SinTecladoCompleto
import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.domain.model.Cita
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CitasViewModel(private val repository: InstantesRepository) : ViewModel() {
    private val _soloFavoritas = MutableStateFlow(false)
    val soloFavoritas: StateFlow<Boolean> = _soloFavoritas.asStateFlow()

    val citas: StateFlow<List<Cita>> = combine(
        repository.todasLasCitas,
        _soloFavoritas
    ) { lista, favoritas ->
        if (favoritas) lista.filter { it.esFavorita } else lista
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSoloFavoritas(favoritas: Boolean) {
        _soloFavoritas.value = favoritas
    }

    fun guardarCita(id: Long?, texto: String, autor: String, obra: String?) {
        viewModelScope.launch {
            if (id != null && id > 0L) {
                repository.actualizarCita(
                    CitaEntity(
                        id = id,
                        texto = texto.trim(),
                        autor = autor.trim(),
                        obra = obra?.trim(),
                        esFavorita = false
                    )
                )
            } else {
                repository.insertarCita(
                    CitaEntity(
                        texto = texto.trim(),
                        autor = autor.trim(),
                        obra = obra?.trim(),
                        esFavorita = false
                    )
                )
            }
        }
    }

    fun alternarFavorito(cita: Cita) {
        viewModelScope.launch {
            repository.actualizarFavoritoCita(cita.id, !cita.esFavorita)
        }
    }

    fun eliminarCita(cita: Cita) {
        viewModelScope.launch {
            repository.eliminarCita(
                CitaEntity(
                    id = cita.id,
                    texto = cita.texto,
                    autor = cita.autor,
                    obra = cita.obra,
                    esFavorita = cita.esFavorita
                )
            )
        }
    }

    class Factory(private val repository: InstantesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CitasViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CitasViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(
    onVolver: () -> Unit = {},
    onInicioClick: () -> Unit = {},
    onFavoritosClick: () -> Unit = {},
    viewModel: CitasViewModel = rememberCitasViewModel()
) {
    SinTecladoCompleto {
        val citas by viewModel.citas.collectAsState()
        val soloFavoritas by viewModel.soloFavoritas.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val clipboardManager = LocalClipboardManager.current
        val coroutineScope = rememberCoroutineScope()

        var mostrarDialogo by rememberSaveable { mutableStateOf(false) }
        var editandoId by rememberSaveable { mutableStateOf<Long?>(null) }
        var textoCita by rememberSaveable { mutableStateOf("") }
        var autorCita by rememberSaveable { mutableStateOf("") }
        var obraCita by rememberSaveable { mutableStateOf("") }

        var citaParaEliminar by remember { mutableStateOf<Cita?>(null) }
        var mostrarDialogoEliminar by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Citas y Reflexiones") },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editandoId = null
                        textoCita = ""
                        autorCita = ""
                        obraCita = ""
                        mostrarDialogo = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar cita")
                }
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
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Star, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_citas)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_perfil)) }
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = !soloFavoritas,
                            onClick = { viewModel.setSoloFavoritas(false) },
                            label = { Text("Todas") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = soloFavoritas,
                            onClick = { viewModel.setSoloFavoritas(true) },
                            label = { Text("Favoritas") }
                        )
                    }
                }

                if (citas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aún no tienes citas. Agrega una con el botón +",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(citas) { cita ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "“${cita.texto}”",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            if (cita.autor.isNotBlank()) {
                                                Text(
                                                    text = "— ${cita.autor}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            if (!cita.obra.isNullOrBlank()) {
                                                Text(
                                                    text = cita.obra,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    val copiaTexto = if (cita.autor.isNotBlank()) "${cita.texto} — ${cita.autor}" else cita.texto
                                                    clipboardManager.setText(AnnotatedString(copiaTexto))
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Cita copiada")
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "Copiar",
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            IconButton(onClick = { viewModel.alternarFavorito(cita) }) {
                                                Icon(
                                                    imageVector = if (cita.esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Favorita",
                                                    tint = if (cita.esFavorita) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    editandoId = cita.id
                                                    textoCita = cita.texto
                                                    autorCita = cita.autor
                                                    obraCita = cita.obra ?: ""
                                                    mostrarDialogo = true
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
                                                    citaParaEliminar = cita
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
                        }
                    }
                }
            }

            if (mostrarDialogo) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogo = false },
                    title = { Text(if (editandoId != null) "Editar Cita" else "Nueva Cita") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = textoCita,
                                onValueChange = { textoCita = it },
                                label = { Text("Texto de la cita") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = autorCita,
                                onValueChange = { autorCita = it },
                                label = { Text("Autor (opcional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = obraCita,
                                onValueChange = { obraCita = it },
                                label = { Text("Obra / Libro (opcional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (textoCita.isNotBlank()) {
                                    viewModel.guardarCita(editandoId, textoCita, autorCita, obraCita)
                                    textoCita = ""
                                    autorCita = ""
                                    obraCita = ""
                                    editandoId = null
                                    mostrarDialogo = false
                                }
                            }
                        ) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                editandoId = null
                                mostrarDialogo = false
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (mostrarDialogoEliminar && citaParaEliminar != null) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoEliminar = false },
                    title = { Text("Eliminar cita") },
                    text = { Text("¿Eliminar esta cita?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                val c = citaParaEliminar
                                mostrarDialogoEliminar = false
                                citaParaEliminar = null
                                if (c != null) {
                                    viewModel.eliminarCita(c)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                mostrarDialogoEliminar = false
                                citaParaEliminar = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun rememberCitasViewModel(): CitasViewModel {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    return remember(activity) {
        val repo = (context.applicationContext as InstantesApplication).repository
        if (activity != null) {
            ViewModelProvider(activity, CitasViewModel.Factory(repo))[CitasViewModel::class.java]
        } else {
            CitasViewModel(repo)
        }
    }
}
