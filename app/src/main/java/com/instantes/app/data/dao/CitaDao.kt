package com.instantes.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instantes.app.data.entity.CitaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarCita(cita: CitaEntity): Long

    @Query("SELECT * FROM citas ORDER BY id DESC")
    fun obtenerTodasCitas(): Flow<List<CitaEntity>>

    @Query("SELECT * FROM citas WHERE id = :id")
    fun obtenerCitaPorId(id: Long): CitaEntity?
}
