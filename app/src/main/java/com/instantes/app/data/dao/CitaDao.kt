package com.instantes.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.instantes.app.data.entity.CitaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarCita(cita: CitaEntity): Long

    @Update
    fun actualizarCita(cita: CitaEntity)

    @Delete
    fun eliminarCita(cita: CitaEntity)

    @Query("UPDATE citas SET esFavorita = :esFavorita WHERE id = :id")
    fun actualizarFavoritoCita(id: Long, esFavorita: Boolean)

    @Query("SELECT COUNT(*) FROM citas")
    fun contarCitas(): Flow<Int>

    @Query("SELECT * FROM citas ORDER BY id DESC")
    fun obtenerTodasCitas(): Flow<List<CitaEntity>>

    @Query("SELECT * FROM citas WHERE id = :id")
    fun obtenerCitaPorId(id: Long): CitaEntity?
}
