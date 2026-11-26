package com.instantes.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.instantes.app.data.entity.InstanteCompleto
import com.instantes.app.data.entity.InstanteEntity
import com.instantes.app.data.entity.InstanteEtiquetaCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarInstante(instante: InstanteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarInstanteEtiqueta(crossRef: InstanteEtiquetaCrossRef): Long

    @Query("DELETE FROM instante_etiqueta WHERE instanteId = :instanteId")
    fun eliminarEtiquetasDeInstante(instanteId: Long)

    @Update
    fun actualizarInstante(instante: InstanteEntity)

    @Query("DELETE FROM instantes WHERE id = :id")
    fun eliminarInstantePorId(id: Long)

    @Query("UPDATE instantes SET esFavorito = :esFavorito WHERE id = :id")
    fun actualizarFavorito(id: Long, esFavorito: Boolean)

    @Query("UPDATE instantes SET archivado = :archivado WHERE id = :id")
    fun actualizarArchivado(id: Long, archivado: Boolean)

    @Query("SELECT COUNT(*) FROM instantes")
    fun contarInstantes(): Flow<Int>

    @Query("SELECT COUNT(*) FROM instantes WHERE esFavorito = 1")
    fun contarFavoritos(): Flow<Int>

    @Query("""
        SELECT etiquetas.nombre FROM etiquetas 
        INNER JOIN instante_etiqueta ON etiquetas.id = instante_etiqueta.etiquetaId 
        GROUP BY etiquetas.id 
        ORDER BY COUNT(instante_etiqueta.instanteId) DESC 
        LIMIT 1
    """)
    fun obtenerEtiquetaMasUsada(): Flow<String?>

    @Transaction
    @Query("SELECT * FROM instantes WHERE id = :id")
    fun obtenerInstanteCompletoPorId(id: Long): InstanteCompleto?

    @Transaction
    @Query("SELECT * FROM instantes WHERE archivado = 0 ORDER BY fechaHora DESC")
    fun obtenerInstantesNoArchivados(): Flow<List<InstanteCompleto>>

    @Transaction
    @Query("SELECT * FROM instantes WHERE archivado = 1 ORDER BY fechaHora DESC")
    fun obtenerInstantesArchivados(): Flow<List<InstanteCompleto>>

    @Transaction
    @Query("SELECT * FROM instantes WHERE esFavorito = 1 AND archivado = 0 ORDER BY fechaHora DESC")
    fun obtenerInstantesFavoritos(): Flow<List<InstanteCompleto>>

    @Transaction
    @Query("""
        SELECT * FROM instantes 
        WHERE archivado = 0 AND (titulo LIKE '%' || :query || '%' 
           OR reflexion LIKE '%' || :query || '%' 
           OR lugar LIKE '%' || :query || '%') 
        ORDER BY fechaHora DESC
    """)
    fun buscarInstantes(query: String): Flow<List<InstanteCompleto>>

    @Transaction
    @Query("""
        SELECT DISTINCT instantes.* FROM instantes 
        INNER JOIN instante_etiqueta ON instantes.id = instante_etiqueta.instanteId 
        INNER JOIN etiquetas ON instante_etiqueta.etiquetaId = etiquetas.id 
        WHERE instantes.archivado = 0 AND etiquetas.nombre = :nombreEtiqueta 
        ORDER BY fechaHora DESC
    """)
    fun obtenerInstantesPorEtiqueta(nombreEtiqueta: String): Flow<List<InstanteCompleto>>
}
