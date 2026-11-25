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
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class NuevoInstanteViewModel(
    private val repository: InstantesRepository,
    val instanteIdParaEditar: Long? = null
) : ViewModel() {

    var titulo by mutableStateOf("")
    var reflexion by mutableStateOf("")
    var lugar by mutableStateOf("")
    var cita by mutableStateOf("")
    var fotoUri by mutableStateOf("")
    var esFotoTemporal by mutableStateOf(false)

    val seleccionadas = mutableStateListOf<Long>()

    val todasLasEtiquetas: StateFlow<List<Etiqueta>> = repository.todasLasEtiquetas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _eventoUi = MutableSharedFlow<String>()
    val eventoUi: SharedFlow<String> = _eventoUi.asSharedFlow()

    init {
        if (instanteIdParaEditar != null && instanteIdParaEditar > 0L) {
            cargarInstanteParaEditar(instanteIdParaEditar)
        }
    }

    private fun cargarInstanteParaEditar(id: Long) {
        viewModelScope.launch {
            val instante = repository.obtenerInstantePorId(id)
            if (instante != null) {
                titulo = instante.titulo
                reflexion = instante.reflexion
                lugar = instante.lugar
                fotoUri = instante.fotoUri
                esFotoTemporal = false
                cita = instante.cita?.texto ?: ""
                seleccionadas.clear()
                seleccionadas.addAll(instante.etiquetas.map { it.id })
            }
        }
    }

    fun actualizarFotoUri(uri: String, esTemporal: Boolean) {
        if (esFotoTemporal && fotoUri.isNotBlank() && fotoUri != uri) {
            borrarArchivoSiEsTemporal(fotoUri)
        }
        fotoUri = uri
        esFotoTemporal = esTemporal
    }

    fun quitarFoto() {
        if (esFotoTemporal && fotoUri.isNotBlank()) {
            borrarArchivoSiEsTemporal(fotoUri)
        }
        fotoUri = ""
        esFotoTemporal = false
    }

    private fun borrarArchivoSiEsTemporal(uriStr: String) {
        try {
            val uri = Uri.parse(uriStr)
            uri.path?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (_: Exception) {}
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

                val instanteOriginal = if (instanteIdParaEditar != null && instanteIdParaEditar > 0L) {
                    repository.obtenerInstantePorId(instanteIdParaEditar)
                } else null

                val instante = InstanteEntity(
                    id = instanteIdParaEditar ?: 0L,
                    titulo = titulo.trim(),
                    reflexion = reflexion.trim(),
                    fotoUri = fotoUri,
                    lugar = lugar.ifBlank { "Lugar desconocido" },
                    latitud = instanteOriginal?.latitud,
                    longitud = instanteOriginal?.longitud,
                    fechaHora = instanteOriginal?.fechaHora ?: System.currentTimeMillis(),
                    esFavorito = instanteOriginal?.esFavorito ?: false,
                    citaId = instanteOriginal?.cita?.id,
                    archivado = instanteOriginal?.archivado ?: false
                )

                if (instanteIdParaEditar != null && instanteIdParaEditar > 0L) {
                    repository.actualizarInstanteCompleto(
                        instanteId = instanteIdParaEditar,
                        instante = instante,
                        etiquetaIds = seleccionadas,
                        cita = citaEntity
                    )
                    _eventoUi.emit("Instante actualizado con éxito")
                } else {
                    repository.insertarInstanteCompleto(instante, seleccionadas, citaEntity)
                    _eventoUi.emit(context.getString(R.string.instante_guardado_exito))
                }
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

    class Factory(
        private val repository: InstantesRepository,
        private val instanteId: Long? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NuevoInstanteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NuevoInstanteViewModel(repository, instanteId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
