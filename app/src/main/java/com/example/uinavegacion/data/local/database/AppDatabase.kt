package com.example.uinavegacion.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 6, // Se añade campo isBlocked a UserEntity
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "onCreate CALLED. Seeding data...")
                            CoroutineScope(Dispatchers.IO).launch {
                                val userDao = getInstance(context).userDao()
                                val adminDao = getInstance(context).adminDao()

                                // Precargamos usuarios
                                val userSeed = listOf(
                                    UserEntity(name = "Usuario Demo", email = "user1@demo.com", phone = "12345678", password = "Password123!"),
                                    UserEntity(name = "Usuario Test", email = "test@test.com", phone = "87654321", password = "Password123!")
                                )


                                
                                val userCount = userDao.count()
                                Log.d("AppDatabase", "User count before seed: $userCount")
                                if (userCount == 0) {
                                    Log.d("AppDatabase", "Seeding users...")
                                    userSeed.forEach { userDao.insert(it) }
                                }

                                // Precargamos administradores
                                val adminSeed = listOf(
                                    AdminEntity(name = "Administrador Principal", email = "admin@steamish.com", phone = "88776655", password = "Admin123!", role = "SUPER_ADMIN"),
                                    AdminEntity(name = "Gerente de Juegos", email = "manager@steamish.com", phone = "77665544", password = "Manager456@", role = "GAME_MANAGER"),
                                    AdminEntity(name = "Soporte Técnico", email = "support@steamish.com", phone = "66554433", password = "Support789#", role = "SUPPORT")
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

                                // Precargamos algunos juegos para las estadísticas
                                val juegoDao = getInstance(context).juegoDao()
                                if (juegoDao.count() == 0) {
                                    Log.d("AppDatabase", "Seeding games...")
                                    val juegosSeed = listOf(
                                        JuegoEntity(
                                            nombre = "The Witcher 3", 
                                            precio = 39.99, 
                                            imagenUrl = "", 
                                            descripcion = "RPG épico", 
                                            stock = 100,
                                            desarrollador = "CD Projekt RED",
                                            fechaLanzamiento = "2015-05-19",
                                            categoriaId = 1, 
                                            generoId = 1
                                        ),
                                        JuegoEntity(
                                            nombre = "Cyberpunk 2077", 
                                            precio = 59.99, 
                                            imagenUrl = "", 
                                            descripcion = "RPG futurista", 
                                            stock = 75,
                                            desarrollador = "CD Projekt RED",
                                            fechaLanzamiento = "2020-12-10",
                                            categoriaId = 1, 
                                            generoId = 1
                                        ),
                                        JuegoEntity(
                                            nombre = "Call of Duty", 
                                            precio = 69.99, 
                                            imagenUrl = "", 
                                            descripcion = "Shooter", 
                                            stock = 50,
                                            desarrollador = "Activision",
                                            fechaLanzamiento = "2023-10-27",
                                            categoriaId = 1, 
                                            generoId = 1
                                        )
                                    )
                                    juegosSeed.forEach { juegoDao.insert(it) }
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}