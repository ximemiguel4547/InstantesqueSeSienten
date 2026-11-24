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
    fun eliminarEtiquetasDeInstante(instanteId: Long): Int

    @Update
    fun actualizarInstante(instante: InstanteEntity): Int

    @Delete
    fun eliminarInstante(instante: InstanteEntity): Int

    @Query("UPDATE instantes SET esFavorito = :esFavorito WHERE id = :id")
    fun actualizarFavorito(id: Long, esFavorito: Boolean): Int

    @Transaction
    @Query("SELECT * FROM instantes ORDER BY fechaHora DESC")
    fun obtenerTodosInstantesCompletos(): Flow<List<InstanteCompleto>>

    @Transaction
    @Query("SELECT * FROM instantes WHERE esFavorito = 1 ORDER BY fechaHora DESC")
    fun obtenerInstantesFavoritos(): Flow<List<InstanteCompleto>>

    @Transaction
    @Query("""
        SELECT * FROM instantes 
        WHERE titulo LIKE '%' || :query || '%' 
           OR reflexion LIKE '%' || :query || '%' 
           OR lugar LIKE '%' || :query || '%' 
        ORDER BY fechaHora DESC
    """)
    fun buscarInstantes(query: String): Flow<List<InstanteCompleto>>

    @Transaction
    @Query("""
        SELECT DISTINCT instantes.* FROM instantes 
        INNER JOIN instante_etiqueta ON instantes.id = instante_etiqueta.instanteId 
        INNER JOIN etiquetas ON instante_etiqueta.etiquetaId = etiquetas.id 
        WHERE etiquetas.nombre = :nombreEtiqueta 
        ORDER BY fechaHora DESC
    """)
    fun obtenerInstantesPorEtiqueta(nombreEtiqueta: String): Flow<List<InstanteCompleto>>
}
