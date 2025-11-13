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
import com.example.uinavegacion.data.local.resena.ResenaDao
import com.example.uinavegacion.data.local.resena.ResenaEntity
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
        ReservaEntity::class,
        ResenaEntity::class,
        com.example.uinavegacion.data.local.library.LibraryEntity::class
    ],
    version = 26, // Forzar recreación de tabla resenas
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
    abstract fun resenaDao(): ResenaDao
    abstract fun libraryDao(): com.example.uinavegacion.data.local.library.LibraryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "ui_navegacion.db"
        
        // Función helper para obtener la lista de juegos seed (todos para PC)
        private fun getJuegosSeed(): List<JuegoEntity> {
            return listOf(
                // Categoría 1: Acción (2 juegos para PC)
                JuegoEntity(
                    nombre = "Doom Eternal", 
                    precio = 59.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. El infierno ha invadido la Tierra. Conviértete en el Slayer y destruye demonios con un arsenal devastador. Combate rápido y brutal, gráficos impresionantes y una banda sonora épica. La experiencia definitiva de acción en primera persona para PC.", 
                    stock = 12, 
                    desarrollador = "id Software", 
                    fechaLanzamiento = "2020", 
                    categoriaId = 1, 
                    generoId = 2,
                    descuento = 20
                ),
                JuegoEntity(
                    nombre = "Counter-Strike 2 - Prime", 
                    precio = 14.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. Licencia Prime de Counter-Strike 2. El shooter táctico más competitivo del mundo con acceso a servidores Prime, matchmaking mejorado, drops de cajas exclusivas y protección contra cheaters. Combate 5v5 por rondas con mecánicas de precisión milimétrica. Gráficos mejorados con Source 2 y servidores de 128-tick.", 
                    stock = 50, 
                    desarrollador = "Valve", 
                    fechaLanzamiento = "2023", 
                    categoriaId = 1, 
                    generoId = 2,
                    descuento = 0
                ),
                // Categoría 2: Aventura (2 juegos para PC)
                JuegoEntity(
                    nombre = "The Witcher 3: Wild Hunt", 
                    precio = 39.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. Acompaña a Geralt de Rivia en una aventura épica de mundo abierto. Explora un continente masivo, toma decisiones que moldean el destino y lucha contra monstruos en combate dinámico. La experiencia definitiva de RPG de acción para PC con mods y gráficos mejorados.", 
                    stock = 15, 
                    desarrollador = "CD Projekt RED", 
                    fechaLanzamiento = "2015", 
                    categoriaId = 2, 
                    generoId = 2,
                    descuento = 30
                ),
                JuegoEntity(
                    nombre = "Cyberpunk 2077", 
                    precio = 49.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. Sumérgete en Night City, una metrópolis futurista llena de peligro y oportunidades. Personaliza tu personaje, elige tu estilo de juego y vive una historia cinematográfica con decisiones que importan. Optimizado para PC con ray tracing y gráficos de última generación.", 
                    stock = 10, 
                    desarrollador = "CD Projekt RED", 
                    fechaLanzamiento = "2020", 
                    categoriaId = 2, 
                    generoId = 2,
                    descuento = 25
                ),
                // Categoría 3: RPG (2 juegos para PC)
                JuegoEntity(
                    nombre = "Baldur's Gate 3", 
                    precio = 59.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. El RPG definitivo con combate por turnos basado en D&D 5ª edición. Explora un mundo rico, forma tu propio grupo de aventureros y toma decisiones que cambian el curso de la historia. Múltiples finales, romances y más de 174 horas de contenido. Experiencia completa para PC.", 
                    stock = 8, 
                    desarrollador = "Larian Studios", 
                    fechaLanzamiento = "2023", 
                    categoriaId = 3, 
                    generoId = 3,
                    descuento = 0
                ),
                JuegoEntity(
                    nombre = "Divinity: Original Sin 2", 
                    precio = 44.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. Un RPG táctico de mundo abierto con combate estratégico por turnos. Crea tu propio héroe, forma un grupo de hasta 4 personajes y explora Rivellon. Sistema de combate innovador, narrativa profunda y mods de la comunidad. La experiencia RPG definitiva para PC.", 
                    stock = 11, 
                    desarrollador = "Larian Studios", 
                    fechaLanzamiento = "2017", 
                    categoriaId = 3, 
                    generoId = 3,
                    descuento = 15
                ),
                // Categoría 4: Deportes (2 juegos para PC)
                JuegoEntity(
                    nombre = "FIFA 26 - Gold Edition", 
                    precio = 89.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. Edición Gold de FIFA 26. La experiencia de fútbol más completa y realista. Incluye el juego base, Ultimate Team con 4600 FIFA Points, acceso anticipado de 3 días, y contenido exclusivo. Juega con los mejores equipos y jugadores del mundo, incluyendo la UEFA Champions League y la Liga Femenina. Modo Carrera mejorado y gráficos de última generación.", 
                    stock = 18, 
                    desarrollador = "EA Sports", 
                    fechaLanzamiento = "2025", 
                    categoriaId = 4, 
                    generoId = 4,
                    descuento = 15
                ),
                JuegoEntity(
                    nombre = "Football Manager 2024", 
                    precio = 54.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. La simulación de gestión futbolística más profunda y realista. Toma el control de cualquier club, gestiona tácticas, transferencias y desarrollo de jugadores. Base de datos con más de 800,000 jugadores y personal real. La experiencia definitiva de gestión para PC.", 
                    stock = 14, 
                    desarrollador = "Sports Interactive", 
                    fechaLanzamiento = "2023", 
                    categoriaId = 4, 
                    generoId = 4,
                    descuento = 10
                ),
                // Categoría 5: Estrategia (2 juegos para PC)
                JuegoEntity(
                    nombre = "Total War: Warhammer III", 
                    precio = 59.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. Combina la estrategia épica de Total War con el mundo de Warhammer. Comanda ejércitos masivos en batallas tácticas, gestiona imperios y conquista el mundo. Múltiples facciones, campañas épicas y combate espectacular. La experiencia de estrategia definitiva para PC.", 
                    stock = 9, 
                    desarrollador = "Creative Assembly", 
                    fechaLanzamiento = "2022", 
                    categoriaId = 5, 
                    generoId = 5,
                    descuento = 0
                ),
                JuegoEntity(
                    nombre = "Crusader Kings III", 
                    precio = 49.99, 
                    imagenUrl = "", 
                    descripcion = "Plataforma: PC. Gestiona tu dinastía medieval a través de generaciones. Toma decisiones políticas, militares y diplomáticas que moldean la historia. Sistema de personajes complejo, intrigas cortesanas y expansión territorial. El juego de estrategia y simulación más profundo para PC.", 
                    stock = 7, 
                    desarrollador = "Paradox Development Studio", 
                    fechaLanzamiento = "2020", 
                    categoriaId = 5, 
                    generoId = 5,
                    descuento = 20
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
        
        // Migración de versión 25 a 26: Forzar recreación completa de tabla resenas
        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 25->26: Forzando recreación completa de tabla resenas...")
                // Intentar múltiples veces si es necesario
                var success = false
                var attempts = 0
                val maxAttempts = 3
                
                while (!success && attempts < maxAttempts) {
                    try {
                        attempts++
                        Log.d("AppDatabase", "MIGRATION 25->26: Intento $attempts de $maxAttempts")
                        
                        // Eliminar índices
                        try {
                            database.execSQL("DROP INDEX IF EXISTS index_resenas_juegoId")
                        } catch (e: Exception) {
                            Log.d("AppDatabase", "MIGRATION 25->26: Índice juegoId no existe")
                        }
                        try {
                            database.execSQL("DROP INDEX IF EXISTS index_resenas_userId")
                        } catch (e: Exception) {
                            Log.d("AppDatabase", "MIGRATION 25->26: Índice userId no existe")
                        }
                        
                        // Eliminar tabla completamente
                        try {
                            database.execSQL("DROP TABLE IF EXISTS resenas")
                            Log.d("AppDatabase", "MIGRATION 25->26: Tabla resenas eliminada")
                        } catch (e: Exception) {
                            Log.w("AppDatabase", "MIGRATION 25->26: Error eliminando tabla (puede no existir)", e)
                        }
                        
                        // Crear tabla con esquema exacto que Room espera
                        database.execSQL(
                            """
                            CREATE TABLE resenas (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                userId INTEGER NOT NULL,
                                juegoId INTEGER NOT NULL,
                                calificacion INTEGER NOT NULL DEFAULT 1,
                                comentario TEXT NOT NULL,
                                fechaCreacion TEXT NOT NULL,
                                isDeleted INTEGER NOT NULL DEFAULT 0,
                                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                                FOREIGN KEY(juegoId) REFERENCES juegos(id) ON DELETE CASCADE
                            )
                            """
                        )
                        Log.d("AppDatabase", "MIGRATION 25->26: Tabla resenas creada")
                        
                        // Crear índices
                        database.execSQL("CREATE INDEX IF NOT EXISTS index_resenas_juegoId ON resenas(juegoId)")
                        database.execSQL("CREATE INDEX IF NOT EXISTS index_resenas_userId ON resenas(userId)")
                        Log.d("AppDatabase", "MIGRATION 25->26: Índices creados")
                        
                        success = true
                        Log.d("AppDatabase", "MIGRATION 25->26: Tabla resenas recreada correctamente")
                    } catch (e: Exception) {
                        Log.e("AppDatabase", "MIGRATION 25->26: Error en intento $attempts", e)
                        if (attempts >= maxAttempts) {
                            // En el último intento, loguear pero no lanzar excepción para que la app pueda iniciar
                            Log.e("AppDatabase", "MIGRATION 25->26: Todos los intentos fallaron, pero continuando para permitir que la app inicie")
                            // No lanzar excepción - permitir que la app inicie sin la tabla resenas
                            success = true // Marcar como éxito para salir del loop
                        } else {
                            // Esperar un poco antes de reintentar
                            try {
                                Thread.sleep(100)
                            } catch (ie: InterruptedException) {
                                Thread.currentThread().interrupt()
                            }
                        }
                    }
                }
            }
        }
        
        // Migración de versión 24 a 25: Corregir esquema de tabla resenas
        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 24->25: Corrigiendo tabla resenas...")
                try {
                    // Eliminar índices primero
                    try {
                        database.execSQL("DROP INDEX IF EXISTS index_resenas_juegoId")
                    } catch (e: Exception) {
                        Log.d("AppDatabase", "MIGRATION 24->25: Índice juegoId no existe, continuando...")
                    }
                    try {
                        database.execSQL("DROP INDEX IF EXISTS index_resenas_userId")
                    } catch (e: Exception) {
                        Log.d("AppDatabase", "MIGRATION 24->25: Índice userId no existe, continuando...")
                    }
                    
                    // Eliminar la tabla si existe (puede tener esquema incorrecto)
                    try {
                        database.execSQL("DROP TABLE IF EXISTS resenas")
                        Log.d("AppDatabase", "MIGRATION 24->25: Tabla resenas eliminada")
                    } catch (e: Exception) {
                        Log.e("AppDatabase", "MIGRATION 24->25: Error eliminando tabla resenas", e)
                    }
                    
                    // Crear la tabla con el esquema correcto (exactamente como Room lo espera)
                    database.execSQL(
                        """
                        CREATE TABLE resenas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            userId INTEGER NOT NULL,
                            juegoId INTEGER NOT NULL,
                            calificacion INTEGER NOT NULL DEFAULT 1,
                            comentario TEXT NOT NULL,
                            fechaCreacion TEXT NOT NULL,
                            isDeleted INTEGER NOT NULL DEFAULT 0,
                            FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY(juegoId) REFERENCES juegos(id) ON DELETE CASCADE
                        )
                        """
                    )
                    Log.d("AppDatabase", "MIGRATION 24->25: Tabla resenas creada")
                    
                    // Crear índices
                    database.execSQL("CREATE INDEX index_resenas_juegoId ON resenas(juegoId)")
                    database.execSQL("CREATE INDEX index_resenas_userId ON resenas(userId)")
                    Log.d("AppDatabase", "MIGRATION 24->25: Índices creados")
                    
                    Log.d("AppDatabase", "MIGRATION 24->25: Tabla resenas corregida correctamente")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "MIGRATION 24->25: Error crítico corrigiendo tabla resenas", e)
                    throw e
                }
            }
        }
        
        // Migración de versión 23 a 24: Crear tabla de reseñas
        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "MIGRATION 23->24: Creando tabla resenas...")
                try {
                    // Eliminar índices si existen
                    try {
                        database.execSQL("DROP INDEX IF EXISTS index_resenas_juegoId")
                    } catch (e: Exception) {
                        // Ignorar si no existe
                    }
                    try {
                        database.execSQL("DROP INDEX IF EXISTS index_resenas_userId")
                    } catch (e: Exception) {
                        // Ignorar si no existe
                    }
                    
                    // Eliminar la tabla si existe
                    database.execSQL("DROP TABLE IF EXISTS resenas")
                    
                    // Crear la tabla con el esquema correcto
                    database.execSQL(
                        """
                        CREATE TABLE resenas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            userId INTEGER NOT NULL,
                            juegoId INTEGER NOT NULL,
                            calificacion INTEGER NOT NULL DEFAULT 1,
                            comentario TEXT NOT NULL,
                            fechaCreacion TEXT NOT NULL,
                            isDeleted INTEGER NOT NULL DEFAULT 0,
                            FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY(juegoId) REFERENCES juegos(id) ON DELETE CASCADE
                        )
                        """
                    )
                    database.execSQL("CREATE INDEX index_resenas_juegoId ON resenas(juegoId)")
                    database.execSQL("CREATE INDEX index_resenas_userId ON resenas(userId)")
                    Log.d("AppDatabase", "MIGRATION 23->24: Tabla resenas creada correctamente")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "MIGRATION 23->24: Error creando tabla resenas", e)
                    throw e
                }
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
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26)
                    .fallbackToDestructiveMigration() // Permite recrear la BD si hay problemas de migración
                    .fallbackToDestructiveMigrationOnDowngrade() // Permite recrear la BD si hay downgrade
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
                                    AdminEntity(name = "Soporte Técnico", email = "support@steamish.com", phone = "+56 9 6655 4433", password = "Support789#", role = "SUPPORT"),
                                    AdminEntity(name = "Moderador", email = "moderador@steamish.com", phone = "+56 9 5544 3322", password = "Moderador123!", role = "MODERATOR")
                                )

                                val adminCount = adminDao.count()
                                Log.d("AppDatabase", "Admin count before seed: $adminCount")
                                if (adminCount == 0) {
                                    Log.d("AppDatabase", "Seeding admins...")
                                    adminSeed.forEach { adminDao.insert(it) }
                                } else {
                                    // Este es el caso problemático: si la tabla no está vacía, no se insertan los admins.
                                    // Esto puede pasar si la tabla de usuarios se crea pero la de admins no, o viceversa.
                                    // Forzamos la inserción de los admins principales si no existen para asegurar que podamos entrar.
                                    adminSeed.forEach { admin ->
                                        try {
                                            val exists = adminDao.getByEmail(admin.email) != null
                                            if (!exists) {
                                                Log.d("AppDatabase", "Admin ${admin.email} not found, seeding it individually.")
                                                adminDao.insert(admin)
                                            } else {
                                                Log.d("AppDatabase", "Admin ${admin.email} already exists, skipping.")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("AppDatabase", "Error checking/inserting admin ${admin.email}: ${e.message}")
                                        }
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
                                if (currentCountActive < 10) {
                                    if (currentCountAll > 0) {
                                        Log.w("AppDatabase", "🧹 Limpiando juegos existentes para insertar los 10 correctos...")
                                        juegoDao.deleteAll()
                                    }
                                    
                                    Log.d("AppDatabase", "Seeding games... (10 juegos totales, 2 por categoría)")
                                    
                                    // Obtener IDs reales de categorías por nombre
                                    val categoriaAccion = categoriaDao.getByNombre("Acción")
                                    val categoriaAventura = categoriaDao.getByNombre("Aventura")
                                    val categoriaRPG = categoriaDao.getByNombre("RPG")
                                    val categoriaDeportes = categoriaDao.getByNombre("Deportes")
                                    val categoriaEstrategia = categoriaDao.getByNombre("Estrategia")
                                    
                                    val categoriasMap = mapOf(
                                        1L to (categoriaAccion?.id ?: 1L),
                                        2L to (categoriaAventura?.id ?: 2L),
                                        3L to (categoriaRPG?.id ?: 3L),
                                        4L to (categoriaDeportes?.id ?: 4L),
                                        5L to (categoriaEstrategia?.id ?: 5L)
                                    )
                                    
                                    Log.d("AppDatabase", "📁 Mapeo de categorías:")
                                    categoriasMap.forEach { (expectedId, realId) ->
                                        val nombre = when(expectedId) {
                                            1L -> "Acción"
                                            2L -> "Aventura"
                                            3L -> "RPG"
                                            4L -> "Deportes"
                                            5L -> "Estrategia"
                                            else -> "Desconocida"
                                        }
                                        Log.d("AppDatabase", "  $nombre: esperado ID=$expectedId, real ID=$realId")
                                    }
                                    
                                    val juegosSeed = getJuegosSeed()
                                    Log.d("AppDatabase", "🎮 Insertando ${juegosSeed.size} juegos...")
                                    var successCount = 0
                                    juegosSeed.forEachIndexed { index, juego ->
                                        try {
                                            // Obtener el ID real de la categoría
                                            val categoriaIdReal = categoriasMap[juego.categoriaId] ?: juego.categoriaId
                                            
                                            // Crear juego con el ID real de categoría
                                            val juegoConCategoriaCorrecta = juego.copy(
                                                categoriaId = categoriaIdReal,
                                                activo = true
                                            )
                                            
                                            val id = juegoDao.insert(juegoConCategoriaCorrecta)
                                            val categoriaNombre = when(juego.categoriaId) {
                                                1L -> "Acción"
                                                2L -> "Aventura"
                                                3L -> "RPG"
                                                4L -> "Deportes"
                                                5L -> "Estrategia"
                                                else -> "Desconocida"
                                            }
                                            Log.d("AppDatabase", "  ✅ [$index] ${juego.nombre} -> ID: $id, categoriaId: $categoriaIdReal ($categoriaNombre), activo: true")
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
                                        
                                        // Intentar activar todos los juegos si están inactivos
                                        if (finalCount == 10 && finalActive < 10) {
                                            Log.w("AppDatabase", "🔄 Activando juegos inactivos...")
                                            val allJuegos = juegoDao.getAllIncludingInactive()
                                            allJuegos.forEach { juego ->
                                                try {
                                                    juegoDao.reactivate(juego.id)
                                                    Log.d("AppDatabase", "  ✅ Activado: ${juego.nombre}")
                                                } catch (e: Exception) {
                                                    Log.e("AppDatabase", "  ❌ Error activando ${juego.nombre}: ${e.message}")
                                                }
                                            }
                                            val afterActivate = juegoDao.count()
                                            Log.d("AppDatabase", "✅ Después de activar: $afterActivate juegos activos")
                                        }
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
                            Log.d("AppDatabase", "🔓 onOpen CALLED. Verificación y seeding automático...")
                            // Verificación y seeding automático (sin bloquear)
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val dbInstance = INSTANCE ?: return@launch
                                    
                                    // PRIMERO: Asegurar que existan TODOS los admins necesarios
                                    val adminDao = dbInstance.adminDao()
                                    val adminSeed = listOf(
                                        AdminEntity(name = "Administrador Principal", email = "admin@steamish.com", phone = "+56 9 8877 6655", password = "Admin123!", role = "SUPER_ADMIN"),
                                        AdminEntity(name = "Gerente de Juegos", email = "manager@steamish.com", phone = "+56 9 7766 5544", password = "Manager456@", role = "GAME_MANAGER"),
                                        AdminEntity(name = "Soporte Técnico", email = "support@steamish.com", phone = "+56 9 6655 4433", password = "Support789#", role = "SUPPORT"),
                                        AdminEntity(name = "Moderador", email = "moderador@steamish.com", phone = "+56 9 5544 3322", password = "Moderador123!", role = "MODERATOR")
                                    )
                                    
                                    Log.d("AppDatabase", "👮 Verificando y creando admins en onOpen...")
                                    adminSeed.forEach { admin ->
                                        try {
                                            val exists = adminDao.getByEmail(admin.email)
                                            if (exists == null) {
                                                val id = adminDao.insert(admin)
                                                Log.d("AppDatabase", "✅ Admin ${admin.email} creado en onOpen con ID: $id")
                                            } else {
                                                Log.d("AppDatabase", "ℹ️ Admin ${admin.email} ya existe (ID: ${exists.id})")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("AppDatabase", "❌ Error verificando/creando admin ${admin.email}: ${e.message}", e)
                                        }
                                    }
                                    
                                    // SEGUNDO: Asegurar que existan categorías y géneros
                                    val categoriaDao = dbInstance.categoriaDao()
                                    val generoDao = dbInstance.generoDao()
                                    
                                    var categoriaCount = 0
                                    var generoCount = 0
                                    try {
                                        categoriaCount = categoriaDao.count()
                                        generoCount = generoDao.count()
                                    } catch (e: Exception) {
                                        Log.e("AppDatabase", "Error contando categorías/géneros: ${e.message}")
                                    }
                                    
                                    Log.d("AppDatabase", "📊 Estado inicial: $categoriaCount categorías, $generoCount géneros")
                                    
                                    // Crear categorías si faltan
                                    if (categoriaCount < 5) {
                                        Log.w("AppDatabase", "⚠️ Faltan categorías, creando...")
                                        val categoriasSeed = listOf(
                                            CategoriaEntity(nombre = "Acción", descripcion = "Juegos de alta intensidad y combate"),
                                            CategoriaEntity(nombre = "Aventura", descripcion = "Exploración y narrativa inmersiva"),
                                            CategoriaEntity(nombre = "RPG", descripcion = "Juegos de rol y desarrollo de personajes"),
                                            CategoriaEntity(nombre = "Deportes", descripcion = "Simulaciones deportivas"),
                                            CategoriaEntity(nombre = "Estrategia", descripcion = "Planificación y táctica")
                                        )
                                        categoriasSeed.forEach { cat ->
                                            try {
                                                categoriaDao.insert(cat)
                                                Log.d("AppDatabase", "✅ Categoría creada: ${cat.nombre}")
                                            } catch (e: Exception) {
                                                Log.e("AppDatabase", "Error creando categoría ${cat.nombre}: ${e.message}")
                                            }
                                        }
                                    }
                                    
                                    // Crear géneros si faltan
                                    if (generoCount < 5) {
                                        Log.w("AppDatabase", "⚠️ Faltan géneros, creando...")
                                        val generosSeed = listOf(
                                            GeneroEntity(nombre = "Plataformas", descripcion = "Juegos de salto y plataformas"),
                                            GeneroEntity(nombre = "Shooter", descripcion = "Juegos de disparos"),
                                            GeneroEntity(nombre = "Racing", descripcion = "Carreras y velocidad"),
                                            GeneroEntity(nombre = "Puzzle", descripcion = "Rompecabezas y lógica"),
                                            GeneroEntity(nombre = "MMORPG", descripcion = "Juegos masivos en línea")
                                        )
                                        generosSeed.forEach { gen ->
                                            try {
                                                generoDao.insert(gen)
                                                Log.d("AppDatabase", "✅ Género creado: ${gen.nombre}")
                                            } catch (e: Exception) {
                                                Log.e("AppDatabase", "Error creando género ${gen.nombre}: ${e.message}")
                                            }
                                        }
                                    }
                                    
                                    // SEGUNDO: Verificar e insertar juegos
                                    val juegoDao = dbInstance.juegoDao()
                                    val activeCount = juegoDao.count()
                                    val totalCount = juegoDao.countAll()
                                    
                                    Log.d("AppDatabase", "🎮 Juegos: $activeCount activos de $totalCount totales")
                                    
                                    // SIEMPRE insertar si hay menos de 10 juegos activos
                                    if (activeCount < 10) {
                                        Log.w("AppDatabase", "⚠️ onOpen: Solo $activeCount juegos activos, INSERTANDO AUTOMÁTICAMENTE...")
                                        
                                        // Limpiar si hay datos incompletos
                                        if (totalCount > 0 && totalCount < 10) {
                                            Log.w("AppDatabase", "🧹 Limpiando datos incompletos...")
                                            try {
                                                juegoDao.deleteAll()
                                            } catch (e: Exception) {
                                                Log.e("AppDatabase", "Error limpiando: ${e.message}")
                                            }
                                        }
                                        
                                        // Obtener IDs reales de categorías
                                        val categoriaAccion = categoriaDao.getByNombre("Acción")
                                        val categoriaAventura = categoriaDao.getByNombre("Aventura")
                                        val categoriaRPG = categoriaDao.getByNombre("RPG")
                                        val categoriaDeportes = categoriaDao.getByNombre("Deportes")
                                        val categoriaEstrategia = categoriaDao.getByNombre("Estrategia")
                                        
                                        val categoriasMap = mapOf(
                                            1L to (categoriaAccion?.id ?: 1L),
                                            2L to (categoriaAventura?.id ?: 2L),
                                            3L to (categoriaRPG?.id ?: 3L),
                                            4L to (categoriaDeportes?.id ?: 4L),
                                            5L to (categoriaEstrategia?.id ?: 5L)
                                        )
                                        
                                        // Insertar los 10 juegos
                                        val juegosSeed = getJuegosSeed()
                                        Log.d("AppDatabase", "📦 Insertando ${juegosSeed.size} juegos...")
                                        var inserted = 0
                                        var errors = 0
                                        
                                        juegosSeed.forEachIndexed { index, juego ->
                                            try {
                                                // Obtener el ID real de la categoría
                                                val categoriaIdReal = categoriasMap[juego.categoriaId] ?: juego.categoriaId
                                                
                                                // Crear juego con el ID real de categoría
                                                val juegoConCategoriaCorrecta = juego.copy(
                                                    categoriaId = categoriaIdReal,
                                                    activo = true
                                                )
                                                
                                                val id = juegoDao.insert(juegoConCategoriaCorrecta)
                                                inserted++
                                                Log.d("AppDatabase", "  ✅ [$index] ${juego.nombre} -> ID: $id, categoriaId: $categoriaIdReal")
                                            } catch (e: Exception) {
                                                errors++
                                                Log.e("AppDatabase", "  ❌ [$index] Error insertando ${juego.nombre}: ${e.message}", e)
                                            }
                                        }
                                        
                                        val afterSeed = juegoDao.count()
                                        Log.d("AppDatabase", "✅ onOpen: Insertados $inserted juegos (errores: $errors), total activos: $afterSeed")
                                        
                                        // Si aún hay menos de 10, intentar activar los inactivos
                                        if (afterSeed < 10) {
                                            Log.w("AppDatabase", "🔄 Intentando activar juegos inactivos...")
                                            try {
                                                val allJuegos = juegoDao.getAllIncludingInactive()
                                                allJuegos.forEach { juego ->
                                                    try {
                                                        if (!juego.activo) {
                                                            juegoDao.reactivate(juego.id)
                                                            Log.d("AppDatabase", "  ✅ Activado: ${juego.nombre}")
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("AppDatabase", "  ❌ Error activando ${juego.nombre}: ${e.message}")
                                                    }
                                                }
                                                val finalCount = juegoDao.count()
                                                Log.d("AppDatabase", "✅ onOpen: Después de activar, $finalCount juegos activos")
                                            } catch (e: Exception) {
                                                Log.e("AppDatabase", "Error obteniendo juegos inactivos: ${e.message}")
                                            }
                                        }
                                    } else {
                                        Log.d("AppDatabase", "✅ onOpen: Ya hay $activeCount juegos activos")
                                        
                                        // Actualizar juegos existentes que coincidan con nombres antiguos
                                        Log.d("AppDatabase", "🔄 Verificando y actualizando juegos existentes...")
                                        try {
                                            // Obtener IDs reales de categorías (necesario para actualización)
                                            val categoriaAccion = categoriaDao.getByNombre("Acción")
                                            val categoriaAventura = categoriaDao.getByNombre("Aventura")
                                            val categoriaRPG = categoriaDao.getByNombre("RPG")
                                            val categoriaDeportes = categoriaDao.getByNombre("Deportes")
                                            val categoriaEstrategia = categoriaDao.getByNombre("Estrategia")
                                            
                                            val categoriasMap = mapOf(
                                                1L to (categoriaAccion?.id ?: 1L),
                                                2L to (categoriaAventura?.id ?: 2L),
                                                3L to (categoriaRPG?.id ?: 3L),
                                                4L to (categoriaDeportes?.id ?: 4L),
                                                5L to (categoriaEstrategia?.id ?: 5L)
                                            )
                                            
                                            val juegosSeed = getJuegosSeed()
                                            val allJuegos = juegoDao.getAllIncludingInactive()
                                            
                                            // Mapeo de nombres antiguos a nuevos
                                            val updatesMap = mapOf(
                                                "Counter-Strike 2" to juegosSeed.find { it.nombre.contains("Counter-Strike") },
                                                "Rocket League" to juegosSeed.find { it.nombre.contains("FIFA") }
                                            )
                                            
                                            updatesMap.forEach { (oldName, newJuego) ->
                                                if (newJuego != null) {
                                                    val existingJuegos = allJuegos.filter { 
                                                        it.nombre == oldName || 
                                                        (oldName == "Counter-Strike 2" && it.nombre.contains("Counter-Strike") && !it.nombre.contains("Prime")) ||
                                                        (oldName == "Rocket League" && it.nombre == "Rocket League")
                                                    }
                                                    
                                                    existingJuegos.forEach { existing ->
                                                        try {
                                                            val categoriaIdReal = categoriasMap[newJuego.categoriaId] ?: newJuego.categoriaId
                                                            juegoDao.updateFull(
                                                                id = existing.id,
                                                                nombre = newJuego.nombre,
                                                                descripcion = newJuego.descripcion,
                                                                precio = newJuego.precio,
                                                                stock = newJuego.stock,
                                                                imagenUrl = newJuego.imagenUrl,
                                                                desarrollador = newJuego.desarrollador,
                                                                fechaLanzamiento = newJuego.fechaLanzamiento,
                                                                categoriaId = categoriaIdReal,
                                                                generoId = newJuego.generoId,
                                                                activo = true
                                                            )
                                                            Log.d("AppDatabase", "  ✅ Actualizado: '${existing.nombre}' -> '${newJuego.nombre}' (precio: ${newJuego.precio})")
                                                        } catch (e: Exception) {
                                                            Log.e("AppDatabase", "  ❌ Error actualizando ${existing.nombre}: ${e.message}")
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("AppDatabase", "Error actualizando juegos existentes: ${e.message}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("AppDatabase", "❌ ERROR CRÍTICO en onOpen: ${e.message}", e)
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