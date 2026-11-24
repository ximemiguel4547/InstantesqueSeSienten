package com.instantes.app.data.mapper

import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.data.entity.InstanteCompleto
import com.instantes.app.domain.model.Cita
import com.instantes.app.domain.model.Etiqueta
import com.instantes.app.domain.model.Instante

fun CitaEntity.toDomain(): Cita {
    return Cita(
        id = id,
        texto = texto,
        autor = autor,
        obra = obra,
        esFavorita = esFavorita
    )
}

fun EtiquetaEntity.toDomain(): Etiqueta {
    return Etiqueta(
        id = id,
        nombre = nombre
    )
}

fun InstanteCompleto.toDomain(): Instante {
    return Instante(
        id = instante.id,
        titulo = instante.titulo,
        reflexion = instante.reflexion,
        fotoUri = instante.fotoUri,
        lugar = instante.lugar,
        latitud = instante.latitud,
        longitud = instante.longitud,
        fechaHora = instante.fechaHora,
        esFavorito = instante.esFavorito,
        cita = cita?.toDomain(),
        etiquetas = etiquetas.map { it.toDomain() }
    )
}
