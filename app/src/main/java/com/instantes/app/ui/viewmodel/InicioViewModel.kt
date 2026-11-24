package com.instantes.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    val instantes: StateFlow<List<Instante>> = combine(
        _busquedaFlow,
        _filtroFlow
    ) { query, filtro ->
        Pair(query, filtro)
    }.flatMapLatest { (query, filtro) ->
        val baseFlow = when (filtro) {
            1 -> repository.obtenerFavoritos()
            2 -> repository.obtenerInstantesPorEtiqueta("Naturaleza")
            3 -> repository.obtenerInstantesPorEtiqueta("Arte")
            else -> repository.todosLosInstantes
        }
        baseFlow.map { lista ->
            if (query.isBlank()) {
                lista
            } else {
                lista.filter {
                    it.titulo.contains(query, ignoreCase = true) ||
                    it.reflexion.contains(query, ignoreCase = true) ||
                    it.lugar.contains(query, ignoreCase = true)
                }
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
