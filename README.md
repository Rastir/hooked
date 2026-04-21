# 🎣 Hooked - Red Social para Pescadores

**Hooked** es una API REST completa para una red social especializada en pescadores. Permite a los usuarios compartir experiencias, fotos de pesca, organizarse por ubicación y nivel de experiencia, interactuar mediante likes y comentarios anidados, y gestionar perfiles personalizados.
 
---

## 📋 Tabla de Contenidos

1. [Tecnología y Stack](#-tecnología-y-stack)
2. [Estructura del Proyecto](#-estructura-del-proyecto)
3. [Arquitectura y Patrones](#-arquitectura-y-patrones)
4. [Endpoints de la API](#-endpoints-de-la-api)
5. [Configuración y Deploy](#-configuración-y-deploy)
6. [Estado de Features](#-estado-de-features)

---

## 🛠️ Tecnología y Stack

| Capa | Tecnología | Versión |
|------|-----------|---------|
| **Framework** | Spring Boot | 3.5.3 |
| **Lenguaje** | Java | 17 |
| **Build** | Maven | - |
| **Seguridad** | Spring Security + JWT (auth0) | 4.5.0 |
| **Base de datos** | MySQL (PostgreSQL ready) | 8.x |
| **Migraciones** | Flyway | - |
| **Cloud Storage** | Cloudinary | 1.34.0 |
| **Documentación** | Springdoc OpenAPI | 2.8.9 |
| **Utilidades** | Lombok | - |

### Dependencias Principales

- **Spring Boot Starter Web** - API REST
- **Spring Boot Starter Data JPA** - Persistencia
- **Spring Boot Starter Security** - Autenticación/Autorización
- **Spring Boot Starter OAuth2 Resource Server** - JWT validation
- **Spring Boot Starter Validation** - Bean Validation (Jakarta)
- **Auth0 Java JWT** - Generación/validación de tokens
- **Flyway Core + MySQL** - Migraciones de base de datos
- **Cloudinary HTTP44** - Almacenamiento de imágenes
- **Springdoc OpenAPI** - Documentación Swagger UI

---

## 📁 Estructura del Proyecto

```
com.flaco.hooked/
├── configuration/          # Configuración de seguridad y utilidades
│   ├── GlobalExceptionHandler.java   # Manejo centralizado de errores
│   ├── PaginationUtils.java          # Factory de paginación
│   └── SecurityConfig.java           # Configuración Spring Security + CORS
│
├── domain/                 # Lógica de negocio (arquitectura limpia)
│   ├── controller/         # 5 controllers REST
│   │   ├── AuthController.java
│   │   ├── CategoriaController.java
│   │   ├── ComentarioController.java
│   │   ├── PostController.java
│   │   └── UsuarioController.java
│   │
│   ├── service/            # 10 services de negocio
│   │   ├── CategoriaService.java
│   │   ├── CloudinaryStorageService.java
│   │   ├── ComentarioService.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── ImageStorageService.java (interface)
│   │   ├── JwtService.java
│   │   ├── PostService.java
│   │   ├── RefreshTokenService.java
│   │   ├── UsuarioService.java
│   │   └── UtilsService.java
│   │
│   ├── repository/           # 6 repositories JPA
│   │   ├── CategoriaRepository.java
│   │   ├── ComentarioRepository.java
│   │   ├── LikeRepository.java
│   │   ├── PostRepository.java
│   │   ├── RefreshTokenRepository.java
│   │   └── UsuarioRepository.java
│   │
│   ├── request/              # 11 DTOs de entrada (validaciones)
│   │   ├── ActualizarCategoriaRequest.java
│   │   ├── ActualizarComentarioRequest.java
│   │   ├── ActualizarPerfilRequest.java
│   │   ├── ActualizarPostRequest.java
│   │   ├── CrearCategoriaRequest.java
│   │   ├── CrearComentarioRequest.java
│   │   ├── CrearPostRequest.java
│   │   ├── CrearUsuarioRequest.java
│   │   ├── LoginRequest.java
│   │   ├── LogoutRequest.java
│   │   └── TokenRefreshRequest.java
│   │
│   ├── response/             # 8 DTOs de salida
│   │   ├── CategoriaResponse.java
│   │   ├── ComentarioResponse.java
│   │   ├── LoginResponse.java
│   │   ├── MessageResponse.java
│   │   ├── PaginatedResponse.java
│   │   ├── PostResponse.java
│   │   ├── TokenRefreshResponse.java
│   │   └── UsuarioResponse.java
│   │
│   ├── filter/               # Filtros de seguridad
│   │   └── JwtAuthenticationFilter.java
│   │
│   └── refreshtoken/         # Gestión de sesiones
│       ├── RefreshToken.java (entidad)
│       └── RefreshTokenException.java
│
├── infrastructure/           # Implementaciones de infraestructura
│   └── cloudinary/
│       └── CloudinaryImageStorageService.java
│
└── model/                    # Entidades JPA (5 entidades)
    ├── Categoria.java
    ├── Comentario.java
    ├── Like.java
    ├── Post.java
    └── Usuario.java
```
 
---

## 🏗️ Arquitectura y Patrones

### Arquitectura en Capas

El proyecto sigue una **arquitectura en capas** con separación clara de responsabilidades:

| Capa | Responsabilidad | Ejemplo |
|------|----------------|---------|
| **Controller** | Recibir HTTP, validar entrada, devolver ResponseEntity | `PostController` con headers informativos |
| **Service** | Lógica de negocio, transacciones, conversión DTOs | `UsuarioService` con cálculo de nivel pescador |
| **Repository** | Acceso a datos, queries optimizadas | `@EntityGraph` para evitar N+1 |
| **Model** | Entidades JPA, relaciones, índices | `Post` con `likeCount` denormalizado |

### Patrones Implementados

| Patrón | Implementación |
|--------|---------------|
| **DTO Pattern** | 11 Request DTOs + 8 Response DTOs separados |
| **Repository Pattern** | Interfaces Spring Data JPA con métodos custom |
| **Service Layer** | Anotación `@Transactional` con readOnly optimizado |
| **Dependency Injection** | Constructor injection en `CloudinaryImageStorageService` |
| **Strategy Pattern** | `ImageStorageService` interface con implementación Cloudinary |
| **Pagination** | `PaginatedResponse<T>` genérico wrapper de `Page<T>` |
| **Global Exception Handling** | `@RestControllerAdvice` con handlers específicos |
| **Security Filter Chain** | JWT filter antes de `UsernamePasswordAuthenticationFilter` |

### Optimizaciones de Performance

- **Índices de base de datos**: Todos los campos de búsqueda frecuente tienen índices (`@Index`)
- **EntityGraph**: Uso sistemático de `@EntityGraph` para evitar problema N+1
- **Paginación**: Todos los listados grandes usan paginación (excepto Categorías - pendiente)
- **Denormalización**: `likeCount` en `Post` para evitar count(*) frecuentes
- **Batch/Fetch size**: Configurado en `application.properties` (20/100)

---

## 🔌 Endpoints de la API

### Base URL

```
http://localhost:8080/api
```

> Todos los endpoints requieren `Bearer Token` en el header `Authorization`, excepto los marcados como **Público**.
 
---

### 🔐 Auth Controller (`/api/auth`)

| Método | Endpoint | Descripción | Acceso |
|--------|----------|-------------|--------|
| POST | `/login` | Iniciar sesión, devuelve tokens | Público |
| POST | `/registro` | Crear cuenta nueva | Público |
| POST | `/refresh` | Renovar access token | Público |
| POST | `/logout` | Cerrar sesión (revocar refresh) | Autenticado |
| POST | `/logout-all` | Cerrar todas las sesiones | Autenticado |
| GET | `/sessions` | Listar sesiones activas del usuario | Autenticado |

**Ejemplo Login:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@ejemplo.com","contrasena":"password123"}'
```

**Respuesta:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tipo": "Bearer",
  "expiresIn": 900,
  "id": 1,
  "email": "usuario@ejemplo.com",
  "nombre": "Juan Pescador"
}
```
 
---

### 📂 Categorías Controller (`/api/categorias`)

| Método | Endpoint | Descripción | Acceso | Headers de Respuesta |
|--------|----------|-------------|--------|----------------------|
| GET | `/` | Listar todas las categorías | Público | `X-Total-Categories`, `Cache-Control: max-age=600` |
| GET | `/{id}` | Obtener categoría por ID | Público | `X-Category-ID`, `Cache-Control: max-age=300` |
| POST | `/` | Crear nueva categoría | Autenticado | `X-Category-Created`, `X-Category-ID` |
| PUT | `/{id}` | Actualizar categoría | Autenticado | `X-Category-Updated` |
| DELETE | `/{id}` | Eliminar categoría (solo si sin posts) | Autenticado | `X-Category-Deleted` |
| GET | `/buscar?nombre={nombre}` | Buscar por nombre | Público | `X-Search-Term`, `X-Results-Found` |
| GET | `/stats` | Estadísticas de categorías | Público | `X-Total-Categories` |

> ⚠️ **Nota:** Listado de categorías actualmente no paginado (feature en desarrollo).
 
---

### 📝 Posts Controller (`/api/posts`)

| Método | Endpoint | Descripción | Acceso | Parámetros |
|--------|----------|-------------|--------|------------|
| GET | `/` | Listar posts paginados | Público | `?pagina=0&tamano=10&categoriaId=&buscar=` |
| GET | `/{id}` | Obtener post por ID | Público | - |
| POST | `/` | Crear nuevo post | Autenticado | Body: `CrearPostRequest` |
| PUT | `/{id}` | Actualizar post propio | Autenticado | Body: `ActualizarPostRequest` |
| DELETE | `/{id}` | Eliminar post propio | Autenticado | - |
| POST | `/{id}/like` | Toggle like (dar/quitar) | Autenticado | - |
| DELETE | `/{id}/like` | Quitar like (legacy) | Autenticado | - |
| GET | `/lista-completa` | Listar todos (sin paginar) | Público | ⚠️ Cuidado con memoria |
| GET | `/usuario/{usuarioId}` | Posts de un usuario | Público | `?pagina=0&tamano=10` |
| GET | `/mis-posts` | Posts del usuario autenticado | Autenticado | - |
| GET | `/populares` | Posts ordenados por likes | Público | `?pagina=0&tamano=10` |

**Filtros de Listado Principal (`GET /`):**

| Parámetro | Descripción | Ejemplo |
|-----------|-------------|---------|
| `categoriaId` | Filtrar por categoría | `?categoriaId=1` |
| `buscar` | Búsqueda en título y contenido | `?buscar=salmon` |
| `pagina` | Número de página (0-based) | `?pagina=0` |
| `tamano` | Elementos por página (max 100) | `?tamano=20` |

**Headers de Respuesta en Posts:**

```
X-Query-Type: list|category|search|popular|user-posts
X-Page-Number: 0
X-Page-Size: 10
X-Total-Elements: 150
X-Total-Pages: 15
X-Like-Action: liked|unliked
X-Total-Likes: 42
X-User-Liked: true|false
```
 
---

### 💬 Comentarios Controller (`/api/comentarios`)

| Método | Endpoint | Descripción | Acceso | Paginado |
|--------|----------|-------------|--------|----------|
| POST | `/` | Crear comentario | Autenticado | No |
| GET | `/{id}` | Obtener comentario | Público | No |
| PUT | `/{id}` | Actualizar comentario propio | Autenticado | No |
| DELETE | `/{id}` | Eliminar comentario propio | Autenticado | No |
| GET | `/post/{postId}` | Comentarios de un post | Público | ✅ Sí |
| GET | `/post/{postId}/principales` | Solo comentarios principales (sin respuestas) | Público | ✅ Sí |
| GET | `/usuario/{usuarioId}` | Comentarios de un usuario | Público | ✅ Sí |
| GET | `/usuario/{usuarioId}/recientes` | Comentarios recientes del usuario | Público | ✅ Sí |
| GET | `/{comentarioId}/respuestas` | Respuestas anidadas de un comentario | Público | ✅ Sí |

**Parámetros de Paginación:**

| Endpoint | Default Tamaño | Máximo | Tipo |
|----------|---------------|--------|------|
| `/post/{postId}` | 20 | 100 | `?tipo=principales` opcional |
| `/post/{postId}/principales` | 20 | 100 | Solo principales |
| `/usuario/{usuarioId}` | 20 | 50 | `?tipo=recientes` opcional |
| `/usuario/{usuarioId}/recientes` | 15 | 25 | Ordenados por fecha DESC |
| `/{id}/respuestas` | 10 | 50 | Nivel 2 de anidación |

**Sistema de Respuestas Anidadas:**

Los comentarios soportan 2 niveles de anidación:
- **Nivel 1:** Comentario principal (`comentarioPadreId = null`)
- **Nivel 2:** Respuesta a comentario (`comentarioPadreId = {id}`)

---

### 👤 Usuarios Controller (`/api/usuarios`)

| Método | Endpoint | Descripción | Acceso | Paginado |
|--------|----------|-------------|--------|----------|
| GET | `/perfil` | Perfil completo del usuario autenticado | Autenticado | No |
| PUT | `/perfil` | Actualizar perfil propio | Autenticado | No |
| POST | `/perfil/foto` | Subir foto de perfil (multipart) | Autenticado | No |
| GET | `/{id}` | Perfil público de cualquier usuario | Público | No |
| GET | `/` | Listar usuarios | Público | ✅ Sí |
| GET | `/?buscar={q}` | Buscar usuarios por nombre/email | Público | ✅ Sí |
| GET | `/especialidad/{tag}` | Usuarios por tag/especialidad | Público | ✅ Sí |
| GET | `/nivel/{nivel}` | Usuarios por nivel (Principiante/Intermedio/Experto) | Público | ✅ Sí |
| GET | `/ubicacion/{ubicacion}` | Usuarios por ubicación preferida | Público | ✅ Sí |
| GET | `/activos?dias={n}` | Usuarios activos últimos N días | Público | ✅ Sí |
| GET | `/mas-activos` | Ranking por cantidad de posts | Público | ✅ Sí |
| GET | `/nuevos?dias={n}` | Usuarios registrados recientemente | Público | ✅ Sí |
| GET | `/buscar-avanzado?q={termino}` | Búsqueda multicampo (nombre, email, ubicación, tags) | Público | ✅ Sí |
| GET | `/stats` | Estadísticas totales de usuarios | Público | No |

**Niveles de Pescador (auto-calculado):**

| Nivel | Criterio |
|-------|----------|
| **Principiante** | Default (< 250 posts y < 800 likes) |
| **Intermedio** | ≥ 250 posts o ≥ 800 likes recibidos |
| **Experto** | ≥ 500 posts o ≥ 1000 likes recibidos |

**Límites de Paginación (Usuarios):**
- Default: 10 elementos
- Máximo: 50 elementos
- Ranking (`/mas-activos`): Máximo 25

---

### 📊 Resumen de Acceso por Recurso

| Recurso | GET (lectura) | POST/PUT/DELETE (escritura) |
|---------|--------------|----------------------------|
| Auth | Público | Público (registro/login) |
| Categorías | Público | Autenticado |
| Posts | Público | Autenticado (solo propios) |
| Comentarios | Público | Autenticado (solo propios) |
| Usuarios | Público (perfil limitado) | Autenticado (solo propio) |
| Likes | Incluido en posts | Autenticado |
 
---

## ⚙️ Configuración y Deploy

### Variables de Entorno Requeridas

| Variable | Descripción | Default | Requerido |
|----------|-------------|---------|-----------|
| `DB_USER` | Usuario de base de datos | `root` | Sí (prod) |
| `DB_PASSWORD` | Contraseña de base de datos | `root` | Sí (prod) |
| `JWT_SECRET` | Clave secreta para firmar JWTs | `hooked-2025-change-in-production` | Sí (prod) |
| `CORS_ORIGINS` | Orígenes permitidos para CORS | `*` | No |
| `CLOUDINARY_CLOUD_NAME` | Nombre del cloud en Cloudinary | - | Sí (si se usa imágenes) |
| `CLOUDINARY_API_KEY` | API Key de Cloudinary | - | Sí (si se usa imágenes) |
| `CLOUDINARY_API_SECRET` | API Secret de Cloudinary | - | Sí (si se usa imágenes) |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `prod` | No |
| `PORT` | Puerto del servidor | `8080` | No |

### Configuración de Base de Datos

**MySQL (Actual):**

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hooked?useSSL=true&serverTimezone=America/Cancun&allowPublicKeyRetrieval=false
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:root}
```

**PostgreSQL (Recomendado para producción):**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hooked
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### JWT Configuration

```properties
# Access token: 15 minutos
hooked.jwt.expiration=900000
 
# Refresh token: 30 días
hooked.jwt.refresh-expiration-seconds=2592000
 
# Secret (cambiar en producción!)
api.security.token.secret=${JWT_SECRET:hooked-2025-change-in-production}
```

### CORS Configuration

```properties
# Múltiples orígenes separados por coma
cors.allowed-origins=https://tufrontend.com,https://app.hooked.com
 
# O wildcard para desarrollo (no recomendado en prod)
cors.allowed-origins=*
```
 
---

## 🐳 Docker (para DonWeb u otro hosting)

**Dockerfile:**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/hooked-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build y Run:**

```bash
# Compilar
./mvnw clean package -DskipTests
 
# Construir imagen
docker build -t hooked-api .
 
# Ejecutar con variables de entorno
docker run -p 8080:8080 \
  -e DB_USER=prod_user \
  -e DB_PASSWORD=prod_pass \
  -e JWT_SECRET=super-secret-key-change-this \
  -e CLOUDINARY_CLOUD_NAME=tu_cloud \
  -e CLOUDINARY_API_KEY=tu_key \
  -e CLOUDINARY_API_SECRET=tu_secret \
  hooked-api
```
 
---

## ✅ Estado de Features

### Features Completadas ✅

| Feature | Estado | Notas |
|---------|--------|-------|
| Autenticación JWT | ✅ Completo | Access + Refresh tokens, 15min/30días |
| Gestión de sesiones | ✅ Completo | Máximo 2 dispositivos, limpieza automática cada 24h |
| CRUD Posts | ✅ Completo | Con paginación, filtros por categoría y búsqueda |
| Sistema de Likes | ✅ Corregido | Toggle con sincronización forzada a BD |
| Comentarios anidados | ✅ Completo | 2 niveles, paginación completa |
| Perfiles de usuario | ✅ Completo | Con estadísticas calculadas en tiempo real |
| Nivel de pescador | ✅ Automático | Calculado por posts/likes recibidos |
| Upload de imágenes | ✅ Completo | Cloudinary con transformaciones |
| Paginación avanzada | ✅ Completo | Posts, comentarios, usuarios |
| Búsqueda multicampo | ✅ Completo | Usuarios: nombre, email, ubicación, tags |
| Optimización N+1 | ✅ Completo | `@EntityGraph` en todos los repositories |
| Índices de BD | ✅ Completo | Optimizados para búsquedas frecuentes |
| Migraciones Flyway | ✅ Configurado | Listo para usar (ubicación: `db/migration`) |
| Documentación API | ✅ Completo | Swagger UI en `/swagger-ui.html` |
| Paginación de Categorías | ✅ Completo | `GET /api/categorias/paginadas?pagina=0&tamano=10`. Máx. 50 elementos, ordenado por nombre A-Z |

### Features en Desarrollo 🚧

| Feature | Prioridad | Notas |
|---------|-----------|-------|
| Tests unitarios | 🟡 Media | Pendiente toda la suite de tests |
| Tests de integración | 🟡 Media | Pendiente |
| WebSocket para likes | 🟢 Baja | Likes en tiempo real |
| Notificaciones | 🟢 Baja | Sistema de notificaciones push/email |
| Frontend | 🟢 Baja | HTML/CSS/JS vanilla planificado |

### Bugs Conocidos Corregidos ✅

| Bug | Ubicación | Solución aplicada |
|-----|-----------|-------------------|
| Validación nombre categoría | `CategoriaService.java:58` | Operador lógico corregido |
| Sincronización likes | `PostService.java` | `flush()` forzado + recarga desde BD |
| Extracción de userId desde JWT | `PostController.java` | Fallback a búsqueda por email en BD |

### Pendientes de Revisión ⚠️

| Item | Ubicación | Recomendación |
|------|-----------|---------------|
| Dependencias duplicadas | `pom.xml` | Revisar `spring-security-test` y `spring-boot-starter-validation` |
| Codificación UTF-8 | `application.properties` | Verificar guardado del archivo o quitar tildes |
| API deprecada | `SecurityConfig.java` | Warning menor, funciona correctamente |
 
---

## 🚀 Comandos Útiles

```bash
# Compilar proyecto
./mvnw clean install -DskipTests
 
# Ejecutar en desarrollo
./mvnw spring-boot:run
 
# Ejecutar con perfil dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
 
# Acceder a Swagger UI
open http://localhost:8080/swagger-ui.html
 
# Health check
curl http://localhost:8080/actuator/health
 
# Probar API (ejemplos)
curl "http://localhost:8080/api/posts?pagina=0&tamano=5"
curl http://localhost:8080/api/categorias
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","contrasena":"password123"}'
```
