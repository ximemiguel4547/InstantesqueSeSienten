package com.instantes.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "instante_etiqueta",
    primaryKeys = ["instanteId", "etiquetaId"],
    foreignKeys = [
        ForeignKey(
            entity = InstanteEntity::class,
            parentColumns = ["id"],
            childColumns = ["instanteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EtiquetaEntity::class,
            parentColumns = ["id"],
            childColumns = ["etiquetaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["etiquetaId"])]
)
data class InstanteEtiquetaCrossRef(
    val instanteId: Long,
    val etiquetaId: Long
)
