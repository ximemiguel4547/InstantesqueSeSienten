package com.instantes.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "instantes",
    foreignKeys = [
        ForeignKey(
            entity = CitaEntity::class,
            parentColumns = ["id"],
            childColumns = ["citaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["citaId"])]
)
data class InstanteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val titulo: String,
    val reflexion: String,
    val fotoUri: String,
    val lugar: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fechaHora: Long,
    val esFavorito: Boolean = false,
    val citaId: Long? = null
)
