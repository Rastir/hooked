# 🎣 HOOKED - Foro de Pesca

Un foro completo para pescadores desarrollado con Spring Boot donde puedes compartir tus aventuras de pesca, técnicas y conectar con otros aficionados.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![JWT](https://img.shields.io/badge/JWT-Auth-blue)
![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow)

## 🐟 ¿Qué es HOOKED?

HOOKED es una plataforma donde los pescadores pueden:
- 📝 **Compartir historias** de sus jornadas de pesca
- 📸 **Mostrar fotos** de sus mejores capturas
- 🏷️ **Organizar contenido** por categorías (río, mar, lago, etc.)
- 👍 **Dar likes** a las mejores historias
- 🔍 **Buscar y filtrar** contenido por tipo de pesca

## ✨ Características Principales

### 🔐 Autenticación Segura
- Registro e inicio de sesión con JWT
- Contraseñas encriptadas con BCrypt
- Sesiones seguras de 24 horas

### 📱 API REST Completa
- Endpoints organizados y documentados
- Respuestas optimizadas para frontend
- Códigos HTTP correctos

### 👍 Sistema de Likes Único
- Un like por usuario por post
- Contadores en tiempo real
- Prevención de likes duplicados

### 🏷️ Sistema de Categorías
- Organiza posts por tipo de pesca
- CRUD completo con validaciones
- Búsqueda inteligente

## 🚀 Tecnologías Utilizadas

| Tecnología | Propósito |
|------------|-----------|
| **Spring Boot 3.x** | Framework principal |
| **Spring Security 6** | Autenticación y autorización |
| **Spring Data JPA** | Base de datos |
| **JWT (Auth0)** | Tokens de autenticación |
| **H2/MySQL** | Base de datos |
| **BCrypt** | Encriptación de contraseñas |
| **Maven** | Gestión de dependencias |

## 📁 Estructura del Proyecto

```
com.flaco.hooked/
├── 🔧 configuration/          # Configuración de seguridad
├── 🌐 controller/            # Controladores REST
├── 🛡️  filter/               # Filtros JWT
├── 📝 request/               # DTOs de entrada
├── 📤 response/              # DTOs de salida
├── ⚙️  service/              # Lógica de negocio
└── 💾 entities/              # Modelos de datos
    ├── usuario/              # Gestión de usuarios
    ├── post/                 # Posts del foro
    ├── categoria/            # Categorías de pesca
    └── like/                 # Sistema de likes
```

## 🛠️ Instalación y Uso

### Requisitos Previos
- Java 17 o superior
- Maven 3.6+
- MySQL 8.0+ (opcional, usa H2 por defecto)

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/hooked.git
cd hooked
```

### 2. Instalar dependencias
```bash
mvn clean install
```

### 3. Configurar base de datos (opcional)
```properties
# application.properties para MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/hooked
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

### 4. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📚 Endpoints Principales

### 🔐 Autenticación
```http
POST /api/auth/registro    # Registrarse
POST /api/auth/login       # Iniciar sesión
```

### 📝 Posts
```http
GET    /api/posts           # Ver todos los posts
POST   /api/posts           # Crear post (requiere login)
GET    /api/posts/{id}      # Ver post específico
PUT    /api/posts/{id}      # Editar post (solo autor)
DELETE /api/posts/{id}      # Eliminar post (solo autor)
```

### 👍 Likes
```http
POST   /api/posts/{id}/like    # Dar like
DELETE /api/posts/{id}/like    # Quitar like
```

### 🏷️ Categorías
```http
GET    /api/categorias         # Ver categorías
POST   /api/categorias         # Crear categoría (requiere login)
GET    /api/categorias/{id}/posts  # Posts de una categoría
```

## 🧪 Ejemplos de Uso

### Registro de Usuario
```json
POST /api/auth/registro
{
  "nombre": "Carlos Pescador",
  "email": "carlos@ejemplo.com",
  "contrasena": "miPassword123"
}
```

### Crear un Post
```json
POST /api/posts
Authorization: Bearer tu-jwt-token
{
  "titulo": "Increíble robalo en Cancún",
  "contenido": "Hoy tuve una jornada espectacular...",
  "categoriaId": 1,
  "fotoLink": "https://mi-foto.com/robalo.jpg"
}
```

## 🎯 Estado Actual

### ✅ Completado
- ✅ Autenticación JWT completa
- ✅ CRUD de posts con autorización
- ✅ Sistema de likes único
- ✅ Gestión completa de categorías
- ✅ Búsqueda y filtrado
- ✅ API REST documentada

### 🚧 En Desarrollo
- 🔄 Sistema de comentarios
- 🔄 Paginación de resultados
- 🔄 Upload de imágenes directo
- 🔄 Dashboard de estadísticas

### 📋 Próximas Funcionalidades
- 💬 Sistema de comentarios
- 🏷️ Tags/etiquetas para posts
- 🔔 Notificaciones
- 📊 Dashboard de estadísticas
- 📱 Aplicación móvil

## 🤝 Contribuir

1. Fork el proyecto
2. Crea tu rama: `git checkout -b feature/nueva-funcionalidad`
3. Commit tus cambios: `git commit -m 'Agregar nueva funcionalidad'`
4. Push a la rama: `git push origin feature/nueva-funcionalidad`
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver `LICENSE` para más detalles.

## 👨‍💻 Autor

**Flaco** - *Desarrollo completo* - [Tu GitHub](https://github.com/rastir)

---

### 🎣 ¿Te gusta pescar? ¡Este foro es para ti!

**HOOKED** - Donde cada historia de pesca importa
