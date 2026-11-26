package com.instantes.app

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.ui.viewmodel.NuevoInstanteViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun NuevoInstante(
    onVolver: () -> Unit = {},
    instanteId: Long? = null
) {
    SinTecladoCompleto {
        val context = LocalContext.current
        val activity = context as? ComponentActivity

        val viewModelKey = if (instanteId != null && instanteId > 0L) "editar_$instanteId" else "nuevo"

        val viewModel: NuevoInstanteViewModel = remember(activity, viewModelKey) {
            val repo = (context.applicationContext as InstantesApplication).repository
            if (activity != null) {
                ViewModelProvider(
                    activity.viewModelStore,
                    NuevoInstanteViewModel.Factory(repo, instanteId)
                )[viewModelKey, NuevoInstanteViewModel::class.java]
            } else {
                NuevoInstanteViewModel(repo, instanteId)
            }
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        val reqTitulo = remember { BringIntoViewRequester() }
        val reqReflexion = remember { BringIntoViewRequester() }
        val reqLugar = remember { BringIntoViewRequester() }
        val reqCita = remember { BringIntoViewRequester() }
        val reqAutor = remember { BringIntoViewRequester() }

        LaunchedEffect(Unit) {
            viewModel.eventoUi.collectLatest { mensaje ->
                snackbarHostState.showSnackbar(mensaje)
            }
        }

        var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }

        val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
            val ruta = rutaPendiente
            val archivo = ruta?.let { File(it) }
            if (exito && archivo != null && archivo.exists() && archivo.length() > 0) {
                viewModel.actualizarFotoUri(archivo.absolutePath, true)
            } else {
                archivo?.delete()
            }
            rutaPendiente = null
        }

        fun abrirCamara() {
            val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
            val archivo = File(carpeta, "instante_${System.currentTimeMillis()}.jpg")
            rutaPendiente = archivo.absolutePath
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            cameraLauncher.launch(uri)
        }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                viewModel.copiarGaleriaAAlmacenamientoInterno(context, it)
            }
        }

        val etiquetas by viewModel.todasLasEtiquetas.collectAsState()
        var mostrarDialogoEtiqueta by remember { mutableStateOf(false) }
        var nuevaEtiquetaNombre by remember { mutableStateOf("") }

        var etiquetaParaGestionar by remember { mutableStateOf<Etiqueta?>(null) }
        var mostrarOpcionesEtiqueta by remember { mutableStateOf(false) }
        var mostrarDialogoEditarEtiqueta by remember { mutableStateOf(false) }
        var nombreEditadoEtiqueta by remember { mutableStateOf("") }
        var mostrarDialogoEliminarEtiqueta by remember { mutableStateOf(false) }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(if (instanteId != null && instanteId > 0L) "Editar instante" else stringResource(R.string.nuevo_titulo)) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.cancelar(onVolver) }) {
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
                    .imePadding()
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
                    val fotoPath = rutaDeFotoNuevo(viewModel.fotoUri)
                    val fotoFile = if (fotoPath.isNotBlank()) File(fotoPath) else null
                    if (fotoFile != null && fotoFile.exists()) {
                        AsyncImage(
                            model = fotoFile,
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
                                abrirCamara()
                            } catch (_: Exception) {
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqTitulo)
                        .onFocusEvent { if (it.isFocused) coroutineScope.launch { reqTitulo.bringIntoView() } }
                )
                OutlinedTextField(
                    value = viewModel.reflexion,
                    onValueChange = { viewModel.reflexion = it },
                    label = { Text(stringResource(R.string.nuevo_campo_reflexion)) },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqReflexion)
                        .onFocusEvent { if (it.isFocused) coroutineScope.launch { reqReflexion.bringIntoView() } }
                )
                OutlinedTextField(
                    value = viewModel.lugar,
                    onValueChange = { viewModel.lugar = it },
                    label = { Text(stringResource(R.string.nuevo_campo_lugar)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqLugar)
                        .onFocusEvent { if (it.isFocused) coroutineScope.launch { reqLugar.bringIntoView() } }
                )

                Text(
                    text = stringResource(R.string.nuevo_etiquetas),
                    style = MaterialTheme.typography.titleSmall
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    etiquetaParaGestionar = etiqueta
                                    mostrarOpcionesEtiqueta = true
                                }
                            )
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            if (etiquetas.size >= 10) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Máximo 10 etiquetas")
                                }
                            } else {
                                mostrarDialogoEtiqueta = true
                            }
                        },
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqCita)
                        .onFocusEvent { if (it.isFocused) coroutineScope.launch { reqCita.bringIntoView() } }
                )

                OutlinedTextField(
                    value = viewModel.autorCita,
                    onValueChange = { viewModel.autorCita = it },
                    label = { Text("Autor (opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(reqAutor)
                        .onFocusEvent { if (it.isFocused) coroutineScope.launch { reqAutor.bringIntoView() } }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.cancelar(onVolver) }) {
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
                val reqDialogo = remember { BringIntoViewRequester() }
                AlertDialog(
                    onDismissRequest = { mostrarDialogoEtiqueta = false },
                    title = { Text("Nueva Etiqueta") },
                    text = {
                        OutlinedTextField(
                            value = nuevaEtiquetaNombre,
                            onValueChange = { nuevaEtiquetaNombre = it },
                            label = { Text("Nombre de la etiqueta") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(reqDialogo)
                                .onFocusEvent { if (it.isFocused) coroutineScope.launch { reqDialogo.bringIntoView() } }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nuevaEtiquetaNombre.isNotBlank()) {
                                    viewModel.agregarEtiqueta(nuevaEtiquetaNombre.trim())
                                    nuevaEtiquetaNombre = ""
                                    mostrarDialogoEtiqueta = false
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

            if (mostrarOpcionesEtiqueta && etiquetaParaGestionar != null) {
                AlertDialog(
                    onDismissRequest = { mostrarOpcionesEtiqueta = false },
                    title = { Text("Gestionar etiqueta") },
                    text = { Text("¿Qué deseas hacer con la etiqueta \"${etiquetaParaGestionar?.nombre}\"?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                val et = etiquetaParaGestionar
                                mostrarOpcionesEtiqueta = false
                                if (et != null) {
                                    nombreEditadoEtiqueta = et.nombre
                                    mostrarDialogoEditarEtiqueta = true
                                }
                            }
                        ) {
                            Text("Editar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                mostrarOpcionesEtiqueta = false
                                mostrarDialogoEliminarEtiqueta = true
                            }
                        ) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            if (mostrarDialogoEditarEtiqueta && etiquetaParaGestionar != null) {
                val reqEditar = remember { BringIntoViewRequester() }
                AlertDialog(
                    onDismissRequest = { mostrarDialogoEditarEtiqueta = false },
                    title = { Text("Editar Etiqueta") },
                    text = {
                        OutlinedTextField(
                            value = nombreEditadoEtiqueta,
                            onValueChange = { nombreEditadoEtiqueta = it },
                            label = { Text("Nuevo nombre") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(reqEditar)
                                .onFocusEvent { if (it.isFocused) coroutineScope.launch { reqEditar.bringIntoView() } }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val et = etiquetaParaGestionar
                                if (et != null && nombreEditadoEtiqueta.isNotBlank()) {
                                    viewModel.actualizarEtiqueta(et.id, nombreEditadoEtiqueta)
                                    mostrarDialogoEditarEtiqueta = false
                                }
                            }
                        ) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoEditarEtiqueta = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (mostrarDialogoEliminarEtiqueta && etiquetaParaGestionar != null) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoEliminarEtiqueta = false },
                    title = { Text("Eliminar Etiqueta") },
                    text = { Text("¿Deseas eliminar la etiqueta \"${etiquetaParaGestionar?.nombre}\"? Se quitará de los instantes asociados.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                val et = etiquetaParaGestionar
                                mostrarDialogoEliminarEtiqueta = false
                                if (et != null) {
                                    viewModel.eliminarEtiqueta(et)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoEliminarEtiqueta = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

fun rutaDeFotoNuevo(fotoUri: String): String = if (fotoUri.startsWith("file://")) (Uri.parse(fotoUri).path ?: fotoUri) else fotoUri
