package com.instantes.app.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerfilViewModel(private val repository: InstantesRepository) : ViewModel() {
    val totalInstantes: StateFlow<Int> = repository.totalInstantes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalFavoritos: StateFlow<Int> = repository.totalFavoritos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalCitas: StateFlow<Int> = repository.totalCitas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val todasLasEtiquetas: StateFlow<List<Etiqueta>> = repository.todasLasEtiquetas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val etiquetaMasUsada: StateFlow<String?> = repository.etiquetaMasUsada.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _eventoUi = MutableSharedFlow<String>()
    val eventoUi: SharedFlow<String> = _eventoUi.asSharedFlow()

    fun agregarEtiqueta(nombre: String) {
        viewModelScope.launch {
            val trimmed = nombre.trim()
            if (trimmed.isBlank()) {
                _eventoUi.emit("Escribe un nombre para la etiqueta")
                return@launch
            }
            if (todasLasEtiquetas.value.size >= 10) {
                _eventoUi.emit("Máximo 10 etiquetas")
                return@launch
            }
            val existe = todasLasEtiquetas.value.any { it.nombre.equals(trimmed, ignoreCase = true) }
            if (existe) {
                _eventoUi.emit("Ya existe una etiqueta con ese nombre")
                return@launch
            }
            try {
                repository.insertarEtiqueta(trimmed)
            } catch (_: Exception) {
                _eventoUi.emit("Ya existe una etiqueta con ese nombre")
            }
        }
    }

    fun actualizarEtiqueta(id: Long, nuevoNombre: String) {
        viewModelScope.launch {
            val trimmed = nuevoNombre.trim()
            if (trimmed.isBlank()) {
                _eventoUi.emit("Escribe un nombre para la etiqueta")
                return@launch
            }
            val existe = todasLasEtiquetas.value.any { it.id != id && it.nombre.equals(trimmed, ignoreCase = true) }
            if (existe) {
                _eventoUi.emit("Ya existe una etiqueta con ese nombre")
                return@launch
            }
            try {
                repository.actualizarEtiqueta(EtiquetaEntity(id = id, nombre = trimmed))
            } catch (_: Exception) {
                _eventoUi.emit("Ya existe una etiqueta con ese nombre")
            }
        }
    }

    fun eliminarEtiqueta(etiqueta: Etiqueta) {
        viewModelScope.launch {
            try {
                repository.eliminarEtiqueta(EtiquetaEntity(id = etiqueta.id, nombre = etiqueta.nombre))
            } catch (_: Exception) {
                _eventoUi.emit("Error al eliminar la etiqueta")
            }
        }
    }

    class Factory(private val repository: InstantesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PerfilViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
