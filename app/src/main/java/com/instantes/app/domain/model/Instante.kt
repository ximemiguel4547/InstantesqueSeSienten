package com.instantes.app.domain.model

data class Instante(
    val id: Long,
    val titulo: String,
    val reflexion: String,
    val fotoUri: String,
    val lugar: String,
    val latitud: Double?,
    val longitud: Double?,
    val fechaHora: Long,
    val esFavorito: Boolean,
    val archivado: Boolean,
    val cita: Cita?,
    val etiquetas: List<Etiqueta>
)
