# 🛒 ERROR 403 AL CREAR ÓRDENES DE COMPRA

## 🐛 Problema Identificado

Al intentar completar una compra, el microservicio de órdenes devuelve **403 Forbidden**.

---

## 📊 Logs del Error

```
D/CartViewModel: Iniciando checkout para usuario 2
I/okhttp: --> POST http://10.0.2.2:3003/api/orders
I/okhttp: Authorization: Bearer eyJhbGci...
I/okhttp: {"items":[{"cantidad":1,"juegoId":44}],"metodoPago":"Tarjeta","userId":2}
I/okhttp: <-- 403 Forbidden
E/CartViewModel: Error en checkout
```

---

## 🔍 Análisis

### ✅ Lo que está BIEN:
- ✅ La ruta es correcta: `POST /api/orders`
- ✅ El token JWT se está enviando
- ✅ El JSON está bien formado
- ✅ El Content-Type es correcto

### ❌ El Problema:
- ❌ El microservicio devuelve **403 Forbidden**
- ❌ Esto indica un problema de **autorización**, no de autenticación

---

## 🎯 Posibles Causas

### 1. **Configuración de Seguridad en el Microservicio**

El endpoint `POST /api/orders` podría estar configurado para:
- Requerir un rol específico (ej: ADMIN)
- Estar bloqueado para todos
- Tener una configuración de CORS incorrecta

**Verificar en el backend (Order Service)**:
```java
// SecurityConfig.java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers("/api/orders").permitAll()  // ¿Está permitido?
        // o
        .antMatchers("/api/orders").hasRole("USER")  // ¿Requiere rol USER?
        // o
        .antMatchers("/api/orders").hasRole("ADMIN")  // ¿Requiere rol ADMIN?
}
```

---

### 2. **Token JWT sin el Rol Correcto**

El token JWT podría no tener el rol necesario.

**Verificar el token JWT**:
```bash
# Decodificar el token en jwt.io
eyJhbGciOiJIUzM4NCJ9.eyJpc0FkbWluIjpmYWxzZSwidXNlcklkIjoyLCJlbWFpbCI6ImJhc3RpQGdtYWlsLmNvbSIsInN1YiI6ImJhc3RpQGdtYWlsLmNvbSIsImlhdCI6MTc2MzUwOTIxNSwiZXhwIjoxNzYzNTk1NjE1fQ.RhVtzxAvfb8gJWdcoSF_UUOv0TyBFhMfEiK5IQeLDZ4le0lDvLObllrjHGvvUm2Z
```

**Payload del token**:
```json
{
  "isAdmin": false,
  "userId": 2,
  "email": "basti@gmail.com",
  "sub": "basti@gmail.com",
  "iat": 1763509215,
  "exp": 1763595615
}
```

**Problema**: El token tiene `"isAdmin": false`, pero podría necesitar un campo `"role": "USER"` o similar.

---

### 3. **Endpoint Requiere Autenticación Diferente**

El microservicio de órdenes podría estar esperando:
- Un header diferente
- Un formato de token diferente
- Validación adicional

---

## 🔧 Soluciones

### Solución 1: Verificar Configuración del Microservicio (RECOMENDADO)

**En el microservicio Order Service**, verificar `SecurityConfig.java`:

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/api/orders").authenticated()  // Cambiar a authenticated()
        .anyRequest().permitAll()
        .and()
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
}
```

**O permitir el endpoint completamente**:
```java
.antMatchers("/api/orders").permitAll()
```

---

### Solución 2: Agregar Rol al Token JWT

**En el microservicio Auth Service**, al generar el token:

```java
// JwtUtil.java
public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("email", user.getEmail());
    claims.put("isAdmin", user.isAdmin());
    claims.put("role", user.isAdmin() ? "ADMIN" : "USER");  // AGREGAR ESTO
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000))
        .signWith(SignatureAlgorithm.HS384, SECRET_KEY)
        .compact();
}
```

---

### Solución 3: Verificar Filtro JWT en Order Service

**En Order Service**, verificar que el filtro JWT esté configurado:

```java
// JwtAuthenticationFilter.java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    String token = extractToken(request);
    
    if (token != null && jwtUtil.validateToken(token)) {
        String email = jwtUtil.getEmailFromToken(token);
        
        // Crear autenticación con roles
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Extraer rol del token
        String role = jwtUtil.getRoleFromToken(token);  // AGREGAR ESTO
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(email, null, authorities);
        
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    filterChain.doFilter(request, response);
}
```

---

### Solución 4: Fallback a BD Local (Temporal)

Mientras se corrige el backend, podemos hacer que la app funcione solo con BD local:

**En `CartViewModel.kt`**:
```kotlin
// Comentar temporalmente la llamada al microservicio
// val remoteResult = orderRemoteRepository.createOrder(...)

// Usar solo BD local
val localOrderId = ordenCompraDao.insert(ordenCompra)
```

---

## 🧪 Cómo Verificar

### 1. Probar el endpoint con Postman:

```http
POST http://localhost:3003/api/orders
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "userId": 2,
  "items": [
    {
      "juegoId": 44,
      "cantidad": 1
    }
  ],
  "metodoPago": "Tarjeta"
}
```

**Resultado esperado**: 200 OK o 201 Created  
**Resultado actual**: 403 Forbidden

---

### 2. Verificar logs del microservicio:

```bash
# En el servidor donde corre Order Service
tail -f logs/order-service.log
```

Buscar mensajes como:
- "Access Denied"
- "Forbidden"
- "Invalid token"
- "Missing authorities"

---

### 3. Verificar configuración de seguridad:

```bash
# En el código del microservicio
grep -r "authorizeRequests" src/main/java/
grep -r "antMatchers" src/main/java/
```

---

## 📋 Checklist de Verificación

- [ ] El microservicio Order Service está corriendo
- [ ] El endpoint `/api/orders` existe
- [ ] El endpoint permite POST
- [ ] El endpoint requiere autenticación (no ADMIN)
- [ ] El token JWT es válido
- [ ] El token JWT tiene el rol correcto
- [ ] El filtro JWT está configurado en Order Service
- [ ] No hay problemas de CORS

---

## 🎯 Solución Rápida (Para Probar)

**En el microservicio Order Service**, temporalmente permitir el endpoint:

```java
// SecurityConfig.java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/api/orders/**").permitAll()  // TEMPORAL
        .anyRequest().authenticated();
}
```

**Reiniciar el microservicio** y probar de nuevo.

---

## 📝 Logs Esperados (Cuando Funcione)

```
D/CartViewModel: Iniciando checkout para usuario 2
I/okhttp: --> POST http://10.0.2.2:3003/api/orders
I/okhttp: Authorization: Bearer eyJhbGci...
I/okhttp: <-- 201 Created
D/CartViewModel: ✅ Orden creada exitosamente: ID 123
D/CartViewModel: Limpiando carrito...
```

---

## 🔗 Documentación Relacionada

- [Spring Security Configuration](https://spring.io/guides/topicals/spring-security-architecture/)
- [JWT Authentication](https://jwt.io/)
- [HTTP Status 403](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/403)

---

**Fecha**: 18 de Noviembre de 2025  
**Versión**: 2.6  
**Estado**: ⚠️ Requiere corrección en el backend del microservicio

