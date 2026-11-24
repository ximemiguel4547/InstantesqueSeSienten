package com.instantes.app.domain.model

data class Cita(
    val id: Long,
    val texto: String,
    val autor: String,
    val obra: String?,
    val esFavorita: Boolean
)
