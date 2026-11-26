package com.instantes.app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.ViewModelProvider
import com.instantes.app.ui.viewmodel.FiltroInicio
import com.instantes.app.ui.viewmodel.InicioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(
    onNuevoClick: () -> Unit = {},
    onEditarClick: (Long) -> Unit = {},
    onItemClick: (Long) -> Unit = {},
    onCitasClick: () -> Unit = {}
) {
    SinTecladoCompleto {
        val context = LocalContext.current
        val activity = context as? ComponentActivity

        val viewModel: InicioViewModel = remember(activity) {
            val repo = (context.applicationContext as InstantesApplication).repository
            if (activity != null) {
                ViewModelProvider(
                    activity,
                    InicioViewModel.Factory(repo)
                )[InicioViewModel::class.java]
            } else {
                InicioViewModel(repo)
            }
        }

        val instantes by viewModel.instantes.collectAsState()
        val etiquetas by viewModel.todasLasEtiquetas.collectAsState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.inicio_titulo)) }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNuevoClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.inicio_nuevo_instante)
                    )
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_inicio)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.Place, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_mapa)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onCitasClick,
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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = viewModel.busqueda,
                        onValueChange = { viewModel.actualizarBusqueda(it) },
                        placeholder = { Text(stringResource(R.string.inicio_buscar)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = viewModel.filtroActual is FiltroInicio.Todos,
                                onClick = { viewModel.actualizarFiltro(FiltroInicio.Todos) },
                                label = { Text(stringResource(R.string.filtro_todos)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = viewModel.filtroActual is FiltroInicio.Favoritos,
                                onClick = { viewModel.actualizarFiltro(FiltroInicio.Favoritos) },
                                label = { Text(stringResource(R.string.filtro_favoritos)) }
                            )
                        }
                        listItemsIndexed(etiquetas) { _, etiqueta ->
                            val esSeleccionado = (viewModel.filtroActual as? FiltroInicio.Etiqueta)?.nombre.equals(etiqueta.nombre, ignoreCase = true)
                            FilterChip(
                                selected = esSeleccionado,
                                onClick = { viewModel.actualizarFiltro(FiltroInicio.Etiqueta(etiqueta.nombre)) },
                                label = { Text(etiqueta.nombre) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = viewModel.filtroActual is FiltroInicio.Archivados,
                                onClick = { viewModel.actualizarFiltro(FiltroInicio.Archivados) },
                                label = { Text("Archivados") }
                            )
                        }
                    }
                }

                if (instantes.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Agrega esos momentos que te hicieron sentir",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(instantes) { instante ->
                        val fechaStr = remember(instante.fechaHora) {
                            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                            sdf.format(Date(instante.fechaHora))
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
                                                tint = if (instante.esFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
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
