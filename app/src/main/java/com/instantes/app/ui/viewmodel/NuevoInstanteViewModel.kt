package com.instantes.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instantes.app.R
import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.data.entity.InstanteEntity
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class NuevoInstanteViewModel(private val repository: InstantesRepository) : ViewModel() {

    var titulo by mutableStateOf("")
    var reflexion by mutableStateOf("")
    var lugar by mutableStateOf("")
    var cita by mutableStateOf("")
    var fotoUri by mutableStateOf("")

    val seleccionadas = mutableStateListOf<Int>()

    private val _eventoUi = MutableSharedFlow<String>()
    val eventoUi: SharedFlow<String> = _eventoUi.asSharedFlow()

    fun actualizarFotoUri(uri: String) {
        fotoUri = uri
    }

    fun guardarInstante(context: Context, onGuardadoExitoso: () -> Unit) {
        if (titulo.isBlank() || fotoUri.isBlank()) {
            viewModelScope.launch {
                _eventoUi.emit(context.getString(R.string.error_foto_titulo_obligatorios))
            }
            return
        }

        viewModelScope.launch {
            try {
                val tagNames = seleccionadas.map { resId ->
                    when (resId) {
                        R.string.filtro_naturaleza -> "Naturaleza"
                        R.string.filtro_arte -> "Arte"
                        R.string.filtro_atardecer -> "Atardecer"
                        else -> "Naturaleza"
                    }
                }

                val etiquetaIds = mutableListOf<Long>()
                for (tagName in tagNames) {
                    val etiqueta = repository.obtenerEtiquetaPorNombre(tagName)
                    if (etiqueta != null) {
                        etiquetaIds.add(etiqueta.id)
                    }
                }

                val citaEntity = if (cita.isNotBlank()) {
                    CitaEntity(
                        texto = cita.trim(),
                        autor = "Desconocido",
                        obra = null,
                        esFavorita = false
                    )
                } else {
                    null
                }

                val instante = InstanteEntity(
                    titulo = titulo.trim(),
                    reflexion = reflexion.trim(),
                    fotoUri = fotoUri,
                    lugar = lugar.ifBlank { "Lugar desconocido" },
                    latitud = null,
                    longitud = null,
                    fechaHora = System.currentTimeMillis(),
                    esFavorito = false,
                    citaId = null
                )

                repository.insertarInstanteCompleto(instante, etiquetaIds, citaEntity)
                _eventoUi.emit(context.getString(R.string.instante_guardado_exito))
                onGuardadoExitoso()
            } catch (e: Exception) {
                _eventoUi.emit("Error al guardar: ${e.localizedMessage}")
            }
        }
    }

    fun copiarUriAAlmacenamientoInterno(context: Context, sourceUri: Uri): String {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return ""
            val fileName = "instante_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return Uri.fromFile(file).toString()
        } catch (_: Exception) {
            return sourceUri.toString()
        }
    }

    class Factory(private val repository: InstantesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NuevoInstanteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NuevoInstanteViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
