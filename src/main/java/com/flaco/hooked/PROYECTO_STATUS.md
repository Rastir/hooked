# HOOKED - Estado del Proyecto

## Descripción
Foro de pesca donde los usuarios pueden crear posts, compartir fotos, comentar y dar likes.

## Tecnologías
- Spring Boot 3.x
- MySQL
- Spring Security + JWT (en progreso)
- BCrypt (implementado)
- Flyway
- Lombok (no funciona, usando getters/setters manuales)
- SpringDoc OpenAPI (Swagger)
- Bean Validation (funcionando)

## Estructura de Paquetes

```
com.flaco.hooked
├── config/
│   └── SecurityConfig.java
├── domain/
│   ├── usuario/
│   │   ├── Usuario.java
│   │   └── CrearUsuarioRequest.java
│   ├── categoria/
│   │   ├── Categoria.java
│   │   └── CrearCategoriaRequest.java
│   ├── post/
│   │   ├── Post.java
│   │   └── CrearPostRequest.java
│   ├── auth/ [NUEVO]
│   │   ├── LoginRequest.java [pendiente]
│   │   └── LoginResponse.java [pendiente]
│   └── controller/
│       ├── UsuarioController.java
│       ├── CategoriaController.java
│       └── PostController.java
├── service/ [NUEVO]
│   └── UsuarioService.java
└── repository/
    ├── UsuarioRepository.java
    ├── CategoriaRepository.java
    └── PostRepository.java
```

## Endpoints Implementados
### Usuarios
- POST /api/usuarios - Crear usuario ✅ (con validaciones y encriptación)
- GET /api/usuarios/{email} - Buscar por email ✅
- [Pendiente: PUT, DELETE]

### Categorías
- POST /api/categorias - Crear categoría
- GET /api/categorias - Listar todas
- GET /api/categorias/{id} - Obtener una categoría
- GET /api/categorias/{id}/posts - Posts de una categoría

### Posts
- POST /api/posts - Crear post
- GET /api/posts - Listar todos los posts
- GET /api/posts/{id} - Obtener un post
- GET /api/posts/usuario/{usuarioId} - Posts de un usuario

### Auth [PENDIENTE]
- POST /api/auth/login - Iniciar sesión
- POST /api/auth/registro - Registrar usuario

## Base de Datos
- Nombre: hooked
- Tablas: usuarios, categorias, posts
- Migraciones: V1__create_initial_tables.sql

## Seguridad Implementada
- ✅ BCrypt para encriptación de contraseñas
- ✅ @JsonIgnore en contraseñas
- ✅ Validaciones con Bean Validation
- ✅ Control de emails duplicados
- ⏳ JWT en progreso
- ❌ Protección de endpoints pendiente

## Validaciones Implementadas
- Email: formato válido
- Contraseña: mínimo 6 caracteres
- Nombre: entre 2 y 100 caracteres

## Categorías Creadas
1. Pesca de mar
2. Pesca de agua dulce
3. Equipos y aparejos
4. Capturas del día
5. Técnicas y consejos

## Posts Creados
1. Truchas en el río Paraná
2. Curvinas en Mar del Plata

## Pendientes
- [ ] JWT completo (LoginRequest, LoginResponse, JwtService, AuthController, JwtFilter)
- [ ] Proteger endpoints con roles
- [ ] Sistema de comentarios
- [ ] Sistema de likes
- [ ] Upload de imágenes real
- [ ] Perfil de usuario con "mejor captura"
- [ ] Sistema de puntos de pesca
- [ ] Frontend
- [ ] Endpoints PUT y DELETE para usuarios

## Notas Importantes
- @JsonIgnore en las listas de posts (Usuario y Categoria) para evitar bucles
- @JsonIgnore en contraseña funcionando correctamente
- Spring Security permite todos los endpoints temporalmente
- Lombok no funciona, usando getters/setters manuales
- BCrypt implementado y funcionando

## Cambios Recientes
- UsuarioService creado con encriptación de contraseñas
- SecurityConfig con BCryptPasswordEncoder
- Validaciones funcionando en CrearUsuarioRequest
- UsuarioRepository con métodos findByEmail y existsByEmail

## Próximo Paso
Implementar JWT:
1. Agregar dependencias JWT al pom.xml
2. Crear LoginRequest y LoginResponse
3. Implementar JwtService
4. Crear AuthController
5. Implementar JwtAuthenticationFilter

## Último Estado
- Fecha: 14 de Julio 2025
- Último cambio: Seguridad básica implementada con BCrypt y validaciones funcionando