package com.instantes.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas")
data class CitaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val texto: String,
    val autor: String,
    val obra: String? = null,
    val esFavorita: Boolean = false
)
