package com.instantes.app

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.instantes.app.ui.viewmodel.InicioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inicio(
    onNuevoClick: () -> Unit = {}
) {
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

    val filtros = listOf(
        R.string.filtro_todos,
        R.string.filtro_favoritos,
        R.string.filtro_naturaleza,
        R.string.filtro_arte
    )

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
        Column(modifier = Modifier.padding(padding)) {

            OutlinedTextField(
                value = viewModel.busqueda,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                placeholder = { Text(stringResource(R.string.inicio_buscar)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(filtros) { indice, texto ->
                    FilterChip(
                        selected = viewModel.filtroActual == indice,
                        onClick = { viewModel.actualizarFiltro(indice) },
                        label = { Text(stringResource(texto)) }
                    )
                }
            }

            if (instantes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_hay_instantes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(instantes) { instante ->
                        val fechaStr = remember(instante.fechaHora) {
                            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                            sdf.format(Date(instante.fechaHora))
                        }
                        val lugarYFecha = "$fechaStr · ${instante.lugar}"

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            val bitmap = remember(instante.fotoUri) {
                                try {
                                    if (instante.fotoUri.isNotBlank()) {
                                        val uri = Uri.parse(instante.fotoUri)
                                        if (uri.scheme == "file" || uri.path != null) {
                                            BitmapFactory.decodeFile(uri.path)
                                        } else {
                                            null
                                        }
                                    } else null
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                )
                            }

                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = instante.titulo,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = lugarYFecha,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = {
                                    viewModel.alternarFavorito(instante.id, instante.esFavorito)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = stringResource(R.string.desc_favorito),
                                        tint = if (instante.esFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
