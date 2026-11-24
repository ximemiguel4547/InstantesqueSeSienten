package com.instantes.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "etiquetas",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class EtiquetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String
)
