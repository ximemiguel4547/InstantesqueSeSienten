package com.instantes.app.data.repository

import com.instantes.app.data.dao.CitaDao
import com.instantes.app.data.dao.EtiquetaDao
import com.instantes.app.data.dao.InstanteDao
import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.data.entity.InstanteEntity
import com.instantes.app.data.entity.InstanteEtiquetaCrossRef
import com.instantes.app.data.mapper.toDomain
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.domain.model.Instante
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class InstantesRepositoryImpl(
    private val instanteDao: InstanteDao,
    private val etiquetaDao: EtiquetaDao,
    private val citaDao: CitaDao
) : InstantesRepository {

    override val todosLosInstantes: Flow<List<Instante>> =
        instanteDao.obtenerTodosInstantesCompletos().map { lista -> lista.map { it.toDomain() } }

    override val todasLasEtiquetas: Flow<List<Etiqueta>> =
        etiquetaDao.obtenerTodasEtiquetas().map { lista -> lista.map { it.toDomain() } }

    override fun buscarInstantes(query: String): Flow<List<Instante>> {
        return if (query.isBlank()) {
            todosLosInstantes
        } else {
            instanteDao.buscarInstantes(query).map { lista -> lista.map { it.toDomain() } }
        }
    }

    override fun obtenerInstantesPorEtiqueta(nombreEtiqueta: String): Flow<List<Instante>> {
        return instanteDao.obtenerInstantesPorEtiqueta(nombreEtiqueta).map { lista -> lista.map { it.toDomain() } }
    }

    override fun obtenerFavoritos(): Flow<List<Instante>> {
        return instanteDao.obtenerInstantesFavoritos().map { lista -> lista.map { it.toDomain() } }
    }

    override suspend fun insertarInstanteCompleto(
        instante: InstanteEntity,
        etiquetaIds: List<Long>,
        cita: CitaEntity?
    ): Long = withContext(Dispatchers.IO) {
        var citaIdFinal = instante.citaId
        if (cita != null && cita.texto.isNotBlank()) {
            citaIdFinal = citaDao.insertarCita(cita)
        }
        val instanteConCita = instante.copy(citaId = citaIdFinal)
        val instanteId = instanteDao.insertarInstante(instanteConCita)

        instanteDao.eliminarEtiquetasDeInstante(instanteId)
        for (etiquetaId in etiquetaIds) {
            instanteDao.insertarInstanteEtiqueta(
                InstanteEtiquetaCrossRef(instanteId = instanteId, etiquetaId = etiquetaId)
            )
        }
        instanteId
    }

    override suspend fun actualizarFavorito(id: Long, esFavorito: Boolean) {
        withContext(Dispatchers.IO) {
            instanteDao.actualizarFavorito(id, esFavorito)
        }
    }

    override suspend fun eliminarInstante(instante: InstanteEntity) {
        withContext(Dispatchers.IO) {
            instanteDao.eliminarInstante(instante)
        }
    }

    override suspend fun obtenerEtiquetaPorNombre(nombre: String): EtiquetaEntity? = withContext(Dispatchers.IO) {
        etiquetaDao.obtenerEtiquetaPorNombre(nombre)
    }
}
