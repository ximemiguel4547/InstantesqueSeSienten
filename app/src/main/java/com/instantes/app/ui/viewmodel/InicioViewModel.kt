package com.instantes.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModel(private val repository: InstantesRepository) : ViewModel() {

    var busqueda by mutableStateOf("")
        private set

    var filtroActual by mutableIntStateOf(0)
        private set

    private val _busquedaFlow = MutableStateFlow("")
    private val _filtroFlow = MutableStateFlow(0)

    val todasLasEtiquetas: StateFlow<List<Etiqueta>> = repository.todasLasEtiquetas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val instantes: StateFlow<List<Instante>> = combine(
        _busquedaFlow,
        _filtroFlow
    ) { query, filtro ->
        Pair(query, filtro)
    }.flatMapLatest { (query, filtro) ->
        val baseFlow = when (filtro) {
            1 -> repository.obtenerFavoritos()
            4 -> repository.instantesArchivados
            else -> {
                // If filtro >= 2, check if it's a dynamic tag filter
                repository.todosLosInstantes
            }
        }
        baseFlow.map { lista ->
            // If filtro is specific tag index >= 2, filter by tag name
            // We handle dynamic tags in UI or here. Let's filter here if needed or pass through.
            lista
        }
    }.combine(todasLasEtiquetas) { lista, etiquetas ->
        val filtro = _filtroFlow.value
        if (filtro >= 2 && filtro - 2 < etiquetas.size) {
            val etiquetaSeleccionada = etiquetas[filtro - 2].nombre
            lista.filter { instante ->
                instante.etiquetas.any { it.nombre.equals(etiquetaSeleccionada, ignoreCase = true) }
            }
        } else {
            lista
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

    fun actualizarFiltro(nuevoFiltro: Int) {
        filtroActual = nuevoFiltro
        _filtroFlow.value = nuevoFiltro
    }

    fun alternarFavorito(instanteId: Long, esFavoritoActual: Boolean) {
        viewModelScope.launch {
            repository.actualizarFavorito(instanteId, !esFavoritoActual)
        }
    }

    fun archivarInstante(instanteId: Long, archivadoActual: Boolean) {
        viewModelScope.launch {
            repository.actualizarArchivado(instanteId, !archivadoActual)
        }
    }

    fun eliminarInstante(instanteId: Long, fotoUri: String) {
        viewModelScope.launch {
            repository.eliminarInstante(instanteId, fotoUri)
        }
    }

    fun agregarEtiqueta(nombre: String) {
        viewModelScope.launch {
            if (nombre.isNotBlank()) {
                repository.insertarEtiqueta(nombre)
            }
        }
    }

    fun eliminarEtiqueta(etiqueta: Etiqueta) {
        viewModelScope.launch {
            repository.eliminarEtiqueta(
                EtiquetaEntity(id = etiqueta.id, nombre = etiqueta.nombre)
            )
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
