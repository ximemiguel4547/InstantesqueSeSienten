package com.instantes.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.domain.model.Instante
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface FiltroInicio {
    object Todos : FiltroInicio
    object Favoritos : FiltroInicio
    object Archivados : FiltroInicio
    data class Etiqueta(val nombre: String) : FiltroInicio
}

@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModel(private val repository: InstantesRepository) : ViewModel() {

    var busqueda by mutableStateOf("")
        private set

    var filtroActual by mutableStateOf<FiltroInicio>(FiltroInicio.Todos)
        private set

    private val _busquedaFlow = MutableStateFlow("")
    private val _filtroFlow = MutableStateFlow<FiltroInicio>(FiltroInicio.Todos)

    private val _eventoUi = MutableSharedFlow<String>()
    val eventoUi: SharedFlow<String> = _eventoUi.asSharedFlow()

    val todasLasEtiquetas: StateFlow<List<Etiqueta>> = repository.todasLasEtiquetas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todosLosInstantes: StateFlow<List<Instante>> = repository.todosLosInstantes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val instantes: StateFlow<List<Instante>> = _filtroFlow.flatMapLatest { filtro ->
        when (filtro) {
            is FiltroInicio.Favoritos -> repository.obtenerFavoritos()
            is FiltroInicio.Archivados -> repository.instantesArchivados
            is FiltroInicio.Etiqueta -> repository.obtenerInstantesPorEtiqueta(filtro.nombre)
            is FiltroInicio.Todos -> repository.todosLosInstantes
        }
    }.combine(_busquedaFlow) { lista, query ->
        if (query.isBlank()) {
            lista
        } else {
            lista.filter {
                it.titulo.contains(query, ignoreCase = true) ||
                it.reflexion.contains(query, ignoreCase = true) ||
                it.lugar.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun actualizarBusqueda(nuevaBusqueda: String) {
        busqueda = nuevaBusqueda
        _busquedaFlow.value = nuevaBusqueda
    }

    fun actualizarFiltro(nuevoFiltro: FiltroInicio) {
        filtroActual = nuevoFiltro
        _filtroFlow.value = nuevoFiltro
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
                val etAntigua = todasLasEtiquetas.value.find { it.id == id }
                repository.actualizarEtiqueta(EtiquetaEntity(id = id, nombre = trimmed))
                val filtro = filtroActual
                if (filtro is FiltroInicio.Etiqueta && etAntigua != null && filtro.nombre.equals(etAntigua.nombre, ignoreCase = true)) {
                    actualizarFiltro(FiltroInicio.Todos)
                }
            } catch (_: Exception) {
                _eventoUi.emit("Ya existe una etiqueta con ese nombre")
            }
        }
    }

    fun eliminarEtiqueta(etiqueta: Etiqueta) {
        viewModelScope.launch {
            try {
                repository.eliminarEtiqueta(EtiquetaEntity(id = etiqueta.id, nombre = etiqueta.nombre))
                val filtro = filtroActual
                if (filtro is FiltroInicio.Etiqueta && filtro.nombre.equals(etiqueta.nombre, ignoreCase = true)) {
                    actualizarFiltro(FiltroInicio.Todos)
                }
            } catch (_: Exception) {
                _eventoUi.emit("Error al eliminar la etiqueta")
            }
        }
    }

    fun alternarFavorito(instanteId: Long, esFavoritoActual: Boolean) {
        viewModelScope.launch {
            try {
                repository.actualizarFavorito(instanteId, !esFavoritoActual)
            } catch (_: Exception) {
                _eventoUi.emit("Error al actualizar favorito")
            }
        }
    }

    fun archivarInstante(instanteId: Long, archivadoActual: Boolean) {
        viewModelScope.launch {
            try {
                repository.actualizarArchivado(instanteId, !archivadoActual)
            } catch (_: Exception) {
                _eventoUi.emit("Error al actualizar archivo")
            }
        }
    }

    fun eliminarInstante(instanteId: Long, fotoUri: String) {
        viewModelScope.launch {
            try {
                repository.eliminarInstante(instanteId, fotoUri)
            } catch (_: Exception) {
                _eventoUi.emit("Error al eliminar el instante")
            }
        }
    }

    class Factory(private val repository: InstantesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InicioViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InicioViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
