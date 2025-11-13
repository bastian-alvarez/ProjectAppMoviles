# 🏗️ Construcción de Microservicios según Modelos Actuales

Este documento muestra cómo transformar tus entidades Room actuales en modelos de microservicios.

---

## 📋 Mapeo de Entidades a Microservicios

### **Entidades Actuales → Microservicios:**

| Entidad Room | Microservicio | Modelo en Servicio |
|-------------|---------------|-------------------|
| `UserEntity` | Auth Service | `User` |
| `AdminEntity` | Auth Service | `Admin` |
| `JuegoEntity` | Game Catalog Service | `Game` |
| `CategoriaEntity` | Game Catalog Service | `Category` |
| `GeneroEntity` | Game Catalog Service | `Genre` |
| `OrdenCompraEntity` | Order Service | `Order` |
| `DetalleEntity` | Order Service | `OrderDetail` |
| `LibraryEntity` | Library Service | `LibraryItem` |
| `ResenaEntity` | Review Service | `Review` |

---

## 🔐 1. AUTH SERVICE - Modelos

### **1.1 Modelo User (desde UserEntity)**

#### **Node.js/Express (TypeScript/JavaScript)**

```javascript
// models/User.js
const { DataTypes } = require('sequelize');
// O si usas Mongoose:
// const mongoose = require('mongoose');

// Opción A: Sequelize (MySQL)
const User = sequelize.define('User', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    name: {
        type: DataTypes.STRING,
        allowNull: false
    },
    email: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true,
        validate: {
            isEmail: true
        }
    },
    phone: {
        type: DataTypes.STRING,
        allowNull: false
    },
    password: {
        type: DataTypes.STRING,
        allowNull: false
    },
    profilePhotoUri: {
        type: DataTypes.STRING(500),
        allowNull: true,
        field: 'profile_photo_uri'
    },
    isBlocked: {
        type: DataTypes.BOOLEAN,
        defaultValue: false,
        field: 'is_blocked'
    },
    gender: {
        type: DataTypes.STRING,
        defaultValue: ''
    },
    createdAt: {
        type: DataTypes.DATE,
        field: 'created_at'
    },
    updatedAt: {
        type: DataTypes.DATE,
        field: 'updated_at'
    }
}, {
    tableName: 'users',
    timestamps: true
});

module.exports = User;
```

#### **PHP/Laravel**

```php
<?php
// app/Models/User.php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class User extends Model
{
    protected $table = 'users';
    
    protected $fillable = [
        'name',
        'email',
        'phone',
        'password',
        'profile_photo_uri',
        'is_blocked',
        'gender'
    ];
    
    protected $hidden = [
        'password'
    ];
    
    protected $casts = [
        'is_blocked' => 'boolean',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    // Relaciones
    public function orders()
    {
        return $this->hasMany(Order::class);
    }
    
    public function library()
    {
        return $this->hasMany(LibraryItem::class);
    }
    
    public function reviews()
    {
        return $this->hasMany(Review::class);
    }
}
```

### **1.2 Modelo Admin (desde AdminEntity)**

#### **Node.js/Express**

```javascript
// models/Admin.js
const Admin = sequelize.define('Admin', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    name: {
        type: DataTypes.STRING,
        allowNull: false
    },
    email: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true
    },
    phone: {
        type: DataTypes.STRING,
        allowNull: false
    },
    password: {
        type: DataTypes.STRING,
        allowNull: false
    },
    role: {
        type: DataTypes.ENUM(
            'SUPER_ADMIN',
            'GAME_MANAGER',
            'SUPPORT',
            'MODERATOR'
        ),
        allowNull: false
    },
    profilePhotoUri: {
        type: DataTypes.STRING(500),
        allowNull: true,
        field: 'profile_photo_uri'
    }
}, {
    tableName: 'admins',
    timestamps: true
});

module.exports = Admin;
```

#### **PHP/Laravel**

```php
<?php
// app/Models/Admin.php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Admin extends Model
{
    protected $table = 'admins';
    
    protected $fillable = [
        'name',
        'email',
        'phone',
        'password',
        'role',
        'profile_photo_uri'
    ];
    
    protected $hidden = [
        'password'
    ];
    
    const ROLES = [
        'SUPER_ADMIN' => 'Super Administrador',
        'GAME_MANAGER' => 'Gerente de Juegos',
        'SUPPORT' => 'Soporte Técnico',
        'MODERATOR' => 'Moderador'
    ];
}
```

### **1.3 DTOs (Data Transfer Objects) para API**

#### **Request DTOs**

```javascript
// dtos/RegisterUserDTO.js
class RegisterUserDTO {
    constructor(data) {
        this.name = data.name;
        this.email = data.email;
        this.phone = data.phone;
        this.password = data.password;
        this.gender = data.gender || '';
    }
    
    validate() {
        if (!this.name || !this.email || !this.phone || !this.password) {
            throw new Error('Campos requeridos faltantes');
        }
        if (!this.email.includes('@')) {
            throw new Error('Email inválido');
        }
        return true;
    }
}

// dtos/LoginDTO.js
class LoginDTO {
    constructor(data) {
        this.email = data.email;
        this.password = data.password;
    }
}
```

#### **Response DTOs**

```javascript
// dtos/UserResponseDTO.js
class UserResponseDTO {
    constructor(user) {
        this.id = user.id;
        this.name = user.name;
        this.email = user.email;
        this.phone = user.phone;
        this.profilePhotoUri = user.profilePhotoUri;
        this.isBlocked = user.isBlocked;
        this.gender = user.gender;
        // NO incluir password
    }
    
    static fromEntity(userEntity) {
        return new UserResponseDTO({
            id: userEntity.id,
            name: userEntity.name,
            email: userEntity.email,
            phone: userEntity.phone,
            profilePhotoUri: userEntity.profilePhotoUri,
            isBlocked: userEntity.isBlocked,
            gender: userEntity.gender
        });
    }
}

// dtos/AuthResponseDTO.js
class AuthResponseDTO {
    constructor(user, token) {
        this.user = new UserResponseDTO(user);
        this.token = token;
        this.expiresIn = '24h';
    }
}
```

---

## 🎮 2. GAME CATALOG SERVICE - Modelos

### **2.1 Modelo Game (desde JuegoEntity)**

#### **Node.js/Express**

```javascript
// models/Game.js
const Game = sequelize.define('Game', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    nombre: {
        type: DataTypes.STRING,
        allowNull: false,
        field: 'nombre'
    },
    descripcion: {
        type: DataTypes.TEXT,
        allowNull: false
    },
    precio: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: false
    },
    stock: {
        type: DataTypes.INTEGER,
        defaultValue: 0
    },
    imagenUrl: {
        type: DataTypes.STRING(500),
        allowNull: true,
        field: 'imagen_url'
    },
    desarrollador: {
        type: DataTypes.STRING,
        defaultValue: 'Desarrollador'
    },
    fechaLanzamiento: {
        type: DataTypes.STRING,
        defaultValue: '2024',
        field: 'fecha_lanzamiento'
    },
    categoriaId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'categoria_id',
        references: {
            model: 'categorias',
            key: 'id'
        }
    },
    generoId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'genero_id',
        references: {
            model: 'generos',
            key: 'id'
        }
    },
    activo: {
        type: DataTypes.BOOLEAN,
        defaultValue: true
    },
    descuento: {
        type: DataTypes.INTEGER,
        defaultValue: 0
    }
}, {
    tableName: 'juegos',
    timestamps: false
});

// Relaciones
Game.belongsTo(Categoria, { foreignKey: 'categoriaId', as: 'categoria' });
Game.belongsTo(Genero, { foreignKey: 'generoId', as: 'genero' });

module.exports = Game;
```

#### **PHP/Laravel**

```php
<?php
// app/Models/Game.php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Game extends Model
{
    protected $table = 'juegos';
    
    protected $fillable = [
        'nombre',
        'descripcion',
        'precio',
        'stock',
        'imagen_url',
        'desarrollador',
        'fecha_lanzamiento',
        'categoria_id',
        'genero_id',
        'activo',
        'descuento'
    ];
    
    protected $casts = [
        'precio' => 'decimal:2',
        'stock' => 'integer',
        'activo' => 'boolean',
        'descuento' => 'integer'
    ];
    
    // Relaciones
    public function categoria()
    {
        return $this->belongsTo(Category::class, 'categoria_id');
    }
    
    public function genero()
    {
        return $this->belongsTo(Genre::class, 'genero_id');
    }
    
    // Accessors (propiedades computadas)
    public function getDiscountedPriceAttribute()
    {
        if ($this->descuento > 0) {
            return $this->precio * (1 - $this->descuento / 100);
        }
        return $this->precio;
    }
    
    public function getHasDiscountAttribute()
    {
        return $this->descuento > 0;
    }
}
```

### **2.2 Modelo Category (desde CategoriaEntity)**

```javascript
// models/Category.js
const Category = sequelize.define('Category', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    nombre: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true
    },
    descripcion: {
        type: DataTypes.TEXT,
        allowNull: true
    }
}, {
    tableName: 'categorias',
    timestamps: false
});

Category.hasMany(Game, { foreignKey: 'categoriaId', as: 'juegos' });

module.exports = Category;
```

### **2.3 Modelo Genre (desde GeneroEntity)**

```javascript
// models/Genre.js
const Genre = sequelize.define('Genre', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    nombre: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true
    },
    descripcion: {
        type: DataTypes.TEXT,
        allowNull: true
    }
}, {
    tableName: 'generos',
    timestamps: false
});

Genre.hasMany(Game, { foreignKey: 'generoId', as: 'juegos' });

module.exports = Genre;
```

### **2.4 DTOs para Game Catalog**

```javascript
// dtos/GameResponseDTO.js
class GameResponseDTO {
    constructor(game) {
        this.id = game.id;
        this.nombre = game.nombre;
        this.descripcion = game.descripcion;
        this.precio = game.precio;
        this.stock = game.stock;
        this.imagenUrl = game.imagenUrl || '';
        this.desarrollador = game.desarrollador;
        this.fechaLanzamiento = game.fechaLanzamiento;
        this.categoriaId = game.categoriaId;
        this.generoId = game.generoId;
        this.activo = game.activo;
        this.descuento = game.descuento;
        
        // Propiedades computadas
        this.discountedPrice = game.descuento > 0 
            ? game.precio * (1 - game.descuento / 100) 
            : game.precio;
        this.hasDiscount = game.descuento > 0;
        
        // Si incluye relaciones
        if (game.categoria) {
            this.categoria = {
                id: game.categoria.id,
                nombre: game.categoria.nombre
            };
        }
        if (game.genero) {
            this.genero = {
                id: game.genero.id,
                nombre: game.genero.nombre
            };
        }
    }
    
    static fromEntity(juegoEntity) {
        return new GameResponseDTO({
            id: juegoEntity.id,
            nombre: juegoEntity.nombre,
            descripcion: juegoEntity.descripcion,
            precio: juegoEntity.precio,
            stock: juegoEntity.stock,
            imagenUrl: juegoEntity.imagenUrl,
            desarrollador: juegoEntity.desarrollador,
            fechaLanzamiento: juegoEntity.fechaLanzamiento,
            categoriaId: juegoEntity.categoriaId,
            generoId: juegoEntity.generoId,
            activo: juegoEntity.activo,
            descuento: juegoEntity.descuento
        });
    }
}

// dtos/CreateGameDTO.js
class CreateGameDTO {
    constructor(data) {
        this.nombre = data.nombre;
        this.descripcion = data.descripcion;
        this.precio = parseFloat(data.precio);
        this.stock = parseInt(data.stock) || 0;
        this.imagenUrl = data.imagenUrl || '';
        this.desarrollador = data.desarrollador || 'Desarrollador';
        this.fechaLanzamiento = data.fechaLanzamiento || '2024';
        this.categoriaId = parseInt(data.categoriaId);
        this.generoId = parseInt(data.generoId);
        this.descuento = parseInt(data.descuento) || 0;
    }
    
    validate() {
        if (!this.nombre || !this.descripcion || !this.precio || 
            !this.categoriaId || !this.generoId) {
            throw new Error('Campos requeridos faltantes');
        }
        if (this.precio < 0) {
            throw new Error('El precio no puede ser negativo');
        }
        return true;
    }
}
```

---

## 🛒 3. ORDER SERVICE - Modelos

### **3.1 Modelo Order (desde OrdenCompraEntity)**

```javascript
// models/Order.js
const Order = sequelize.define('Order', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    userId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'user_id',
        references: {
            model: 'users',
            key: 'id'
        }
    },
    fechaOrden: {
        type: DataTypes.STRING,
        allowNull: false,
        field: 'fecha_orden'
    },
    total: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: false
    },
    estadoId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        defaultValue: 1,
        field: 'estado_id',
        references: {
            model: 'estados',
            key: 'id'
        }
    },
    metodoPago: {
        type: DataTypes.STRING,
        allowNull: false,
        field: 'metodo_pago'
    },
    direccionEnvio: {
        type: DataTypes.STRING(500),
        allowNull: true,
        field: 'direccion_envio'
    }
}, {
    tableName: 'ordenes_compra',
    timestamps: false
});

Order.belongsTo(User, { foreignKey: 'userId', as: 'usuario' });
Order.belongsTo(Estado, { foreignKey: 'estadoId', as: 'estado' });
Order.hasMany(OrderDetail, { foreignKey: 'ordenId', as: 'detalles' });

module.exports = Order;
```

### **3.2 Modelo OrderDetail (desde DetalleEntity)**

```javascript
// models/OrderDetail.js
const OrderDetail = sequelize.define('OrderDetail', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    ordenId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'orden_id',
        references: {
            model: 'ordenes_compra',
            key: 'id'
        }
    },
    juegoId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'juego_id',
        references: {
            model: 'juegos',
            key: 'id'
        }
    },
    cantidad: {
        type: DataTypes.INTEGER,
        allowNull: false
    },
    precioUnitario: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: false,
        field: 'precio_unitario'
    },
    subtotal: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: false
    }
}, {
    tableName: 'detalles_orden',
    timestamps: false
});

OrderDetail.belongsTo(Order, { foreignKey: 'ordenId', as: 'orden' });
OrderDetail.belongsTo(Game, { foreignKey: 'juegoId', as: 'juego' });

module.exports = OrderDetail;
```

### **3.3 DTOs para Order Service**

```javascript
// dtos/CreateOrderDTO.js
class CreateOrderDTO {
    constructor(data) {
        this.userId = parseInt(data.userId);
        this.items = data.items; // Array de { juegoId, cantidad }
        this.metodoPago = data.metodoPago || 'Tarjeta';
        this.direccionEnvio = data.direccionEnvio || null;
    }
    
    validate() {
        if (!this.userId || !this.items || this.items.length === 0) {
            throw new Error('Datos de orden inválidos');
        }
        return true;
    }
}

// dtos/OrderResponseDTO.js
class OrderResponseDTO {
    constructor(order) {
        this.id = order.id;
        this.userId = order.userId;
        this.fechaOrden = order.fechaOrden;
        this.total = parseFloat(order.total);
        this.estadoId = order.estadoId;
        this.metodoPago = order.metodoPago;
        this.direccionEnvio = order.direccionEnvio;
        
        if (order.detalles) {
            this.detalles = order.detalles.map(d => ({
                id: d.id,
                juegoId: d.juegoId,
                cantidad: d.cantidad,
                precioUnitario: parseFloat(d.precioUnitario),
                subtotal: parseFloat(d.subtotal),
                juego: d.juego ? {
                    id: d.juego.id,
                    nombre: d.juego.nombre,
                    imagenUrl: d.juego.imagenUrl
                } : null
            }));
        }
        
        if (order.estado) {
            this.estado = {
                id: order.estado.id,
                nombre: order.estado.nombre
            };
        }
    }
}
```

---

## 📚 4. LIBRARY SERVICE - Modelos

### **4.1 Modelo LibraryItem (desde LibraryEntity)**

```javascript
// models/LibraryItem.js
const LibraryItem = sequelize.define('LibraryItem', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    userId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'user_id',
        references: {
            model: 'users',
            key: 'id'
        }
    },
    juegoId: {
        type: DataTypes.STRING,
        allowNull: false,
        field: 'juego_id'
    },
    name: {
        type: DataTypes.STRING,
        allowNull: false
    },
    price: {
        type: DataTypes.DECIMAL(10, 2),
        allowNull: false
    },
    dateAdded: {
        type: DataTypes.STRING,
        allowNull: false,
        field: 'date_added'
    },
    status: {
        type: DataTypes.STRING,
        defaultValue: 'Disponible'
    },
    genre: {
        type: DataTypes.STRING,
        defaultValue: 'Acción'
    }
}, {
    tableName: 'biblioteca',
    timestamps: false
});

LibraryItem.belongsTo(User, { foreignKey: 'userId', as: 'usuario' });

module.exports = LibraryItem;
```

### **4.2 DTOs para Library Service**

```javascript
// dtos/LibraryItemResponseDTO.js
class LibraryItemResponseDTO {
    constructor(item) {
        this.id = item.id;
        this.userId = item.userId;
        this.juegoId = item.juegoId;
        this.name = item.name;
        this.price = parseFloat(item.price);
        this.dateAdded = item.dateAdded;
        this.status = item.status;
        this.genre = item.genre;
    }
    
    static fromEntity(libraryEntity) {
        return new LibraryItemResponseDTO({
            id: libraryEntity.id,
            userId: libraryEntity.userId,
            juegoId: libraryEntity.juegoId,
            name: libraryEntity.name,
            price: libraryEntity.price,
            dateAdded: libraryEntity.dateAdded,
            status: libraryEntity.status,
            genre: libraryEntity.genre
        });
    }
}
```

---

## ⭐ 5. REVIEW SERVICE - Modelos

### **5.1 Modelo Review (desde ResenaEntity)**

```javascript
// models/Review.js
const Review = sequelize.define('Review', {
    id: {
        type: DataTypes.BIGINT,
        primaryKey: true,
        autoIncrement: true
    },
    userId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'user_id',
        references: {
            model: 'users',
            key: 'id'
        }
    },
    juegoId: {
        type: DataTypes.BIGINT,
        allowNull: false,
        field: 'juego_id',
        references: {
            model: 'juegos',
            key: 'id'
        }
    },
    calificacion: {
        type: DataTypes.INTEGER,
        allowNull: false,
        defaultValue: 1,
        validate: {
            min: 1,
            max: 5
        }
    },
    comentario: {
        type: DataTypes.TEXT,
        allowNull: false
    },
    fechaCreacion: {
        type: DataTypes.STRING,
        allowNull: false,
        field: 'fecha_creacion'
    },
    isDeleted: {
        type: DataTypes.BOOLEAN,
        defaultValue: false,
        field: 'is_deleted'
    }
}, {
    tableName: 'resenas',
    timestamps: false
});

Review.belongsTo(User, { foreignKey: 'userId', as: 'usuario' });
Review.belongsTo(Game, { foreignKey: 'juegoId', as: 'juego' });

module.exports = Review;
```

### **5.2 DTOs para Review Service**

```javascript
// dtos/CreateReviewDTO.js
class CreateReviewDTO {
    constructor(data) {
        this.userId = parseInt(data.userId);
        this.juegoId = parseInt(data.juegoId);
        this.calificacion = parseInt(data.calificacion);
        this.comentario = data.comentario;
    }
    
    validate() {
        if (!this.userId || !this.juegoId || !this.comentario) {
            throw new Error('Campos requeridos faltantes');
        }
        if (this.calificacion < 1 || this.calificacion > 5) {
            throw new Error('La calificación debe estar entre 1 y 5');
        }
        return true;
    }
}

// dtos/ReviewResponseDTO.js
class ReviewResponseDTO {
    constructor(review) {
        this.id = review.id;
        this.userId = review.userId;
        this.juegoId = review.juegoId;
        this.calificacion = review.calificacion;
        this.comentario = review.comentario;
        this.fechaCreacion = review.fechaCreacion;
        this.isDeleted = review.isDeleted;
        
        if (review.usuario) {
            this.usuario = {
                id: review.usuario.id,
                name: review.usuario.name
            };
        }
        
        if (review.juego) {
            this.juego = {
                id: review.juego.id,
                nombre: review.juego.nombre
            };
        }
    }
}
```

---

## 📁 Estructura de Carpetas por Microservicio

### **Estructura Node.js/Express:**

```
auth-service/
├── src/
│   ├── models/
│   │   ├── User.js
│   │   └── Admin.js
│   ├── controllers/
│   │   ├── AuthController.js
│   │   └── UserController.js
│   ├── routes/
│   │   └── authRoutes.js
│   ├── dtos/
│   │   ├── RegisterUserDTO.js
│   │   ├── LoginDTO.js
│   │   └── UserResponseDTO.js
│   ├── middleware/
│   │   ├── authMiddleware.js
│   │   └── validationMiddleware.js
│   ├── services/
│   │   ├── AuthService.js
│   │   └── UserService.js
│   └── utils/
│       ├── jwt.js
│       └── bcrypt.js
├── database/
│   ├── migrations/
│   └── seeders/
├── config/
│   └── database.js
└── package.json
```

### **Estructura PHP/Laravel:**

```
auth-service/
├── app/
│   ├── Models/
│   │   ├── User.php
│   │   └── Admin.php
│   ├── Http/
│   │   ├── Controllers/
│   │   │   ├── AuthController.php
│   │   │   └── UserController.php
│   │   └── Requests/
│   │       ├── RegisterUserRequest.php
│   │       └── LoginRequest.php
│   ├── Services/
│   │   ├── AuthService.php
│   │   └── UserService.php
│   └── Resources/
│       └── UserResource.php
├── database/
│   ├── migrations/
│   └── seeders/
├── routes/
│   └── api.php
└── composer.json
```

---

## 🔄 Transformación de Entidades Room a Modelos de Servicio

### **Ejemplo: Transformar UserEntity a User (Node.js)**

```javascript
// utils/entityMapper.js

class EntityMapper {
    // UserEntity → User Model
    static userEntityToModel(userEntity) {
        return {
            id: userEntity.id,
            name: userEntity.name,
            email: userEntity.email,
            phone: userEntity.phone,
            password: userEntity.password, // Hasheado
            profilePhotoUri: userEntity.profilePhotoUri,
            isBlocked: userEntity.isBlocked || false,
            gender: userEntity.gender || '',
            createdAt: new Date(),
            updatedAt: new Date()
        };
    }
    
    // JuegoEntity → Game Model
    static juegoEntityToModel(juegoEntity) {
        return {
            id: juegoEntity.id,
            nombre: juegoEntity.nombre,
            descripcion: juegoEntity.descripcion,
            precio: juegoEntity.precio,
            stock: juegoEntity.stock,
            imagenUrl: juegoEntity.imagenUrl || '',
            desarrollador: juegoEntity.desarrollador || 'Desarrollador',
            fechaLanzamiento: juegoEntity.fechaLanzamiento || '2024',
            categoriaId: juegoEntity.categoriaId,
            generoId: juegoEntity.generoId,
            activo: juegoEntity.activo !== false,
            descuento: juegoEntity.descuento || 0
        };
    }
    
    // ResenaEntity → Review Model
    static resenaEntityToModel(resenaEntity) {
        return {
            id: resenaEntity.id,
            userId: resenaEntity.userId,
            juegoId: resenaEntity.juegoId,
            calificacion: resenaEntity.calificacion || 1,
            comentario: resenaEntity.comentario,
            fechaCreacion: resenaEntity.fechaCreacion || new Date().toISOString(),
            isDeleted: resenaEntity.isDeleted || false
        };
    }
}

module.exports = EntityMapper;
```

---

## 📝 Scripts SQL para Crear Tablas en MySQL (XAMPP)

### **Auth Service - users y admins**

```sql
-- Base de datos: auth_db
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    profile_photo_uri VARCHAR(500),
    is_blocked BOOLEAN DEFAULT FALSE,
    gender VARCHAR(50) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_blocked (is_blocked)
);

CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('SUPER_ADMIN', 'GAME_MANAGER', 'SUPPORT', 'MODERATOR') NOT NULL,
    profile_photo_uri VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
);
```

### **Game Catalog Service - juegos, categorias, generos**

```sql
-- Base de datos: games_db
CREATE DATABASE IF NOT EXISTS games_db;
USE games_db;

CREATE TABLE categorias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    INDEX idx_nombre (nombre)
);

CREATE TABLE generos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    INDEX idx_nombre (nombre)
);

CREATE TABLE juegos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0,
    imagen_url VARCHAR(500),
    desarrollador VARCHAR(255) DEFAULT 'Desarrollador',
    fecha_lanzamiento VARCHAR(50) DEFAULT '2024',
    categoria_id BIGINT NOT NULL,
    genero_id BIGINT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    descuento INT DEFAULT 0,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE,
    FOREIGN KEY (genero_id) REFERENCES generos(id) ON DELETE CASCADE,
    INDEX idx_categoria (categoria_id),
    INDEX idx_genero (genero_id),
    INDEX idx_activo (activo),
    INDEX idx_descuento (descuento),
    INDEX idx_nombre (nombre)
);
```

### **Order Service - ordenes_compra, detalles_orden, estados**

```sql
-- Base de datos: orders_db
CREATE DATABASE IF NOT EXISTS orders_db;
USE orders_db;

CREATE TABLE estados (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT
);

CREATE TABLE ordenes_compra (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    fecha_orden VARCHAR(50) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    estado_id BIGINT NOT NULL DEFAULT 1,
    metodo_pago VARCHAR(100) NOT NULL,
    direccion_envio VARCHAR(500),
    FOREIGN KEY (estado_id) REFERENCES estados(id),
    INDEX idx_user (user_id),
    INDEX idx_fecha (fecha_orden),
    INDEX idx_estado (estado_id)
);

CREATE TABLE detalles_orden (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    orden_id BIGINT NOT NULL,
    juego_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (orden_id) REFERENCES ordenes_compra(id) ON DELETE CASCADE,
    INDEX idx_orden (orden_id),
    INDEX idx_juego (juego_id)
);
```

### **Library Service - biblioteca**

```sql
-- Base de datos: library_db
CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

CREATE TABLE biblioteca (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    juego_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    date_added VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'Disponible',
    genre VARCHAR(100) DEFAULT 'Acción',
    UNIQUE KEY unique_user_game (user_id, juego_id),
    INDEX idx_user (user_id),
    INDEX idx_juego (juego_id)
);
```

### **Review Service - resenas**

```sql
-- Base de datos: reviews_db
CREATE DATABASE IF NOT EXISTS reviews_db;
USE reviews_db;

CREATE TABLE resenas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    juego_id BIGINT NOT NULL,
    calificacion INT NOT NULL DEFAULT 1 CHECK (calificacion >= 1 AND calificacion <= 5),
    comentario TEXT NOT NULL,
    fecha_creacion VARCHAR(50) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    INDEX idx_user (user_id),
    INDEX idx_juego (juego_id),
    INDEX idx_deleted (is_deleted),
    INDEX idx_calificacion (calificacion)
);
```

---

## 🚀 Ejemplo de Controlador (Node.js/Express)

```javascript
// controllers/GameController.js
const Game = require('../models/Game');
const Category = require('../models/Category');
const Genre = require('../models/Genre');
const GameResponseDTO = require('../dtos/GameResponseDTO');
const CreateGameDTO = require('../dtos/CreateGameDTO');

class GameController {
    // GET /api/games
    async getAllGames(req, res) {
        try {
            const { categoria, genero, descuento, search } = req.query;
            
            const where = { activo: true };
            
            if (categoria) where.categoriaId = categoria;
            if (genero) where.generoId = genero;
            if (descuento === 'true') where.descuento = { [Op.gt]: 0 };
            
            const games = await Game.findAll({
                where,
                include: [
                    { model: Category, as: 'categoria' },
                    { model: Genre, as: 'genero' }
                ]
            });
            
            const gamesDTO = games.map(game => new GameResponseDTO(game));
            res.json(gamesDTO);
        } catch (error) {
            res.status(500).json({ error: error.message });
        }
    }
    
    // GET /api/games/:id
    async getGameById(req, res) {
        try {
            const game = await Game.findByPk(req.params.id, {
                include: [
                    { model: Category, as: 'categoria' },
                    { model: Genre, as: 'genero' }
                ]
            });
            
            if (!game) {
                return res.status(404).json({ error: 'Juego no encontrado' });
            }
            
            res.json(new GameResponseDTO(game));
        } catch (error) {
            res.status(500).json({ error: error.message });
        }
    }
    
    // POST /api/games (Admin only)
    async createGame(req, res) {
        try {
            const createDTO = new CreateGameDTO(req.body);
            createDTO.validate();
            
            const game = await Game.create(createDTO);
            res.status(201).json(new GameResponseDTO(game));
        } catch (error) {
            res.status(400).json({ error: error.message });
        }
    }
    
    // PUT /api/games/:id/stock (Admin only)
    async updateStock(req, res) {
        try {
            const { stock } = req.body;
            const game = await Game.findByPk(req.params.id);
            
            if (!game) {
                return res.status(404).json({ error: 'Juego no encontrado' });
            }
            
            game.stock = stock;
            await game.save();
            
            res.json(new GameResponseDTO(game));
        } catch (error) {
            res.status(500).json({ error: error.message });
        }
    }
}

module.exports = new GameController();
```

---

## 📱 Ejemplo de Cliente Android (Retrofit)

```kotlin
// data/remote/api/GameApi.kt
interface GameApi {
    @GET("games")
    suspend fun getAllGames(
        @Query("categoria") categoria: String? = null,
        @Query("genero") genero: String? = null,
        @Query("descuento") descuento: Boolean? = null
    ): Response<List<GameResponse>>
    
    @GET("games/{id}")
    suspend fun getGameById(@Path("id") id: Long): Response<GameResponse>
    
    @POST("games")
    suspend fun createGame(@Body game: CreateGameRequest): Response<GameResponse>
}

// data/remote/dto/GameResponse.kt
data class GameResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String,
    val desarrollador: String,
    val fechaLanzamiento: String,
    val categoriaId: Long,
    val generoId: Long,
    val activo: Boolean,
    val descuento: Int,
    val discountedPrice: Double,
    val hasDiscount: Boolean,
    val categoria: CategoryResponse? = null,
    val genero: GenreResponse? = null
)

// Mapper: GameResponse → JuegoEntity (para Room local)
fun GameResponse.toJuegoEntity(): JuegoEntity {
    return JuegoEntity(
        id = this.id,
        nombre = this.nombre,
        descripcion = this.descripcion,
        precio = this.precio,
        stock = this.stock,
        imagenUrl = this.imagenUrl,
        desarrollador = this.desarrollador,
        fechaLanzamiento = this.fechaLanzamiento,
        categoriaId = this.categoriaId,
        generoId = this.generoId,
        activo = this.activo,
        descuento = this.descuento
    )
}
```

---

## ✅ Checklist de Implementación

1. **Crear bases de datos en MySQL (XAMPP)**
2. **Crear modelos según entidades Room**
3. **Crear DTOs para requests/responses**
4. **Implementar controladores**
5. **Crear rutas y middleware**
6. **Probar endpoints con Postman**
7. **Integrar con Android usando Retrofit**

---

**¿Necesitas ayuda con algún servicio específico o con la implementación de algún modelo?** 🚀

