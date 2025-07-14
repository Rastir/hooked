# HOOKED - Estado del Proyecto

## Descripción
Foro de pesca donde los usuarios pueden crear posts, compartir fotos, comentar y dar likes.

## Tecnologías
- Spring Boot 3.x
- MySQL
- Spring Security + JWT
- Flyway
- Lombok (no funciona, usando getters/setters manuales)
- SpringDoc OpenAPI (Swagger)

## Estructura de Paquetes

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
│   └── controller/
│       ├── UsuarioController.java
│       ├── CategoriaController.java
│       └── PostController.java
└── repository/
├── UsuarioRepository.java
├── CategoriaRepository.java
└── PostRepository.java


## Endpoints Implementados
### Usuarios
- POST /api/usuarios - Crear usuario
- [Pendiente: GET, PUT, DELETE]

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

## Base de Datos
- Nombre: hooked
- Tablas: usuarios, categorias, posts
- Migraciones: V1__create_initial_tables.sql

## Categorías Creadas
1. Pesca de mar
2. Pesca de agua dulce
3. Equipos y aparejos
4. Capturas del día
5. Técnicas y consejos

## Pendientes
- [ ] Sistema de comentarios
- [ ] Sistema de likes
- [ ] Autenticación JWT completa
- [ ] Encriptar contraseñas
- [ ] Upload de imágenes real
- [ ] Perfil de usuario con "mejor captura"
- [ ] Sistema de puntos de pesca
- [ ] Frontend

## Notas Importantes
- @JsonIgnore en las listas de posts (Usuario y Categoria) para evitar bucles
- Spring Security permite todos los endpoints por ahora
- Lombok no funciona, usando getters/setters manuales

## Último Estado
- Fecha: 13 de Julio 2025
- Último cambio: Post creados : 2(uno sobre truchas y otro sobre curvinas)