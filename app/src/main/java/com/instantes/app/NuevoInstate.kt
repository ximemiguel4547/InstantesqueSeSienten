package com.instantes.app

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.ui.viewmodel.NuevoInstanteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoInstante(
    onVolver: () -> Unit = {},
    instanteId: Long? = null
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val viewModel: NuevoInstanteViewModel = remember(activity, instanteId) {
        val repo = (context.applicationContext as InstantesApplication).repository
        if (activity != null) {
            ViewModelProvider(
                activity,
                NuevoInstanteViewModel.Factory(repo, instanteId)
            )[NuevoInstanteViewModel::class.java]
        } else {
            NuevoInstanteViewModel(repo, instanteId)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.eventoUi.collectLatest { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val internalUriStr = viewModel.copiarUriAAlmacenamientoInterno(context, it)
            viewModel.actualizarFotoUri(internalUriStr, esTemporal = true)
        }
    }

    val etiquetas by viewModel.todasLasEtiquetas.collectAsState()
    var mostrarDialogoEtiqueta by remember { mutableStateOf(false) }
    var nuevaEtiquetaNombre by remember { mutableStateOf("") }
    val repo = (context.applicationContext as InstantesApplication).repository

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (instanteId != null && instanteId > 0L) "Editar instante" else stringResource(R.string.nuevo_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = remember(viewModel.fotoUri) {
                    try {
                        if (viewModel.fotoUri.isNotBlank()) {
                            val uri = Uri.parse(viewModel.fotoUri)
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
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.quitarFoto() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar foto",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.nuevo_foto),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        try {
                            val fotosDir = File(context.filesDir, "fotos")
                            if (!fotosDir.exists()) {
                                fotosDir.mkdirs()
                            }
                            val photoFile = File(fotosDir, "instante_${System.currentTimeMillis()}.jpg")
                            val photoUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            viewModel.actualizarFotoUri(photoUri.toString(), esTemporal = true)
                            cameraLauncher.launch(photoUri)
                        } catch (e: Exception) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error al abrir la cámara")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.nuevo_camara))
                }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.nuevo_galeria))
                }
            }

            OutlinedTextField(
                value = viewModel.titulo,
                onValueChange = { viewModel.titulo = it },
                label = { Text(stringResource(R.string.nuevo_campo_titulo)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.reflexion,
                onValueChange = { viewModel.reflexion = it },
                label = { Text(stringResource(R.string.nuevo_campo_reflexion)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.lugar,
                onValueChange = { viewModel.lugar = it },
                label = { Text(stringResource(R.string.nuevo_campo_lugar)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.nuevo_etiquetas),
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                etiquetas.forEach { etiqueta ->
                    val seleccionado = etiqueta.id in viewModel.seleccionadas
                    FilterChip(
                        selected = seleccionado,
                        onClick = {
                            if (seleccionado) viewModel.seleccionadas.remove(etiqueta.id)
                            else viewModel.seleccionadas.add(etiqueta.id)
                        },
                        label = { Text(etiqueta.nombre) },
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                if (seleccionado) viewModel.seleccionadas.remove(etiqueta.id)
                                else viewModel.seleccionadas.add(etiqueta.id)
                            },
                            onLongClick = {
                                coroutineScope.launch {
                                    repo.eliminarEtiqueta(EtiquetaEntity(id = etiqueta.id, nombre = etiqueta.nombre))
                                }
                            }
                        )
                    )
                }
                OutlinedButton(
                    onClick = { mostrarDialogoEtiqueta = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Etiqueta")
                }
            }

            OutlinedTextField(
                value = viewModel.cita,
                onValueChange = { viewModel.cita = it },
                label = { Text(stringResource(R.string.nuevo_campo_cita)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onVolver) {
                    Text(stringResource(R.string.nuevo_cancelar))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    viewModel.guardarInstante(context) {
                        onVolver()
                    }
                }) {
                    Text(stringResource(R.string.nuevo_guardar))
                }
            }
        }

        if (mostrarDialogoEtiqueta) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoEtiqueta = false },
                title = { Text("Nueva Etiqueta") },
                text = {
                    OutlinedTextField(
                        value = nuevaEtiquetaNombre,
                        onValueChange = { nuevaEtiquetaNombre = it },
                        label = { Text("Nombre de la etiqueta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nuevaEtiquetaNombre.isNotBlank()) {
                                coroutineScope.launch {
                                    repo.insertarEtiqueta(nuevaEtiquetaNombre.trim())
                                    nuevaEtiquetaNombre = ""
                                    mostrarDialogoEtiqueta = false
                                }
                            }
                        }
                    ) {
                        Text("Agregar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoEtiqueta = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
