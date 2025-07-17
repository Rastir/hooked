# Implementación de Autenticación JWT en Hooked

## 📅 Fecha: 17 de Julio de 2025

## 🎯 Objetivo
Implementar un sistema completo de autenticación y autorización usando JWT (JSON Web Tokens) en el proyecto Hooked - Foro de Pesca.

## 🛠️ Tecnologías Utilizadas
- Spring Boot 3.x
- Spring Security
- JWT (com.auth0:java-jwt)
- H2 Database
- BCrypt para encriptación de contraseñas

## 📋 Cambios Implementados

### 1. Configuración de Seguridad (`SecurityConfig.java`)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // Configuración completa con:
    // - JWT Filter
    // - CORS
    // - Rutas públicas y protegidas
    // - Stateless session management
}
Rutas públicas:

/api/auth/** (login y registro)
/swagger-ui/**
/v3/api-docs/**
2. Entidad Usuario
Implementa UserDetails de Spring Security
Método getAuthorities() retorna ROLE_USER por defecto
Campos: id, nombre, email, contraseña
Relación OneToMany con Posts
3. Servicios Implementados
JwtService
generarToken(): Crea tokens con expiración de 24 horas
validarToken(): Valida y extrae información del token
Usa algoritmo HMAC256
UsuarioService
crearUsuario(): Registro con encriptación de contraseña
buscarPorEmail(): Búsqueda de usuarios
listarTodos(): Lista todos los usuarios
CustomUserDetailsService
Implementa la carga de usuarios para Spring Security
4. Filtros
JwtAuthenticationFilter
Intercepta todas las peticiones
Extrae y valida el token del header Authorization
Establece la autenticación en el SecurityContext
5. Controladores
AuthController
POST /api/auth/registro: Registro de nuevos usuarios (201 Created)
POST /api/auth/login: Autenticación de usuarios (200 OK)
UsuarioController
GET /api/usuarios: Lista todos los usuarios (requiere autenticación)
GET /api/usuarios/{email}: Busca usuario por email
POST /api/usuarios: Crea nuevo usuario
🧪 Pruebas con Insomnia
1. Registro de Usuario
http
POST http://localhost:8080/api/auth/registro
Content-Type: application/json

{
  "nombre": "Test User",
  "email": "test@example.com",
  "contrasena": "password123"
}
Respuesta esperada: 201 Created

json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "id": 1,
  "email": "test@example.com",
  "nombre": "Test User"
}
2. Login
http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
Respuesta esperada: 200 OK (con token JWT)

3. Acceso a Endpoint Protegido
http
GET http://localhost:8080/api/usuarios
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Respuesta esperada: 200 OK con lista de usuarios

🐛 Problemas Resueltos
Error 403 Forbidden

Causa: Usuario sin authorities
Solución: Agregar ROLE_USER por defecto
Error 405 Method Not Allowed

Causa: Falta endpoint GET en /api/usuarios
Solución: Agregar método listarUsuarios()
Token JWT inválido

Causa: Error en validación (verificaba la llave en lugar del token)
Solución: Corregir método validarToken()
Validation Error en registro

Causa: Campo esperaba "contrasena" pero recibía "password"
Solución: Usar nombre correcto o agregar @JsonProperty
📁 Estructura de Paquetes
text
com.flaco.hooked/
├── configuration/
│   └── SecurityConfig.java
├── domain/
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── UsuarioController.java
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java
│   ├── request/
│   │   ├── LoginRequest.java
│   │   └── CrearUsuarioRequest.java
│   ├── response/
│   │   └── LoginResponse.java
│   ├── service/
│   │   ├── JwtService.java
│   │   ├── UsuarioService.java
│   │   └── CustomUserDetailsService.java
│   └── usuario/
│       └── Usuario.java
└── repository/
    └── UsuarioRepository.java
✅ Estado Actual
✅ Autenticación JWT completamente funcional
✅ Registro y login de usuarios
✅ Protección de endpoints
✅ Manejo de roles básico (ROLE_USER)
✅ Integración con Spring Security
🚀 Próximos Pasos
Implementar sistema de roles más complejo (ADMIN, MODERATOR)
Agregar refresh tokens
Implementar logout
Agregar validaciones adicionales
Implementar recuperación de contraseña
📌 Notas Importantes
La clave secreta JWT debe configurarse en application.properties
Los tokens expiran en 24 horas
Zona horaria configurada: Cancún (UTC-5)
Contraseñas encriptadas con BCrypt