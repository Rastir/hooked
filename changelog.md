# Changelog — Hooked API

Todos los cambios notables de este proyecto se documentan aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es/1.0.0/).

---

## [Unreleased]

## [1.1.0] — 2026-04-20

### Fixed
- `GET /api/posts/{id}` no recibía `Authentication`, devolviendo siempre
  `likedByCurrentUser = null` — ahora recibe el JWT y pasa el `userId`
  al servicio para calcular correctamente el estado del like
- `PostService.obtenerPostPorId()` ahora acepta `Long usuarioId` como
  parámetro en lugar de pasar `null` fijo a `convertirAResponse()`
- Headers `Cache-Control` en `UsuarioController` y `PostController`
  cambiados de `public/private, max-age=N` a `no-store, no-cache,
  must-revalidate` en los endpoints de perfil, mis-posts y posts por
  usuario — evita que el navegador sirva likes y datos desactualizados

---

## [1.0.0] — 2026-02-25

### Added
- Autenticación JWT con access token (15 min) y refresh token (30 días)
- Gestión de sesiones: máximo 2 dispositivos, limpieza automática cada 24h
- CRUD completo de Posts con paginación, filtros por categoría y búsqueda
- Sistema de Likes con toggle y sincronización forzada a BD (`flush()`)
- Comentarios anidados en 2 niveles con paginación completa
- Perfiles de usuario con estadísticas calculadas en tiempo real
- Nivel de pescador automático: Principiante / Intermedio / Experto
- Upload de imágenes a Cloudinary con transformaciones
- Búsqueda multicampo de usuarios: nombre, email, ubicación, tags
- Optimización N+1 con `@EntityGraph` en todos los repositories
- Índices de BD optimizados para búsquedas frecuentes
- Migraciones con Flyway
- Documentación Swagger UI en `/swagger-ui.html`
- Paginación de categorías `GET /api/categorias/paginadas`