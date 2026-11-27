package com.instantes.app.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instantes.app.FotoInstante
import com.instantes.app.InstantesApplication
import com.instantes.app.R
import com.instantes.app.SinTecladoCompleto
import com.instantes.app.aFechaLegible
import com.instantes.app.domain.model.Instante
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritosViewModel(private val repository: InstantesRepository) : ViewModel() {
    val instantes: StateFlow<List<Instante>> = repository.obtenerFavoritos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun alternarFavorito(instanteId: Long, esFavoritoActual: Boolean) {
        viewModelScope.launch {
            repository.actualizarFavorito(instanteId, !esFavoritoActual)
        }
    }

    fun archivarInstante(instanteId: Long, archivadoActual: Boolean) {
        viewModelScope.launch {
            repository.actualizarArchivado(instanteId, !archivadoActual)
        }
    }

    fun eliminarInstante(instanteId: Long, fotoUri: String) {
        viewModelScope.launch {
            repository.eliminarInstante(instanteId, fotoUri)
        }
    }

    class Factory(private val repository: InstantesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoritosViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FavoritosViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    onVolver: () -> Unit = {},
    onInicioClick: () -> Unit = {},
    onCitasClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    onEditarClick: (Long) -> Unit = {},
    onItemClick: (Long) -> Unit = {},
    viewModel: FavoritosViewModel = rememberFavoritosViewModel()
) {
    SinTecladoCompleto {
        val instantes by viewModel.instantes.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Favoritos") },
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
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = false,
                        onClick = onInicioClick,
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_inicio)) }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
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
                        selected = false,
                        onClick = onPerfilClick,
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_perfil)) }
                    )
                }
            }
        ) { padding ->
            if (instantes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no tienes favoritos. Toca el corazón en un instante",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(instantes) { instante ->
                        val fechaStr = remember(instante.fechaHora) {
                            instante.fechaHora.aFechaLegible()
                        }
                        val lugarYFecha = "$fechaStr · ${instante.lugar}"

                        Card(
                            onClick = { onItemClick(instante.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                ) {
                                    if (instante.fotoUri.isNotBlank()) {
                                        FotoInstante(
                                            fotoUri = instante.fotoUri,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FavoriteBorder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.alternarFavorito(instante.id, instante.esFavorito) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = stringResource(R.string.desc_favorito),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        var menuExpandido by rememberSaveable(instante.id) { mutableStateOf(false) }
                                        var mostrarDialogoEliminar by rememberSaveable(instante.id) { mutableStateOf(false) }

                                        Box {
                                            IconButton(
                                                onClick = { menuExpandido = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Opciones",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = menuExpandido,
                                                onDismissRequest = { menuExpandido = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Editar") },
                                                    onClick = {
                                                        menuExpandido = false
                                                        onEditarClick(instante.id)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(if (instante.archivado) "Desarchivar" else "Archivar") },
                                                    onClick = {
                                                        menuExpandido = false
                                                        viewModel.archivarInstante(instante.id, instante.archivado)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Eliminar") },
                                                    onClick = {
                                                        menuExpandido = false
                                                        mostrarDialogoEliminar = true
                                                    }
                                                )
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
                                                            viewModel.eliminarInstante(instante.id, instante.fotoUri)
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

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = instante.titulo,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = lugarYFecha,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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

@Composable
fun rememberFavoritosViewModel(): FavoritosViewModel {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    return remember(activity) {
        val repo = (context.applicationContext as InstantesApplication).repository
        if (activity != null) {
            ViewModelProvider(activity, FavoritosViewModel.Factory(repo))[FavoritosViewModel::class.java]
        } else {
            FavoritosViewModel(repo)
        }
    }
}
