package com.instantes.app

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.ViewModelProvider
import com.instantes.app.ui.viewmodel.NuevoInstanteViewModel
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoInstante(onVolver: () -> Unit = {}) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val viewModel: NuevoInstanteViewModel = remember(activity) {
        val repo = (context.applicationContext as InstantesApplication).repository
        if (activity != null) {
            ViewModelProvider(
                activity,
                NuevoInstanteViewModel.Factory(repo)
            )[NuevoInstanteViewModel::class.java]
        } else {
            NuevoInstanteViewModel(repo)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventoUi.collectLatest { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    // Camera temp file & launcher
    val tempFile = remember {
        File(context.filesDir, "camera_${System.currentTimeMillis()}.jpg")
    }
    val tempUri = remember {
        Uri.fromFile(tempFile)
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.actualizarFotoUri(tempUri.toString())
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val internalUriStr = viewModel.copiarUriAAlmacenamientoInterno(context, it)
            viewModel.actualizarFotoUri(internalUriStr)
        }
    }

    val etiquetas = listOf(
        R.string.filtro_naturaleza,
        R.string.filtro_arte,
        R.string.filtro_atardecer
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nuevo_titulo)) },
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
            // Área de la foto
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
                } else {
                    Text(
                        text = stringResource(R.string.nuevo_foto),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cámara y galería
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        try {
                            cameraLauncher.launch(tempUri)
                        } catch (_: Exception) {}
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

            // Campos de texto
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

            // Etiquetas
            Text(
                text = stringResource(R.string.nuevo_etiquetas),
                style = MaterialTheme.typography.titleSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                etiquetas.forEach { etiqueta ->
                    FilterChip(
                        selected = etiqueta in viewModel.seleccionadas,
                        onClick = {
                            if (etiqueta in viewModel.seleccionadas) viewModel.seleccionadas.remove(etiqueta)
                            else viewModel.seleccionadas.add(etiqueta)
                        },
                        label = { Text(stringResource(etiqueta)) }
                    )
                }
            }

            OutlinedTextField(
                value = viewModel.cita,
                onValueChange = { viewModel.cita = it },
                label = { Text(stringResource(R.string.nuevo_campo_cita)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Cancelar y Guardar
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
    }
}
