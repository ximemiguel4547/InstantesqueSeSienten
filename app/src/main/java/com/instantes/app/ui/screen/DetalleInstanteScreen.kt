package com.instantes.app.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instantes.app.FotoInstante
import com.instantes.app.InstantesApplication
import com.instantes.app.domain.model.Instante
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleViewModel(
    private val repository: InstantesRepository,
    private val instanteId: Long
) : ViewModel() {
    var instante by mutableStateOf<Instante?>(null)
        private set

    init {
        viewModelScope.launch {
            instante = repository.obtenerInstantePorId(instanteId)
        }
    }

    fun eliminarInstante(onEliminado: () -> Unit) {
        viewModelScope.launch {
            instante?.let {
                repository.eliminarInstante(it.id, it.fotoUri)
                onEliminado()
            }
        }
    }

    fun alternarFavorito() {
        viewModelScope.launch {
            instante?.let {
                repository.actualizarFavorito(it.id, !it.esFavorito)
                instante = repository.obtenerInstantePorId(instanteId)
            }
        }
    }

    class Factory(
        private val repository: InstantesRepository,
        private val instanteId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetalleViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DetalleViewModel(repository, instanteId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleInstanteScreen(
    instanteId: Long,
    onVolver: () -> Unit = {},
    onEditarClick: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val viewModel: DetalleViewModel = remember(activity, instanteId) {
        val repo = (context.applicationContext as InstantesApplication).repository
        if (activity != null) {
            ViewModelProvider(
                activity,
                DetalleViewModel.Factory(repo, instanteId)
            )[DetalleViewModel::class.java]
        } else {
            DetalleViewModel(repo, instanteId)
        }
    }

    val instante = viewModel.instante
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(instante?.titulo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (instante != null) {
                        IconButton(onClick = { viewModel.alternarFavorito() }) {
                            Icon(
                                imageVector = if (instante.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (instante.esFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = { onEditarClick(instante.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }
                        IconButton(onClick = { mostrarDialogoEliminar = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (instante == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (instante.fotoUri.isNotBlank()) {
                        FotoInstante(
                            fotoUri = instante.fotoUri,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Sin foto",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = instante.titulo,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    val fechaStr = remember(instante.fechaHora) {
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        sdf.format(Date(instante.fechaHora))
                    }
                    Text(
                        text = "$fechaStr · ${instante.lugar}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (instante.etiquetas.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        instante.etiquetas.forEach { etiqueta ->
                            AssistChip(
                                onClick = {},
                                label = { Text(etiqueta.nombre) }
                            )
                        }
                    }
                }

                if (instante.reflexion.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = instante.reflexion,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (instante.cita != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "“${instante.cita.texto}”",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val autorTexto = if (instante.cita.autor.isNotBlank()) "— ${instante.cita.autor}" else ""
                            val obraTexto = if (!instante.cita.obra.isNullOrBlank()) " (${instante.cita.obra})" else ""
                            if (autorTexto.isNotBlank() || obraTexto.isNotBlank()) {
                                Text(
                                    text = "$autorTexto$obraTexto",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        if (mostrarDialogoEliminar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoEliminar = false },
                title = { Text("Eliminar instante") },
                text = { Text("¿Estás seguro de que deseas eliminar este instante? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoEliminar = false
                            viewModel.eliminarInstante {
                                onVolver()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoEliminar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
