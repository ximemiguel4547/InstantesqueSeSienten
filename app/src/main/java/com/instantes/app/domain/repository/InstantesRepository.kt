package com.instantes.app.domain.repository

import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.data.entity.InstanteEntity
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.domain.model.Instante
import kotlinx.coroutines.flow.Flow

interface InstantesRepository {
    val todosLosInstantes: Flow<List<Instante>>
    val todasLasEtiquetas: Flow<List<Etiqueta>>

    fun buscarInstantes(query: String): Flow<List<Instante>>
    fun obtenerInstantesPorEtiqueta(nombreEtiqueta: String): Flow<List<Instante>>
    fun obtenerFavoritos(): Flow<List<Instante>>

    suspend fun insertarInstanteCompleto(
        instante: InstanteEntity,
        etiquetaIds: List<Long>,
        cita: CitaEntity?
    ): Long

    suspend fun actualizarFavorito(id: Long, esFavorito: Boolean)
    suspend fun eliminarInstante(instante: InstanteEntity)
    suspend fun obtenerEtiquetaPorNombre(nombre: String): EtiquetaEntity?
}
