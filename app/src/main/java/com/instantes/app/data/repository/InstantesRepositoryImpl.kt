package com.instantes.app.data.repository

import android.net.Uri
import com.instantes.app.data.dao.CitaDao
import com.instantes.app.data.dao.EtiquetaDao
import com.instantes.app.data.dao.InstanteDao
import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.data.entity.InstanteEntity
import com.instantes.app.data.entity.InstanteEtiquetaCrossRef
import com.instantes.app.data.mapper.toDomain
import com.instantes.app.domain.model.Cita
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.domain.model.Instante
import com.instantes.app.domain.repository.InstantesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class InstantesRepositoryImpl(
    private val instanteDao: InstanteDao,
    private val etiquetaDao: EtiquetaDao,
    private val citaDao: CitaDao
) : InstantesRepository {

    override val todosLosInstantes: Flow<List<Instante>> =
        instanteDao.obtenerInstantesNoArchivados().map { lista -> lista.map { it.toDomain() } }

    override val instantesArchivados: Flow<List<Instante>> =
        instanteDao.obtenerInstantesArchivados().map { lista -> lista.map { it.toDomain() } }

    override val todasLasEtiquetas: Flow<List<Etiqueta>> =
        etiquetaDao.obtenerTodasEtiquetas().map { lista -> lista.map { it.toDomain() } }

    override val todasLasCitas: Flow<List<Cita>> =
        citaDao.obtenerTodasCitas().map { lista -> lista.map { it.toDomain() } }

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

    override suspend fun obtenerInstantePorId(id: Long): Instante? = withContext(Dispatchers.IO) {
        instanteDao.obtenerInstanteCompletoPorId(id)?.toDomain()
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

    override suspend fun actualizarInstanteCompleto(
        instanteId: Long,
        instante: InstanteEntity,
        etiquetaIds: List<Long>,
        cita: CitaEntity?
    ) = withContext(Dispatchers.IO) {
        var citaIdFinal = instante.citaId
        if (cita != null && cita.texto.isNotBlank()) {
            if (citaIdFinal != null && citaIdFinal > 0L) {
                citaDao.actualizarCita(cita.copy(id = citaIdFinal))
            } else {
                citaIdFinal = citaDao.insertarCita(cita)
            }
        } else {
            citaIdFinal = null
        }

        val instanteActualizado = instante.copy(id = instanteId, citaId = citaIdFinal)
        instanteDao.actualizarInstante(instanteActualizado)

        instanteDao.eliminarEtiquetasDeInstante(instanteId)
        for (etiquetaId in etiquetaIds) {
            instanteDao.insertarInstanteEtiqueta(
                InstanteEtiquetaCrossRef(instanteId = instanteId, etiquetaId = etiquetaId)
            )
        }
    }

    override suspend fun actualizarFavorito(id: Long, esFavorito: Boolean) = withContext(Dispatchers.IO) {
        instanteDao.actualizarFavorito(id, esFavorito)
    }

    override suspend fun actualizarArchivado(id: Long, archivado: Boolean) = withContext(Dispatchers.IO) {
        instanteDao.actualizarArchivado(id, archivado)
    }

    override suspend fun eliminarInstante(id: Long, fotoUri: String) = withContext(Dispatchers.IO) {
        try {
            if (fotoUri.isNotBlank()) {
                val uri = Uri.parse(fotoUri)
                if (uri.path != null) {
                    val file = File(uri.path!!)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        } catch (_: Exception) {}

        instanteDao.eliminarInstantePorId(id)
    }

    override suspend fun insertarEtiqueta(nombre: String): Long = withContext(Dispatchers.IO) {
        etiquetaDao.insertarEtiqueta(EtiquetaEntity(nombre = nombre.trim()))
    }

    override suspend fun eliminarEtiqueta(etiqueta: EtiquetaEntity) = withContext(Dispatchers.IO) {
        etiquetaDao.eliminarEtiqueta(etiqueta)
    }

    override suspend fun obtenerEtiquetaPorNombre(nombre: String): EtiquetaEntity? = withContext(Dispatchers.IO) {
        etiquetaDao.obtenerEtiquetaPorNombre(nombre)
    }

    override suspend fun insertarCita(cita: CitaEntity): Long = withContext(Dispatchers.IO) {
        citaDao.insertarCita(cita)
    }

    override suspend fun actualizarCita(cita: CitaEntity) = withContext(Dispatchers.IO) {
        citaDao.actualizarCita(cita)
    }

    override suspend fun eliminarCita(cita: CitaEntity) = withContext(Dispatchers.IO) {
        citaDao.eliminarCita(cita)
    }

    override suspend fun actualizarFavoritoCita(id: Long, esFavorita: Boolean) = withContext(Dispatchers.IO) {
        citaDao.actualizarFavoritoCita(id, esFavorita)
    }
}
