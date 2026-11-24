package com.instantes.app.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class InstanteCompleto(
    @Embedded
    val instante: InstanteEntity,

    @Relation(
        parentColumn = "citaId",
        entityColumn = "id"
    )
    val cita: CitaEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = InstanteEtiquetaCrossRef::class,
            parentColumn = "instanteId",
            entityColumn = "etiquetaId"
        )
    )
    val etiquetas: List<EtiquetaEntity>
)
