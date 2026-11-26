package com.instantes.app.domain.repository

import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.data.entity.InstanteEntity
import com.instantes.app.domain.model.Cita
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.domain.model.Instante
import kotlinx.coroutines.flow.Flow

interface InstantesRepository {
    val todosLosInstantes: Flow<List<Instante>>
    val instantesArchivados: Flow<List<Instante>>
    val todasLasEtiquetas: Flow<List<Etiqueta>>
    val todasLasCitas: Flow<List<Cita>>
    val totalInstantes: Flow<Int>
    val totalFavoritos: Flow<Int>
    val totalCitas: Flow<Int>
    val etiquetaMasUsada: Flow<String?>

    fun buscarInstantes(query: String): Flow<List<Instante>>
    fun obtenerInstantesPorEtiqueta(nombreEtiqueta: String): Flow<List<Instante>>
    fun obtenerFavoritos(): Flow<List<Instante>>

    suspend fun obtenerInstantePorId(id: Long): Instante?

    suspend fun insertarInstanteCompleto(
        instante: InstanteEntity,
        etiquetaIds: List<Long>,
        cita: CitaEntity?
    ): Long

    suspend fun actualizarInstanteCompleto(
        instanteId: Long,
        instante: InstanteEntity,
        etiquetaIds: List<Long>,
        cita: CitaEntity?
    )

    suspend fun actualizarFavorito(id: Long, esFavorito: Boolean)
    suspend fun actualizarArchivado(id: Long, archivado: Boolean)
    suspend fun eliminarInstante(id: Long, fotoUri: String)

    suspend fun insertarEtiqueta(nombre: String): Long
    suspend fun actualizarEtiqueta(etiqueta: EtiquetaEntity)
    suspend fun eliminarEtiqueta(etiqueta: EtiquetaEntity)
    suspend fun obtenerEtiquetaPorNombre(nombre: String): EtiquetaEntity?

    suspend fun insertarCita(cita: CitaEntity): Long
    suspend fun actualizarCita(cita: CitaEntity)
    suspend fun eliminarCita(cita: CitaEntity)
    suspend fun actualizarFavoritoCita(id: Long, esFavorita: Boolean)
}
