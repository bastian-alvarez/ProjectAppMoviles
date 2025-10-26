package com.example.uinavegacion.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.uinavegacion.data.local.admin.AdminDao
import com.example.uinavegacion.data.local.admin.AdminEntity
import com.example.uinavegacion.data.local.categoria.CategoriaDao
import com.example.uinavegacion.data.local.categoria.CategoriaEntity
import com.example.uinavegacion.data.local.detalle.DetalleDao
import com.example.uinavegacion.data.local.detalle.DetalleEntity
import com.example.uinavegacion.data.local.estado.EstadoDao
import com.example.uinavegacion.data.local.estado.EstadoEntity
import com.example.uinavegacion.data.local.genero.GeneroDao
import com.example.uinavegacion.data.local.genero.GeneroEntity
import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.juego.JuegoEntity
import com.example.uinavegacion.data.local.licencia.LicenciaDao
import com.example.uinavegacion.data.local.licencia.LicenciaEntity
import com.example.uinavegacion.data.local.ordenCompra.OrdenCompraDao
import com.example.uinavegacion.data.local.ordenCompra.OrdenCompraEntity
import com.example.uinavegacion.data.local.reserva.ReservaDao
import com.example.uinavegacion.data.local.reserva.ReservaEntity
import com.example.uinavegacion.data.local.rol.RolDao
import com.example.uinavegacion.data.local.rol.RolEntity
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    entities = [
        UserEntity::class,
        AdminEntity::class,
        CategoriaEntity::class,
        GeneroEntity::class,
        JuegoEntity::class,
        EstadoEntity::class,
        RolEntity::class,
        LicenciaEntity::class,
        OrdenCompraEntity::class,
        DetalleEntity::class,
        ReservaEntity::class
        ,
        com.example.uinavegacion.data.local.library.LibraryEntity::class
    ],
    version = 13, // Forzar recreación completa con 20 juegos
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun adminDao(): AdminDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun generoDao(): GeneroDao
    abstract fun juegoDao(): JuegoDao
    abstract fun estadoDao(): EstadoDao
    abstract fun rolDao(): RolDao
    abstract fun licenciaDao(): LicenciaDao
    abstract fun ordenCompraDao(): OrdenCompraDao
    abstract fun detalleDao(): DetalleDao
    abstract fun reservaDao(): ReservaDao
    abstract fun libraryDao(): com.example.uinavegacion.data.local.library.LibraryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"
        
        // Migración de versión 5 a 6: Agregar columna isBlocked a users
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columna isBlocked con valor por defecto FALSE
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN isBlocked INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        
        // Migración de versión 6 a 7: Actualizar formato de teléfonos a +56 9
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 6->7: Actualizando formato de teléfonos...")
                // Actualizar teléfonos que no empiezan con +56
                database.execSQL(
                    """
                    UPDATE users 
                    SET phone = '+56 9 ' || phone 
                    WHERE phone NOT LIKE '+56%' AND phone != ''
                    """
                )
                database.execSQL(
                    """
                    UPDATE admins 
                    SET phone = '+56 9 ' || phone 
                    WHERE phone NOT LIKE '+56%' AND phone != ''
                    """
                )
                Log.d("AppDatabase", "MIGRATION 6->7: Teléfonos actualizados correctamente")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration() // Permite recrear la BD si hay problemas de migración
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "onCreate CALLED. Seeding data...")
                            CoroutineScope(Dispatchers.IO).launch {
                                val userDao = getInstance(context).userDao()
                                val adminDao = getInstance(context).adminDao()

                                // Precargamos usuarios con formato chileno +56 9
                                val userSeed = listOf(
                                    UserEntity(name = "Usuario Demo", email = "user1@demo.com", phone = "+56 9 1234 5678", password = "Password123!"),
                                    UserEntity(name = "Usuario Test", email = "test@test.com", phone = "+56 9 8765 4321", password = "Password123!")
                                )


                                
                                val userCount = userDao.count()
                                Log.d("AppDatabase", "User count before seed: $userCount")
                                if (userCount == 0) {
                                    Log.d("AppDatabase", "Seeding users...")
                                    userSeed.forEach { userDao.insert(it) }
                                }

                                // Precargamos administradores con formato chileno +56 9
                                val adminSeed = listOf(
                                    AdminEntity(name = "Administrador Principal", email = "admin@steamish.com", phone = "+56 9 8877 6655", password = "Admin123!", role = "SUPER_ADMIN"),
                                    AdminEntity(name = "Gerente de Juegos", email = "manager@steamish.com", phone = "+56 9 7766 5544", password = "Manager456@", role = "GAME_MANAGER"),
                                    AdminEntity(name = "Soporte Técnico", email = "support@steamish.com", phone = "+56 9 6655 4433", password = "Support789#", role = "SUPPORT")
                                )

                                val adminCount = adminDao.count()
                                Log.d("AppDatabase", "Admin count before seed: $adminCount")
                                if (adminCount == 0) {
                                    Log.d("AppDatabase", "Seeding admins...")
                                    adminSeed.forEach { adminDao.insert(it) }
                                } else {
                                    // Este es el caso problemático: si la tabla no está vacía, no se insertan los admins.
                                    // Esto puede pasar si la tabla de usuarios se crea pero la de admins no, o viceversa.
                                    // Forzamos la inserción del admin principal si no existe para asegurar que podamos entrar.
                                    val adminExists = adminDao.getByEmail("admin@steamish.com") != null
                                    if (!adminExists) {
                                        Log.d("AppDatabase", "Main admin not found, seeding it individually.")
                                        adminDao.insert(adminSeed[0])
                                    }
                                }

                                // Primero, precargamos categorías
                                val categoriaDao = getInstance(context).categoriaDao()
                                val categoriaCount = categoriaDao.count()
                                Log.d("AppDatabase", "📁 Categorías actuales en BD: $categoriaCount")
                                if (categoriaCount == 0) {
                                    Log.d("AppDatabase", "Seeding categorías...")
                                    val categoriasSeed = listOf(
                                        CategoriaEntity(nombre = "Acción", descripcion = "Juegos de alta intensidad y combate"),
                                        CategoriaEntity(nombre = "Aventura", descripcion = "Exploración y narrativa inmersiva"),
                                        CategoriaEntity(nombre = "RPG", descripcion = "Juegos de rol y desarrollo de personajes"),
                                        CategoriaEntity(nombre = "Deportes", descripcion = "Simulaciones deportivas"),
                                        CategoriaEntity(nombre = "Estrategia", descripcion = "Planificación y táctica")
                                    )
                                    categoriasSeed.forEachIndexed { index, categoria ->
                                        val id = categoriaDao.insert(categoria)
                                        Log.d("AppDatabase", "  [$index] ${categoria.nombre} -> ID: $id")
                                    }
                                }

                                // Luego, precargamos géneros
                                val generoDao = getInstance(context).generoDao()
                                val generoCount = generoDao.count()
                                Log.d("AppDatabase", "🎯 Géneros actuales en BD: $generoCount")
                                if (generoCount == 0) {
                                    Log.d("AppDatabase", "Seeding géneros...")
                                    val generosSeed = listOf(
                                        GeneroEntity(nombre = "Plataformas", descripcion = "Juegos de salto y plataformas"),
                                        GeneroEntity(nombre = "Shooter", descripcion = "Juegos de disparos"),
                                        GeneroEntity(nombre = "Racing", descripcion = "Carreras y velocidad"),
                                        GeneroEntity(nombre = "Puzzle", descripcion = "Rompecabezas y lógica"),
                                        GeneroEntity(nombre = "MMORPG", descripcion = "Juegos masivos en línea")
                                    )
                                    generosSeed.forEachIndexed { index, genero ->
                                        val id = generoDao.insert(genero)
                                        Log.d("AppDatabase", "  [$index] ${genero.nombre} -> ID: $id")
                                    }
                                }

                                // Finalmente, precargamos catálogo completo de juegos con imágenes por defecto
                                val juegoDao = getInstance(context).juegoDao()
                                val currentCount = juegoDao.count()
                                Log.d("AppDatabase", "🎮 Juegos actuales en BD: $currentCount")
                                
                                // Si hay datos incompletos (menos de 20 juegos), limpiamos y reiniciamos
                                if (currentCount > 0 && currentCount < 20) {
                                    Log.w("AppDatabase", "🧹 Datos incompletos detectados ($currentCount juegos), limpiando BD...")
                                    juegoDao.deleteAll()
                                    Log.d("AppDatabase", "🧹 Juegos eliminados, reiniciando seeding...")
                                }
                                
                                val finalCurrentCount = juegoDao.count()
                                if (finalCurrentCount == 0) {
                                    Log.d("AppDatabase", "Seeding games...")
                                    val juegosSeed = listOf(
                                        JuegoEntity(nombre = "Super Mario Bros",            precio = 29.99, imagenUrl = "",            descripcion = "El clásico juego de plataformas",     stock = 15,  desarrollador = "Nintendo",        fechaLanzamiento = "1985", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "The Legend of Zelda",         precio = 39.99, imagenUrl = "",         descripcion = "Épica aventura en Hyrule",            stock = 8,   desarrollador = "Nintendo",        fechaLanzamiento = "1986", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Pokémon Red",                 precio = 24.99, imagenUrl = "",                 descripcion = "Conviértete en maestro Pokémon",      stock = 20,  desarrollador = "Game Freak",      fechaLanzamiento = "1996", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Sonic the Hedgehog",          precio = 19.99, imagenUrl = "",          descripcion = "Velocidad supersónica",               stock = 12,  desarrollador = "Sega",            fechaLanzamiento = "1991", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Final Fantasy VII",           precio = 49.99, imagenUrl = "",           descripcion = "RPG épico de Square Enix",            stock = 5,   desarrollador = "Square Enix",     fechaLanzamiento = "1997", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Street Fighter II",           precio = 14.99, imagenUrl = "",           descripcion = "El mejor juego de lucha",             stock = 10,  desarrollador = "Capcom",          fechaLanzamiento = "1991", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Minecraft",                   precio = 26.99, imagenUrl = "",                   descripcion = "Construye tu mundo",                  stock = 25,  desarrollador = "Mojang",          fechaLanzamiento = "2011", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Call of Duty Modern Warfare", precio = 59.99, imagenUrl = "",          descripcion = "Acción militar intensa",              stock = 7,   desarrollador = "Infinity Ward",   fechaLanzamiento = "2019", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "FIFA 24",                     precio = 69.99, imagenUrl = "",                     descripcion = "El mejor fútbol virtual",             stock = 18,  desarrollador = "EA Sports",       fechaLanzamiento = "2023", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "The Witcher 3 Wild Hunt",     precio = 39.99, imagenUrl = "",                   descripcion = "Aventura de Geralt de Rivia",         stock = 6,   desarrollador = "CD Projekt RED",  fechaLanzamiento = "2015", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Cyberpunk 2077",              precio = 59.99, imagenUrl = "",              descripcion = "Futuro cyberpunk",                    stock = 9,   desarrollador = "CD Projekt RED",  fechaLanzamiento = "2020", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Red Dead Redemption 2",       precio = 49.99, imagenUrl = "",       descripcion = "Western épico",                       stock = 11,  desarrollador = "Rockstar Games",  fechaLanzamiento = "2018", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Dark Souls III",              precio = 39.99, imagenUrl = "",              descripcion = "Desafío extremo",                     stock = 8,   desarrollador = "FromSoftware",    fechaLanzamiento = "2016", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Grand Theft Auto V",          precio = 29.99, imagenUrl = "",                       descripcion = "Mundo abierto épico",                 stock = 22,  desarrollador = "Rockstar Games",  fechaLanzamiento = "2013", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Elden Ring",                  precio = 59.99, imagenUrl = "",                  descripcion = "Obra maestra de FromSoftware",        stock = 10,  desarrollador = "FromSoftware",    fechaLanzamiento = "2022", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Overwatch 2",                 precio = 39.99, imagenUrl = "",                 descripcion = "Shooter por equipos",                 stock = 14,  desarrollador = "Blizzard",        fechaLanzamiento = "2022", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Among Us",                    precio = 4.99,  imagenUrl = "",                    descripcion = "Encuentra al impostor",               stock = 30,  desarrollador = "InnerSloth",      fechaLanzamiento = "2018", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Valorant",                    precio = 19.99, imagenUrl = "",                    descripcion = "Shooter táctico",                     stock = 100, desarrollador = "Riot Games",      fechaLanzamiento = "2020", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Assassin's Creed Valhalla",   precio = 59.99, imagenUrl = "",   descripcion = "Aventura vikinga",                    stock = 13,  desarrollador = "Ubisoft",         fechaLanzamiento = "2020", categoriaId = 1, generoId = 1),
                                        JuegoEntity(nombre = "Fortnite",                    precio = 0.0,   imagenUrl = "",                    descripcion = "Battle Royale",                       stock = 100, desarrollador = "Epic Games",      fechaLanzamiento = "2017", categoriaId = 1, generoId = 1)
                                    )
                                    Log.d("AppDatabase", "🎮 Insertando ${juegosSeed.size} juegos...")
                                    var successCount = 0
                                    juegosSeed.forEachIndexed { index, juego ->
                                        try {
                                            val id = juegoDao.insert(juego)
                                            Log.d("AppDatabase", "  ✅ [$index] ${juego.nombre} -> ID: $id")
                                            successCount++
                                        } catch (e: Exception) {
                                            Log.e("AppDatabase", "  ❌ [$index] Error insertando ${juego.nombre}: ${e.message}")
                                        }
                                    }
                                    val finalCount = juegoDao.count()
                                    Log.d("AppDatabase", "✅ Insertados: $finalCount/$successCount juegos en total")
                                    
                                    // Verificación adicional
                                    if (finalCount < juegosSeed.size) {
                                        Log.w("AppDatabase", "⚠️ Solo se insertaron $finalCount de ${juegosSeed.size} juegos")
                                        Log.w("AppDatabase", "⚠️ Verificando foreign keys...")
                                        Log.w("AppDatabase", "⚠️ Categorías disponibles: ${categoriaDao.count()}")
                                        Log.w("AppDatabase", "⚠️ Géneros disponibles: ${generoDao.count()}")
                                    }
                                } else {
                                    Log.d("AppDatabase", "⚠️ BD ya tiene $currentCount juegos, omitiendo seed")
                                }

                                // Precargamos algunas órdenes para las estadísticas
                                val ordenDao = getInstance(context).ordenCompraDao()
                                if (ordenDao.count() == 0) {
                                    Log.d("AppDatabase", "Seeding orders...")
                                    val ordenesSeed = listOf(
                                        OrdenCompraEntity(
                                            userId = 1, 
                                            total = 39.99, 
                                            fechaOrden = "2024-10-01", 
                                            estadoId = 1,
                                            metodoPago = "Tarjeta de Crédito"
                                        ),
                                        OrdenCompraEntity(
                                            userId = 2, 
                                            total = 59.99, 
                                            fechaOrden = "2024-10-02", 
                                            estadoId = 1,
                                            metodoPago = "PayPal"
                                        ),
                                        OrdenCompraEntity(
                                            userId = 1, 
                                            total = 109.98, 
                                            fechaOrden = "2024-10-03", 
                                            estadoId = 1,
                                            metodoPago = "Tarjeta de Débito"
                                        )
                                    )
                                    ordenesSeed.forEach { ordenDao.insert(it) }
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