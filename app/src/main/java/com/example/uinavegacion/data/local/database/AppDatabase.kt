package com.example.uinavegacion.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.uinavegacion.data.local.admin.AdminDao
import com.example.uinavegacion.data.local.admin.AdminEntity
import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.juego.JuegoEntity
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.local.library.LibraryDao
import com.example.uinavegacion.data.local.library.LibraryEntity

/**
 * AppDatabase SIMPLIFICADO - Solo caché mínima
 * 
 * Tablas mantenidas (SOLO 4):
 * - users: Caché de usuarios (TTL: 30 min)
 * - admins: Administradores locales
 * - juegos: Caché de juegos (TTL: 1 hora)
 * - biblioteca: Caché de biblioteca (TTL: 15 min)
 * 
 * Tablas ELIMINADAS (obtener de microservicios):
 * - categorias, generos, estados, roles
 * - licencias, ordenCompra, detalles
 * - reservas, resenas
 */
@Database(
    entities = [
        UserEntity::class,
        AdminEntity::class,
        JuegoEntity::class,
        LibraryEntity::class
    ],
    version = 27, // Incrementar para forzar migración destructiva
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun adminDao(): AdminDao
    abstract fun juegoDao(): JuegoDao
    abstract fun libraryDao(): LibraryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"
        
        // Migración destructiva: Eliminar todo y empezar de cero
        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 26->27: Limpieza masiva - eliminando tablas innecesarias...")
                
                // Eliminar tablas que ya no existen en el código
                val tablesToDrop = listOf(
                    "categorias", "generos", "estados", "roles",
                    "licencias", "ordenCompra", "detalles",
                    "reservas", "resenas"
                )
                
                tablesToDrop.forEach { table ->
                    try {
                        database.execSQL("DROP TABLE IF EXISTS $table")
                        Log.d("AppDatabase", "  ✓ Eliminada tabla: $table")
                    } catch (e: Exception) {
                        Log.w("AppDatabase", "  ⚠ No se pudo eliminar $table (puede no existir)")
                    }
                }
                
                // Agregar columna cachedAt a tablas existentes si no existe
                try {
                    database.execSQL("ALTER TABLE users ADD COLUMN cachedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                } catch (e: Exception) {
                    Log.d("AppDatabase", "  columna cachedAt ya existe en users")
                }
                
                try {
                    database.execSQL("ALTER TABLE juegos ADD COLUMN cachedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                } catch (e: Exception) {
                    Log.d("AppDatabase", "  columna cachedAt ya existe en juegos")
                }
                
                try {
                    database.execSQL("ALTER TABLE biblioteca ADD COLUMN cachedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                } catch (e: Exception) {
                    Log.d("AppDatabase", "  columna cachedAt ya existe en biblioteca")
                }
                
                Log.d("AppDatabase", "✅ MIGRATION 26->27: Limpieza completada")
            }
        }
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_26_27)
                    .fallbackToDestructiveMigration() // Permitir recreación si hay problemas
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "🎉 onCreate: Base de datos creada (caché mínima)")
                            
                            // Seed mínimo solo para admins
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val dbInstance = INSTANCE ?: return@launch
                                    val adminDao = dbInstance.adminDao()
                                    
                                    // Crear admins si no existen
                                    val adminSeed = listOf(
                                        AdminEntity(name = "Administrador Principal", email = "admin@steamish.com", phone = "+56 9 8877 6655", password = "Admin123!", role = "SUPER_ADMIN"),
                                        AdminEntity(name = "Gerente de Juegos", email = "manager@steamish.com", phone = "+56 9 7766 5544", password = "Manager456@", role = "GAME_MANAGER"),
                                        AdminEntity(name = "Soporte Técnico", email = "support@steamish.com", phone = "+56 9 6655 4433", password = "Support789#", role = "SUPPORT"),
                                        AdminEntity(name = "Moderador", email = "moderador@steamish.com", phone = "+56 9 5544 3322", password = "Moderador123!", role = "MODERATOR")
                                    )
                                    
                                    if (adminDao.count() == 0) {
                                        adminSeed.forEach { adminDao.insert(it) }
                                        Log.d("AppDatabase", "✅ Admins creados")
                                    }
                                } catch (e: Exception) {
                                    Log.e("AppDatabase", "Error en onCreate: ${e.message}", e)
                                }
                            }
                        }
                        
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("AppDatabase", "🔓 onOpen: Verificando admins...")
                            
                            // Asegurar que existan los admins
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val dbInstance = INSTANCE ?: return@launch
                                    val adminDao = dbInstance.adminDao()
                                    
                                    val adminSeed = listOf(
                                        AdminEntity(name = "Administrador Principal", email = "admin@steamish.com", phone = "+56 9 8877 6655", password = "Admin123!", role = "SUPER_ADMIN"),
                                        AdminEntity(name = "Gerente de Juegos", email = "manager@steamish.com", phone = "+56 9 7766 5544", password = "Manager456@", role = "GAME_MANAGER"),
                                        AdminEntity(name = "Soporte Técnico", email = "support@steamish.com", phone = "+56 9 6655 4433", password = "Support789#", role = "SUPPORT"),
                                        AdminEntity(name = "Moderador", email = "moderador@steamish.com", phone = "+56 9 5544 3322", password = "Moderador123!", role = "MODERATOR")
                                    )
                                    
                                    adminSeed.forEach { admin ->
                                        try {
                                            val exists = adminDao.getByEmail(admin.email)
                                            if (exists == null) {
                                                adminDao.insert(admin)
                                                Log.d("AppDatabase", "✅ Admin ${admin.email} creado")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("AppDatabase", "Error verificando admin ${admin.email}: ${e.message}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("AppDatabase", "Error en onOpen: ${e.message}", e)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
