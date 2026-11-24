package com.instantes.app

import android.app.Application
import com.instantes.app.data.local.AppDatabase
import com.instantes.app.data.repository.InstantesRepositoryImpl
import com.instantes.app.domain.repository.InstantesRepository

class InstantesApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository: InstantesRepository by lazy {
        InstantesRepositoryImpl(
            database.instanteDao(),
            database.etiquetaDao(),
            database.citaDao()
        )
    }
}
