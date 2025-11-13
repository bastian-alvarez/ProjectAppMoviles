# 🏗️ Arquitectura de Microservicios - GameStore

## 📋 Resumen Ejecutivo

Propuesta de **5-6 microservicios** para migrar la aplicación Android a una arquitectura basada en microservicios con XAMPP (MySQL).

---

## 🎯 Microservicios Propuestos

### **Opción 1: 5 Microservicios (Recomendada)**

1. **Auth Service** - Autenticación y Gestión de Usuarios
2. **Game Catalog Service** - Catálogo de Juegos
3. **Order Service** - Compras y Carrito
4. **Library Service** - Biblioteca de Usuario
5. **Review Service** - Reseñas y Moderación

### **Opción 2: 6 Microservicios (Más granular)**

1. **Auth Service** - Autenticación y Usuarios
2. **Admin Service** - Gestión de Administradores
3. **Game Catalog Service** - Catálogo de Juegos
4. **Order Service** - Compras y Carrito
5. **Library Service** - Biblioteca de Usuario
6. **Review Service** - Reseñas y Moderación

---

## 🔐 1. Auth Service (Autenticación y Usuarios)

### **Responsabilidades:**
- Autenticación de usuarios (login, registro)
- Gestión de perfiles de usuario
- Gestión de administradores y roles
- Bloqueo/desbloqueo de usuarios
- Sesiones y tokens JWT

### **Base de Datos (MySQL):**
```sql
-- Tabla: users
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    profile_photo_uri VARCHAR(500),
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla: admins
CREATE TABLE admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    role ENUM('SUPER_ADMIN', 'GAME_MANAGER', 'SUPPORT', 'MODERATOR') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla: roles (si se necesita más granularidad)
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT
);
```

### **Endpoints REST:**
```
POST   /api/auth/register          - Registrar nuevo usuario
POST   /api/auth/login             - Login de usuario
POST   /api/auth/admin/login       - Login de administrador
GET    /api/users/{id}              - Obtener perfil de usuario
PUT    /api/users/{id}              - Actualizar perfil
PUT    /api/users/{id}/block       - Bloquear usuario
PUT    /api/users/{id}/unblock     - Desbloquear usuario
GET    /api/users                   - Listar usuarios (admin)
GET    /api/admins                  - Listar administradores (admin)
POST   /api/admins                  - Crear administrador (super admin)
```

### **Tecnologías sugeridas:**
- **Backend:** Node.js (Express) o PHP (Laravel/Slim)
- **Base de datos:** MySQL en XAMPP
- **Autenticación:** JWT (JSON Web Tokens)
- **Seguridad:** bcrypt para passwords

---

## 🎮 2. Game Catalog Service (Catálogo de Juegos)

### **Responsabilidades:**
- CRUD de juegos
- Gestión de categorías y géneros
- Búsqueda y filtrado de juegos
- Gestión de stock
- Ofertas y descuentos

### **Base de Datos (MySQL):**
```sql
-- Tabla: categorias
CREATE TABLE categorias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: generos
CREATE TABLE generos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: juegos
CREATE TABLE juegos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0,
    imagen_url VARCHAR(500),
    desarrollador VARCHAR(255),
    fecha_lanzamiento VARCHAR(50),
    categoria_id BIGINT,
    genero_id BIGINT,
    descuento INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    FOREIGN KEY (genero_id) REFERENCES generos(id),
    INDEX idx_categoria (categoria_id),
    INDEX idx_genero (genero_id),
    INDEX idx_activo (activo),
    INDEX idx_descuento (descuento)
);
```

### **Endpoints REST:**
```
GET    /api/games                   - Listar todos los juegos
GET    /api/games/{id}               - Obtener juego por ID
GET    /api/games?categoria={cat}    - Filtrar por categoría
GET    /api/games?genero={gen}       - Filtrar por género
GET    /api/games?descuento=true     - Juegos con descuento
GET    /api/games/search?q={query}    - Buscar juegos
POST   /api/games                    - Crear juego (admin)
PUT    /api/games/{id}                - Actualizar juego (admin)
DELETE /api/games/{id}               - Eliminar juego (admin)
PUT    /api/games/{id}/stock         - Actualizar stock (admin)

GET    /api/categories                - Listar categorías
GET    /api/genres                    - Listar géneros
```

### **Tecnologías sugeridas:**
- **Backend:** Node.js (Express) o PHP (Laravel)
- **Base de datos:** MySQL en XAMPP
- **Búsqueda:** LIKE o Full-Text Search de MySQL

---

## 🛒 3. Order Service (Compras y Carrito)

### **Responsabilidades:**
- Gestión del carrito de compras
- Procesamiento de órdenes
- Actualización de stock (comunicación con Game Catalog)
- Historial de compras
- Detalles de órdenes

### **Base de Datos (MySQL):**
```sql
-- Tabla: estados
CREATE TABLE estados (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT
);

-- Tabla: ordenes_compra
CREATE TABLE ordenes_compra (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    fecha_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10, 2) NOT NULL,
    estado_id BIGINT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (estado_id) REFERENCES estados(id),
    INDEX idx_user (user_id),
    INDEX idx_fecha (fecha_compra)
);

-- Tabla: detalles_orden
CREATE TABLE detalles_orden (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    orden_id BIGINT NOT NULL,
    juego_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (orden_id) REFERENCES ordenes_compra(id) ON DELETE CASCADE,
    FOREIGN KEY (juego_id) REFERENCES juegos(id),
    INDEX idx_orden (orden_id)
);
```

### **Endpoints REST:**
```
POST   /api/cart/add                 - Agregar juego al carrito
GET    /api/cart                     - Obtener carrito del usuario
PUT    /api/cart/{itemId}             - Actualizar cantidad
DELETE /api/cart/{itemId}             - Eliminar del carrito
DELETE /api/cart                      - Vaciar carrito

POST   /api/orders                    - Crear orden de compra
GET    /api/orders                    - Listar órdenes del usuario
GET    /api/orders/{id}               - Obtener detalle de orden
PUT    /api/orders/{id}/status        - Actualizar estado (admin)
```

### **Comunicación con otros servicios:**
- **Game Catalog Service:** Verificar stock antes de comprar
- **Game Catalog Service:** Actualizar stock después de compra
- **Auth Service:** Validar usuario autenticado

### **Tecnologías sugeridas:**
- **Backend:** Node.js (Express) o PHP (Laravel)
- **Base de datos:** MySQL en XAMPP
- **Comunicación:** HTTP REST o RabbitMQ (opcional)

---

## 📚 4. Library Service (Biblioteca de Usuario)

### **Responsabilidades:**
- Gestión de biblioteca de juegos del usuario
- Agregar juegos comprados a la biblioteca
- Listar juegos del usuario
- Estado de juegos (Disponible, Instalado, etc.)

### **Base de Datos (MySQL):**
```sql
-- Tabla: library
CREATE TABLE library (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    juego_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2),
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'Disponible',
    genre VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (juego_id) REFERENCES juegos(id),
    UNIQUE KEY unique_user_game (user_id, juego_id),
    INDEX idx_user (user_id),
    INDEX idx_juego (juego_id)
);
```

### **Endpoints REST:**
```
GET    /api/library                  - Obtener biblioteca del usuario
POST   /api/library                  - Agregar juego a biblioteca
GET    /api/library/{gameId}         - Verificar si usuario tiene juego
PUT    /api/library/{id}/status      - Actualizar estado del juego
DELETE /api/library/{id}             - Eliminar de biblioteca
```

### **Comunicación con otros servicios:**
- **Order Service:** Recibir notificación cuando se completa una compra
- **Game Catalog Service:** Obtener información del juego

### **Tecnologías sugeridas:**
- **Backend:** Node.js (Express) o PHP (Laravel)
- **Base de datos:** MySQL en XAMPP

---

## ⭐ 5. Review Service (Reseñas y Moderación)

### **Responsabilidades:**
- Crear y gestionar reseñas de juegos
- Calificaciones (1-5 estrellas)
- Moderación de reseñas (eliminar/restaurar)
- Estadísticas de reseñas por juego

### **Base de Datos (MySQL):**
```sql
-- Tabla: resenas
CREATE TABLE resenas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    juego_id BIGINT NOT NULL,
    comentario TEXT NOT NULL,
    calificacion INT NOT NULL DEFAULT 1 CHECK (calificacion >= 1 AND calificacion <= 5),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (juego_id) REFERENCES juegos(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_juego (juego_id),
    INDEX idx_deleted (is_deleted)
);
```

### **Endpoints REST:**
```
GET    /api/reviews/game/{gameId}    - Obtener reseñas de un juego
POST   /api/reviews                  - Crear reseña
PUT    /api/reviews/{id}             - Actualizar reseña
DELETE /api/reviews/{id}             - Eliminar reseña (soft delete)
GET    /api/reviews/user/{userId}    - Reseñas de un usuario

-- Endpoints de moderación (admin/moderator)
GET    /api/reviews/moderation       - Listar todas las reseñas (moderación)
PUT    /api/reviews/{id}/restore     - Restaurar reseña eliminada
GET    /api/reviews/stats/{gameId}   - Estadísticas de reseñas
```

### **Comunicación con otros servicios:**
- **Auth Service:** Validar permisos de moderador
- **Game Catalog Service:** Obtener información del juego

### **Tecnologías sugeridas:**
- **Backend:** Node.js (Express) o PHP (Laravel)
- **Base de datos:** MySQL en XAMPP

---

## 🔧 6. Admin Service (Opcional - Solo si usas 6 microservicios)

### **Responsabilidades:**
- Dashboard de administración
- Estadísticas generales
- Gestión avanzada de administradores
- Reportes y analytics

### **Endpoints REST:**
```
GET    /api/admin/stats              - Estadísticas generales
GET    /api/admin/users/stats        - Estadísticas de usuarios
GET    /api/admin/games/stats         - Estadísticas de juegos
GET    /api/admin/orders/stats        - Estadísticas de ventas
GET    /api/admin/reports             - Generar reportes
```

---

## 🌐 Arquitectura de Comunicación

### **Patrón de Comunicación:**
```
┌─────────────┐
│   Android   │
│     App     │
└──────┬──────┘
       │
       │ HTTP/REST
       │
┌──────▼──────────────────────────────────────┐
│         API Gateway (Opcional)              │
│    (Nginx, Kong, o similar)                 │
└──────┬──────────────────────────────────────┘
       │
       ├──────────┬──────────┬──────────┬──────────┐
       │          │          │          │          │
┌──────▼──┐ ┌─────▼───┐ ┌───▼────┐ ┌───▼────┐ ┌──▼─────┐
│  Auth   │ │  Game   │ │ Order  │ │Library │ │Review  │
│ Service │ │Catalog  │ │Service │ │Service │ │Service │
└────┬────┘ └────┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
     │           │           │          │          │
     └───────────┴───────────┴──────────┴──────────┘
                    │
            ┌────────▼────────┐
            │   MySQL (XAMPP)  │
            │   - auth_db       │
            │   - games_db      │
            │   - orders_db     │
            │   - library_db    │
            │   - reviews_db    │
            └──────────────────┘
```

---

## 📦 Estructura de Proyecto Sugerida

```
microservicios/
├── auth-service/
│   ├── src/
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── routes/
│   │   └── middleware/
│   ├── database/
│   │   └── migrations/
│   └── package.json (o composer.json)
│
├── game-catalog-service/
│   ├── src/
│   ├── database/
│   └── package.json
│
├── order-service/
│   ├── src/
│   ├── database/
│   └── package.json
│
├── library-service/
│   ├── src/
│   ├── database/
│   └── package.json
│
└── review-service/
    ├── src/
    ├── database/
    └── package.json
```

---

## 🔐 Consideraciones de Seguridad

1. **Autenticación JWT:**
   - Todos los servicios validan tokens JWT
   - El Auth Service genera y valida tokens

2. **CORS:**
   - Configurar CORS para permitir requests desde la app Android

3. **Validación de Datos:**
   - Validar todos los inputs en cada servicio
   - Sanitizar datos antes de guardar en BD

4. **Rate Limiting:**
   - Implementar límites de requests por usuario/IP

5. **HTTPS:**
   - Usar HTTPS en producción (en desarrollo con XAMPP puede ser HTTP)

---

## 🚀 Pasos de Implementación

### **Fase 1: Setup Inicial**
1. Instalar XAMPP y crear bases de datos separadas o una base de datos con prefijos
2. Crear estructura de carpetas para cada microservicio
3. Configurar rutas básicas

### **Fase 2: Auth Service**
1. Implementar registro y login
2. Implementar JWT
3. Probar desde Postman

### **Fase 3: Game Catalog Service**
1. Migrar datos de juegos
2. Implementar CRUD
3. Implementar búsqueda y filtros

### **Fase 4: Order Service**
1. Implementar carrito
2. Implementar checkout
3. Integrar con Game Catalog para stock

### **Fase 5: Library Service**
1. Implementar biblioteca
2. Integrar con Order Service

### **Fase 6: Review Service**
1. Implementar reseñas
2. Implementar moderación

### **Fase 7: Integración Android**
1. Reemplazar Room Database por llamadas HTTP
2. Implementar Retrofit/OkHttp
3. Manejar estados offline (opcional)

---

## 📊 Base de Datos en XAMPP

### **Opción A: Una base de datos con prefijos**
```sql
CREATE DATABASE gamestore_db;

USE gamestore_db;

-- Tablas con prefijos
CREATE TABLE auth_users (...);
CREATE TABLE auth_admins (...);
CREATE TABLE games_juegos (...);
CREATE TABLE games_categorias (...);
CREATE TABLE orders_ordenes_compra (...);
CREATE TABLE library_library (...);
CREATE TABLE reviews_resenas (...);
```

### **Opción B: Bases de datos separadas (más limpio)**
```sql
CREATE DATABASE auth_db;
CREATE DATABASE games_db;
CREATE DATABASE orders_db;
CREATE DATABASE library_db;
CREATE DATABASE reviews_db;
```

---

## 🛠️ Tecnologías Recomendadas por Servicio

### **Node.js + Express (Recomendado para aprendizaje)**
- Fácil de aprender
- JavaScript en frontend y backend
- Buena comunidad

### **PHP + Laravel/Slim (Si ya conoces PHP)**
- Integración natural con XAMPP
- Laravel tiene ORM (Eloquent)
- Slim es más ligero

### **Python + Flask/FastAPI**
- Fácil de leer
- FastAPI tiene documentación automática

---

## 📝 Notas Finales

1. **Empezar simple:** Implementa primero Auth Service y Game Catalog
2. **Testing:** Prueba cada servicio independientemente con Postman
3. **Documentación:** Documenta cada endpoint con Swagger/OpenAPI
4. **Versionado:** Usa versionado de API (`/api/v1/...`)
5. **Logs:** Implementa logging en cada servicio
6. **Error Handling:** Maneja errores de forma consistente

---

## ✅ Checklist de Implementación

- [ ] Setup XAMPP y bases de datos
- [ ] Auth Service (login, registro, JWT)
- [ ] Game Catalog Service (CRUD juegos)
- [ ] Order Service (carrito y compras)
- [ ] Library Service (biblioteca)
- [ ] Review Service (reseñas y moderación)
- [ ] Integración Android (Retrofit)
- [ ] Testing de endpoints
- [ ] Documentación API
- [ ] Deploy en servidor (opcional)

---

**¿Necesitas ayuda con la implementación de algún servicio específico?** 🚀

