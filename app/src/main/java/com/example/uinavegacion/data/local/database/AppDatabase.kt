package com.example.uinavegacion.data.local.database

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking
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
import kotlinx.coroutines.runBlocking


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
    version = 23, // Corregir esquema de juegos
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
        
        // Función helper para obtener la lista de juegos seed
        private fun getJuegosSeed(): List<JuegoEntity> {
            return listOf(
                // Categoría 1: Acción (2 juegos)
                JuegoEntity(
                    nombre = "Super Mario Bros", 
                    precio = 29.99, 
                    imagenUrl = "", 
                    descripcion = "Únete a Mario en su aventura épica para rescatar a la Princesa Peach. Este clásico juego de plataformas de Nintendo ofrece horas de diversión con sus niveles desafiantes, enemigos icónicos y mecánicas de juego atemporales.", 
                    stock = 15, 
                    desarrollador = "Nintendo", 
                    fechaLanzamiento = "1985", 
                    categoriaId = 1, 
                    generoId = 1,
                    descuento = 25
                ),
                JuegoEntity(
                    nombre = "Call of Duty Modern Warfare", 
                    precio = 59.99, 
                    imagenUrl = "", 
                    descripcion = "Sumérgete en un intenso campo de batalla moderno con gráficos de última generación. Experimenta una campaña cinematográfica y un multijugador competitivo que define la acción militar de primera persona.", 
                    stock = 7, 
                    desarrollador = "Infinity Ward", 
                    fechaLanzamiento = "2019", 
                    categoriaId = 1, 
                    generoId = 2,
                    descuento = 0
                ),
                // Categoría 2: Aventura (2 juegos)
                JuegoEntity(
                    nombre = "The Legend of Zelda", 
                    precio = 39.99, 
                    imagenUrl = "", 
                    descripcion = "Embárcate en una épica aventura en el reino de Hyrule. Explora vastos territorios, resuelve puzzles ingeniosos y lucha contra poderosos enemigos mientras descubres la historia de Link y la Princesa Zelda.", 
                    stock = 8, 
                    desarrollador = "Nintendo", 
                    fechaLanzamiento = "1986", 
                    categoriaId = 2, 
                    generoId = 2,
                    descuento = 30
                ),
                JuegoEntity(
                    nombre = "Red Dead Redemption 2", 
                    precio = 49.99, 
                    imagenUrl = "", 
                    descripcion = "Vive la experiencia del Lejano Oeste en este western épico. Únete a la banda de forajidos de Arthur Morgan mientras atraviesas un mundo abierto increíblemente detallado lleno de acción, drama y decisiones morales.", 
                    stock = 11, 
                    desarrollador = "Rockstar Games", 
                    fechaLanzamiento = "2018", 
                    categoriaId = 2, 
                    generoId = 2,
                    descuento = 0
                ),
                // Categoría 3: RPG (2 juegos)
                JuegoEntity(
                    nombre = "Final Fantasy VII", 
                    precio = 49.99, 
                    imagenUrl = "", 
                    descripcion = "Sumérgete en el mundo de Midgar con Cloud Strife y sus aliados en este RPG épico. Combate por turnos estratégico, desarrollo profundo de personajes y una historia memorable hacen de este uno de los mejores RPGs de todos los tiempos.", 
                    stock = 5, 
                    desarrollador = "Square Enix", 
                    fechaLanzamiento = "1997", 
                    categoriaId = 3, 
                    generoId = 3,
                    descuento = 20
                ),
                JuegoEntity(
                    nombre = "The Witcher 3 Wild Hunt", 
                    precio = 39.99, 
                    imagenUrl = "", 
                    descripcion = "Acompaña a Geralt de Rivia, el legendario cazador de monstruos, en una aventura de mundo abierto llena de decisiones que moldean el destino. Con combate dinámico, misiones envolventes y un mundo rico en detalles.", 
                    stock = 6, 
                    desarrollador = "CD Projekt RED", 
                    fechaLanzamiento = "2015", 
                    categoriaId = 3, 
                    generoId = 3,
                    descuento = 0
                ),
                // Categoría 4: Deportes (2 juegos)
                JuegoEntity(
                    nombre = "FIFA 24", 
                    precio = 69.99, 
                    imagenUrl = "", 
                    descripcion = "El simulador de fútbol más realista del mercado. Disfruta de gráficos mejorados, física avanzada y todas las ligas y equipos oficiales. Vive la emoción del fútbol desde la comodidad de tu hogar.", 
                    stock = 18, 
                    desarrollador = "EA Sports", 
                    fechaLanzamiento = "2023", 
                    categoriaId = 4, 
                    generoId = 4,
                    descuento = 15
                ),
                JuegoEntity(
                    nombre = "NBA 2K24", 
                    precio = 59.99, 
                    imagenUrl = "", 
                    descripcion = "Experimenta el baloncesto profesional con la simulación más auténtica. Controla a los mejores jugadores de la NBA, juega en el modo MyCareer o compite en línea con jugadores de todo el mundo.", 
                    stock = 12, 
                    desarrollador = "Visual Concepts", 
                    fechaLanzamiento = "2023", 
                    categoriaId = 4, 
                    generoId = 4,
                    descuento = 0
                ),
                // Categoría 5: Estrategia (2 juegos)
                JuegoEntity(
                    nombre = "Civilization VI", 
                    precio = 39.99, 
                    imagenUrl = "", 
                    descripcion = "Construye y lidera tu civilización desde la antigüedad hasta la era moderna. Toma decisiones estratégicas, expande tu imperio, investiga tecnologías y compite con otros líderes para dominar el mundo.", 
                    stock = 10, 
                    desarrollador = "Firaxis", 
                    fechaLanzamiento = "2016", 
                    categoriaId = 5, 
                    generoId = 5,
                    descuento = 35
                ),
                JuegoEntity(
                    nombre = "Age of Empires IV", 
                    precio = 49.99, 
                    imagenUrl = "", 
                    descripcion = "Estrategia en tiempo real que te lleva a través de las épocas históricas. Construye ciudades, forma ejércitos y conquista a tus enemigos en batallas épicas con gráficos modernos y mecánicas mejoradas.", 
                    stock = 8, 
                    desarrollador = "Relic", 
                    fechaLanzamiento = "2021", 
                    categoriaId = 5, 
                    generoId = 5,
                    descuento = 0
                )
            )
        }
        
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

        // Migración de versión 16 a 17: Actualizar biblioteca para usuarios específicos
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 16->17: Actualizando tabla biblioteca para usuarios...")
                
                // Crear tabla temporal con nueva estructura
                database.execSQL(
                    """
                    CREATE TABLE biblioteca_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        juegoId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        price REAL NOT NULL,
                        dateAdded TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'Disponible',
                        genre TEXT NOT NULL DEFAULT 'Acción',
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """
                )
                
                // Crear índice para userId
                database.execSQL("CREATE INDEX index_biblioteca_userId ON biblioteca_new(userId)")
                
                // Eliminar tabla antigua y renombrar nueva
                database.execSQL("DROP TABLE IF EXISTS biblioteca")
                database.execSQL("ALTER TABLE biblioteca_new RENAME TO biblioteca")
                
                Log.d("AppDatabase", "MIGRATION 16->17: Biblioteca actualizada para usuarios específicos")
            }
        }
        
        // Migración de versión 17 a 18: Agregar columna gender a users
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 17->18: Agregando columna gender a users...")
                // Agregar columna gender con valor por defecto vacío
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN gender TEXT NOT NULL DEFAULT ''"
                )
                Log.d("AppDatabase", "MIGRATION 17->18: Columna gender agregada correctamente")
            }
        }
        
        // Migración de versión 18 a 19: Agregar columna activo a juegos
        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 18->19: Agregando columna activo a juegos...")
                // Agregar columna activo con valor por defecto 1 (true)
                database.execSQL(
                    "ALTER TABLE juegos ADD COLUMN activo INTEGER NOT NULL DEFAULT 1"
                )
                Log.d("AppDatabase", "MIGRATION 18->19: Columna activo agregada correctamente")
            }
        }
        
        // Migración de versión 19 a 20: Agregar columna profilePhotoUri a admins
        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 19->20: Agregando columna profilePhotoUri a admins...")
                try {
                    // Agregar columna profilePhotoUri (nullable)
                    database.execSQL(
                        "ALTER TABLE admins ADD COLUMN profilePhotoUri TEXT"
                    )
                    Log.d("AppDatabase", "MIGRATION 19->20: Columna profilePhotoUri agregada correctamente")
                } catch (e: SQLiteException) {
                    // Si la columna ya existe, ignorar el error
                    if (e.message?.contains("duplicate column name") == true) {
                        Log.d("AppDatabase", "MIGRATION 19->20: La columna profilePhotoUri ya existe, omitiendo...")
                    } else {
                        // Si es otro error, relanzarlo
                        throw e
                    }
                }
            }
        }
        
        // Migración de versión 20 a 21: Agregar columna descuento a juegos
        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 20->21: Agregando columna descuento a juegos...")
                // Agregar columna descuento con valor por defecto 0
                database.execSQL(
                    "ALTER TABLE juegos ADD COLUMN descuento INTEGER NOT NULL DEFAULT 0"
                )
                Log.d("AppDatabase", "MIGRATION 20->21: Columna descuento agregada correctamente")
            }
        }
        
        // Migración de versión 21 a 22: Forzar recreación de juegos con descuentos
        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 21->22: Forzando recreación de juegos...")
                // Eliminar todos los juegos existentes para forzar re-seeding
                database.execSQL("DELETE FROM juegos")
                Log.d("AppDatabase", "MIGRATION 21->22: Juegos eliminados, se insertarán en onCreate")
            }
        }
        
        // Migración de versión 22 a 23: Corregir esquema de tabla juegos
        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 22->23: Corrigiendo esquema de tabla juegos...")
                try {
                    // Verificar si la tabla existe
                    val cursor = database.query(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name='juegos'"
                    )
                    val tableExists = cursor.count > 0
                    cursor.close()
                    
                    if (tableExists) {
                        // Crear tabla temporal con el esquema correcto
                        database.execSQL(
                            """
                            CREATE TABLE juegos_new (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                nombre TEXT NOT NULL,
                                descripcion TEXT NOT NULL,
                                precio REAL NOT NULL,
                                stock INTEGER NOT NULL,
                                imagenUrl TEXT,
                                desarrollador TEXT NOT NULL DEFAULT 'Desarrollador',
                                fechaLanzamiento TEXT NOT NULL DEFAULT '2024',
                                categoriaId INTEGER NOT NULL DEFAULT 1,
                                generoId INTEGER NOT NULL DEFAULT 1,
                                activo INTEGER NOT NULL DEFAULT 1,
                                descuento INTEGER NOT NULL DEFAULT 0,
                                FOREIGN KEY(categoriaId) REFERENCES categorias(id) ON DELETE CASCADE,
                                FOREIGN KEY(generoId) REFERENCES generos(id) ON DELETE CASCADE
                            )
                            """
                        )
                        
                        // Intentar copiar datos existentes
                        try {
                            database.execSQL(
                                """
                                INSERT INTO juegos_new (id, nombre, descripcion, precio, stock, imagenUrl, desarrollador, fechaLanzamiento, categoriaId, generoId, activo, descuento)
                                SELECT 
                                    id,
                                    nombre,
                                    COALESCE(descripcion, '') as descripcion,
                                    precio,
                                    COALESCE(stock, 0) as stock,
                                    imagenUrl,
                                    COALESCE(desarrollador, 'Desarrollador') as desarrollador,
                                    COALESCE(fechaLanzamiento, '2024') as fechaLanzamiento,
                                    COALESCE(categoriaId, 1) as categoriaId,
                                    COALESCE(generoId, 1) as generoId,
                                    COALESCE(activo, 1) as activo,
                                    COALESCE(descuento, 0) as descuento
                                FROM juegos
                                """
                            )
                            Log.d("AppDatabase", "MIGRATION 22->23: Datos copiados correctamente")
                        } catch (e: SQLiteException) {
                            Log.w("AppDatabase", "MIGRATION 22->23: No se pudieron copiar datos (esquema incompatible), continuando sin datos: ${e.message}")
                        }
                        
                        // Eliminar tabla antigua
                        database.execSQL("DROP TABLE juegos")
                        
                        // Renombrar tabla nueva
                        database.execSQL("ALTER TABLE juegos_new RENAME TO juegos")
                    } else {
                        // Si la tabla no existe, crearla directamente
                        database.execSQL(
                            """
                            CREATE TABLE juegos (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                nombre TEXT NOT NULL,
                                descripcion TEXT NOT NULL,
                                precio REAL NOT NULL,
                                stock INTEGER NOT NULL,
                                imagenUrl TEXT,
                                desarrollador TEXT NOT NULL DEFAULT 'Desarrollador',
                                fechaLanzamiento TEXT NOT NULL DEFAULT '2024',
                                categoriaId INTEGER NOT NULL DEFAULT 1,
                                generoId INTEGER NOT NULL DEFAULT 1,
                                activo INTEGER NOT NULL DEFAULT 1,
                                descuento INTEGER NOT NULL DEFAULT 0,
                                FOREIGN KEY(categoriaId) REFERENCES categorias(id) ON DELETE CASCADE,
                                FOREIGN KEY(generoId) REFERENCES generos(id) ON DELETE CASCADE
                            )
                            """
                        )
                        Log.d("AppDatabase", "MIGRATION 22->23: Tabla juegos creada (no existía previamente)")
                    }
                    
                    // Recrear índices
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_juegos_categoriaId ON juegos(categoriaId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_juegos_generoId ON juegos(generoId)")
                    
                    Log.d("AppDatabase", "MIGRATION 22->23: Esquema de juegos corregido correctamente")
                } catch (e: SQLiteException) {
                    Log.e("AppDatabase", "MIGRATION 22->23: Error corrigiendo esquema: ${e.message}", e)
                    // Si falla, intentar solo eliminar y recrear (se perderán datos pero se reinsertarán en onCreate)
                    try {
                        database.execSQL("DROP TABLE IF EXISTS juegos")
                        Log.d("AppDatabase", "MIGRATION 22->23: Tabla juegos eliminada, se recreará en onCreate")
                    } catch (e2: SQLiteException) {
                        Log.e("AppDatabase", "MIGRATION 22->23: Error eliminando tabla: ${e2.message}", e2)
                        throw e2
                    }
                }
            }
        }

        @Volatile
        private var seedingContext: Context? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                seedingContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23)
                    .fallbackToDestructiveMigration() // Permite recrear la BD si hay problemas de migración
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "onCreate CALLED. Seeding data...")
                            // Seeding completo solo en onCreate (cuando se crea la BD)
                            runBlocking(Dispatchers.IO) {
                                val ctx = seedingContext ?: return@runBlocking
                                val dbInstance = INSTANCE ?: return@runBlocking
                                val userDao = dbInstance.userDao()
                                val adminDao = dbInstance.adminDao()

                                // Precargamos usuarios con más datos de prueba
                                val userSeed = listOf(
                                    UserEntity(name = "Usuario Demo", email = "user1@demo.com", phone = "+56 9 1234 5678", password = "Password123!", gender = "Masculino"),
                                    UserEntity(name = "Usuario Test", email = "test@test.com", phone = "+56 9 8765 4321", password = "Password123!", gender = "Femenino"),
                                    UserEntity(name = "María González", email = "maria.gonzalez@email.com", phone = "+56 9 1111 2222", password = "Password123!", gender = "Femenino"),
                                    UserEntity(name = "Carlos Ramírez", email = "carlos.ramirez@email.com", phone = "+56 9 3333 4444", password = "Password123!", gender = "Masculino"),
                                    UserEntity(name = "Ana Martínez", email = "ana.martinez@email.com", phone = "+56 9 5555 6666", password = "Password123!", gender = "Femenino"),
                                    UserEntity(name = "Luis Fernández", email = "luis.fernandez@email.com", phone = "+56 9 7777 8888", password = "Password123!", gender = "Masculino"),
                                    UserEntity(name = "Sofía López", email = "sofia.lopez@email.com", phone = "+56 9 9999 0000", password = "Password123!", gender = "Femenino"),
                                    UserEntity(name = "Diego Torres", email = "diego.torres@email.com", phone = "+56 9 2222 3333", password = "Password123!", gender = "Masculino")
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
                                val categoriaDao = dbInstance.categoriaDao()
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
                                val generoDao = dbInstance.generoDao()
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

                                // Finalmente, precargamos exactamente 10 juegos (2 por cada categoría)
                                val juegoDao = dbInstance.juegoDao()
                                val currentCountAll = juegoDao.countAll()
                                val currentCountActive = juegoDao.count()
                                Log.d("AppDatabase", "🎮 Juegos totales en BD (activos + inactivos): $currentCountAll")
                                Log.d("AppDatabase", "🎮 Juegos activos en BD: $currentCountActive")
                                
                                // Siempre insertar los 10 juegos si no existen exactamente 10 activos
                                if (currentCountActive != 10) {
                                    if (currentCountAll > 0) {
                                        Log.w("AppDatabase", "🧹 Limpiando juegos existentes para insertar los 10 correctos...")
                                        juegoDao.deleteAll()
                                    }
                                    
                                    Log.d("AppDatabase", "Seeding games... (10 juegos totales, 2 por categoría)")
                                    val juegosSeed = getJuegosSeed()
                                    Log.d("AppDatabase", "🎮 Insertando ${juegosSeed.size} juegos...")
                                    var successCount = 0
                                    juegosSeed.forEachIndexed { index, juego ->
                                        try {
                                            val id = juegoDao.insert(juego)
                                            Log.d("AppDatabase", "  ✅ [$index] ${juego.nombre} -> ID: $id")
                                            successCount++
                                        } catch (e: Exception) {
                                            Log.e("AppDatabase", "  ❌ [$index] Error insertando ${juego.nombre}: ${e.message}", e)
                                        }
                                    }
                                    val finalCount = juegoDao.countAll()
                                    val finalActive = juegoDao.count()
                                    Log.d("AppDatabase", "✅ Insertados: $finalCount/$successCount juegos en total (activos: $finalActive)")
                                    
                                    // Verificación final
                                    if (finalCount != 10 || finalActive != 10) {
                                        Log.w("AppDatabase", "⚠️ Se insertaron $finalCount juegos (activos: $finalActive), se esperaban 10")
                                        Log.w("AppDatabase", "⚠️ Categorías disponibles: ${categoriaDao.count()}")
                                        Log.w("AppDatabase", "⚠️ Géneros disponibles: ${generoDao.count()}")
                                    } else {
                                        Log.d("AppDatabase", "✅ Se insertaron correctamente los 10 juegos (2 por categoría), todos activos")
                                    }
                                } else {
                                    Log.d("AppDatabase", "✅ BD ya tiene los 10 juegos requeridos (todos activos), omitiendo seed")
                                }

                                // Precargamos algunas órdenes para las estadísticas
                                val ordenDao = dbInstance.ordenCompraDao()
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
                                
                                Log.d("AppDatabase", "🏁 ¡SEEDING COMPLETADO! Base de datos inicializada correctamente")
                            }
                        }
                        
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("AppDatabase", "onOpen CALLED. Verificación rápida...")
                            // Verificación rápida y asíncrona (sin bloquear)
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val dbInstance = INSTANCE ?: return@launch
                                    val juegoDao = dbInstance.juegoDao()
                                    val activeCount = juegoDao.count()
                                    
                                    // Solo si faltan juegos, insertarlos en background (no bloquea el login)
                                    if (activeCount < 10) {
                                        Log.w("AppDatabase", "⚠️ onOpen: Solo $activeCount juegos activos, insertando en background...")
                                        val currentCountAll = juegoDao.countAll()
                                        if (currentCountAll > 0 && currentCountAll < 10) {
                                            juegoDao.deleteAll()
                                        }
                                        val juegosSeed = getJuegosSeed()
                                        juegosSeed.forEach { juego ->
                                            try {
                                                juegoDao.insert(juego)
                                            } catch (e: Exception) {
                                                Log.e("AppDatabase", "Error insertando ${juego.nombre}: ${e.message}")
                                            }
                                        }
                                        val afterSeed = juegoDao.count()
                                        Log.d("AppDatabase", "✅ onOpen: Background seeding completado, $afterSeed juegos activos")
                                    }
                                } catch (e: Exception) {
                                    Log.e("AppDatabase", "❌ Error en onOpen: ${e.message}", e)
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