package com.instantes.app.data.repository

import com.instantes.app.data.dao.CitaDao
import com.instantes.app.data.dao.EtiquetaDao
import com.instantes.app.data.dao.InstanteDao
import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.data.entity.InstanteCompleto
import com.instantes.app.data.entity.InstanteEntity
import com.instantes.app.data.entity.InstanteEtiquetaCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InstantesRepository(
    private val instanteDao: InstanteDao,
    private val etiquetaDao: EtiquetaDao,
    private val citaDao: CitaDao
) {
    val todosLosInstantes: Flow<List<InstanteCompleto>> = instanteDao.obtenerTodosInstantesCompletos()
    val todasLasEtiquetas: Flow<List<EtiquetaEntity>> = etiquetaDao.obtenerTodasEtiquetas()
    val todasLasCitas: Flow<List<CitaEntity>> = citaDao.obtenerTodasCitas()

    fun buscarInstantes(query: String): Flow<List<InstanteCompleto>> {
        return if (query.isBlank()) {
            todosLosInstantes
        } else {
            instanteDao.buscarInstantes(query)
        }
    }

    fun obtenerInstantesPorEtiqueta(nombreEtiqueta: String): Flow<List<InstanteCompleto>> {
        return instanteDao.obtenerInstantesPorEtiqueta(nombreEtiqueta)
    }

    fun obtenerFavoritos(): Flow<List<InstanteCompleto>> {
        return instanteDao.obtenerInstantesFavoritos()
    }

    suspend fun insertarInstanteCompleto(
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

    suspend fun actualizarFavorito(id: Long, esFavorito: Boolean) = withContext(Dispatchers.IO) {
        instanteDao.actualizarFavorito(id, esFavorito)
    }

    suspend fun eliminarInstante(instante: InstanteEntity) = withContext(Dispatchers.IO) {
        instanteDao.eliminarInstante(instante)
    }

    suspend fun obtenerEtiquetaPorNombre(nombre: String): EtiquetaEntity? = withContext(Dispatchers.IO) {
        etiquetaDao.obtenerEtiquetaPorNombre(nombre)
    }
}
