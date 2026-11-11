# kanban-backend

Backend **terminado** del Tablero Kanban · Java 21 · Spring Boot 3.5 · PostgreSQL 18
IPSS · Desarrollo de Software II · Clase 03

API REST con arquitectura **MVC en capas**, en Java puro (**sin Lombok**), que gestiona tareas en tres columnas:
**Por hacer → En curso → Hecha**. Es la referencia para comparar con lo que construyes desde
[kanban_backend_base](https://github.com/cbarralesp/kanban_backend_base) siguiendo la
[guía completa](https://github.com/cbarralesp/Kanban_docs/blob/main/GUIA-COMPLETA-PASO-A-PASO.md).

## Reglas de negocio (en el service)

1. Máximo **3 tareas en `EN_CURSO`**: la cuarta devuelve **409**.
2. Solo se mueve **una columna a la vez** (se puede retroceder): saltar de `POR_HACER` a `HECHA` devuelve **409**.
3. Una tarea **`HECHA` no se edita** (**409**), salvo moverla.

## Endpoints

| Verbo | Ruta | Acción | Éxito |
|---|---|---|---|
| GET | `/api/tareas` | Listar | 200 |
| GET | `/api/tareas/{id}` | Obtener | 200 |
| POST | `/api/tareas` | Crear (nace `POR_HACER`) | 201 + `Location` |
| PUT | `/api/tareas/{id}` | Editar título, descripción, prioridad, fecha | 200 |
| PATCH | `/api/tareas/{id}/estado` | Mover entre columnas | 200 |
| DELETE | `/api/tareas/{id}` | Eliminar | 204 |

Todos los errores responden con el mismo formato `ApiError` (`fecha`, `status`, `error`, `mensaje`, `detalles`).

## Estructura

```
src/main/java/cl/ipss/kanban
├── enums        EstadoTarea, PrioridadTarea
├── entity       Tarea
├── repository   TareaRepository
├── dto          CrearTareaRequest, ActualizarTareaRequest, MoverTareaRequest, TareaResponse
├── exception    4 excepciones, ApiError, GlobalExceptionHandler
├── mapper       TareaMapper
├── service      TareaService  ·  impl/TareaServiceImpl
├── config       CorsConfig, SwaggerConfig
└── controller   TareaController
```

## Ejecutar

Requiere **JDK 21** y PostgreSQL 18.

1. Crea el usuario y la base (una vez, como `postgres`):

   ```powershell
   psql -U postgres -h localhost -p 5432 -f crear-bd.sql
   ```

2. Levanta el backend:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. Abre Swagger: http://localhost:8083/swagger-ui.html

Variables (valores por defecto entre paréntesis): `DB_HOST` (localhost), `DB_PORT` (5432), `DB_NAME` (kanban_db),
`DB_USER` (kanban), `DB_PASSWORD` (kanban), `SERVER_PORT` (8083), `CORS_ORIGINS` (http://localhost:4200), `LOG_LEVEL_APP`.

## Pruebas

```powershell
.\mvnw.cmd test        # 20 pruebas: 12 del service (Mockito) y 8 web (@WebMvcTest); no necesitan base de datos
```

El frontend está en [kanban-frontend](https://github.com/cbarralesp/kanban-frontend).
