package com.instantes.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instantes.app.data.entity.EtiquetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EtiquetaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarEtiqueta(etiqueta: EtiquetaEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarEtiquetas(etiquetas: List<EtiquetaEntity>)

    @Update
    fun actualizarEtiqueta(etiqueta: EtiquetaEntity)

    @Delete
    fun eliminarEtiqueta(etiqueta: EtiquetaEntity)

    @Query("SELECT * FROM etiquetas ORDER BY nombre ASC")
    fun obtenerTodasEtiquetas(): Flow<List<EtiquetaEntity>>

    @Query("SELECT * FROM etiquetas WHERE nombre = :nombre LIMIT 1")
    fun obtenerEtiquetaPorNombre(nombre: String): EtiquetaEntity?
}
