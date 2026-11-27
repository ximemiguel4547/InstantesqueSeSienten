package com.instantes.app

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import java.io.File

@Composable
fun FotoInstante(fotoUri: String, modifier: Modifier = Modifier) {
    val path = if (fotoUri.startsWith("file://")) {
        Uri.parse(fotoUri).path ?: fotoUri
    } else {
        fotoUri
    }
    val file = File(path)
    val existe = file.exists()
    val tamano = if (existe) file.length() else 0L

    Log.d("FOTO", "Ruta: $path | Existe: $existe | Tamaño: $tamano")

    if (fotoUri.isNotBlank() && existe && tamano > 0L) {
        AsyncImage(
            model = file,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Preview
@Composable
fun FotoInstantePreview() {
    MaterialTheme {
        FotoInstante(fotoUri = "")
    }
}
