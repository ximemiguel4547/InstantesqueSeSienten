package com.instantes.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.instantes.app.data.dao.CitaDao
import com.instantes.app.data.dao.EtiquetaDao
import com.instantes.app.data.dao.InstanteDao
import com.instantes.app.data.entity.CitaEntity
import com.instantes.app.data.entity.EtiquetaEntity
import com.instantes.app.data.entity.InstanteEntity
import com.instantes.app.data.entity.InstanteEtiquetaCrossRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        InstanteEntity::class,
        EtiquetaEntity::class,
        CitaEntity::class,
        InstanteEtiquetaCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun instanteDao(): InstanteDao
    abstract fun etiquetaDao(): EtiquetaDao
    abstract fun citaDao(): CitaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "instantes_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val etiquetaDao = database.etiquetaDao()
                        etiquetaDao.insertarEtiquetas(
                            listOf(
                                EtiquetaEntity(nombre = "Naturaleza"),
                                EtiquetaEntity(nombre = "Arte"),
                                EtiquetaEntity(nombre = "Atardecer"),
                                EtiquetaEntity(nombre = "Ciudad")
                            )
                        )
                    }
                }
            }
        }
    }
}
