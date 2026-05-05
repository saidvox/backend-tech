# TechStore Pro Backend

Backend Spring Boot para el mini e-commerce TechStore Pro.

## Stack

- Java 21
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA
- H2 en memoria para desarrollo
- SpringDoc OpenAPI / Swagger UI
- Maven Wrapper

## Estructura

El backend esta organizado por dominio y cada dominio separa su responsabilidad interna:

```text
src/main/java/com/techstore/backend
|-- auth
|   |-- api              # Controladores y DTOs
|   `-- application      # Casos de uso de autenticacion
|-- cart
|   |-- api
|   |-- application
|   |-- domain
|   `-- infrastructure
|-- product
|   |-- api
|   |-- application
|   |-- domain
|   `-- infrastructure
|-- order
|   |-- api
|   |-- application
|   |-- domain
|   `-- infrastructure
|-- user
|   |-- api
|   |-- domain
|   `-- infrastructure
|-- config              # Security, OpenAPI, propiedades y seed data
`-- common              # Excepciones y respuestas de error
```

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

API local:

```text
http://localhost:8080
```

Consola H2:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:techstore
User: sa
Password:
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## PostgreSQL con Docker

El backend detecta PostgreSQL automaticamente cuando no defines un perfil explicito:

- Si PostgreSQL responde con las credenciales de `.env`, usa PostgreSQL.
- Si PostgreSQL no responde, usa H2 en memoria.
- Si defines `SPRING_PROFILES_ACTIVE`, se respeta ese perfil y no se aplica la deteccion automatica.

Para usar PostgreSQL local en Docker:

1. Crear el archivo local de variables:

```powershell
Copy-Item .env.example .env
```

2. Levantar PostgreSQL:

```powershell
docker compose up -d
```

3. Ejecutar el backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Si quieres forzar PostgreSQL sin autodeteccion:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
.\mvnw.cmd spring-boot:run
```

Si quieres desactivar la autodeteccion y usar H2 aunque Docker este prendido:

```powershell
$env:APP_DATASOURCE_AUTO_DETECT="false"
.\mvnw.cmd spring-boot:run
```

Comandos utiles:

```powershell
docker compose ps
docker compose logs -f postgres
docker compose down
```

Para borrar tambien los datos persistidos de PostgreSQL:

```powershell
docker compose down -v
```

## Usuarios Demo

```text
Admin:
email: admin@techstore.com
password: admin123

Cliente:
email: cliente@techstore.com
password: cliente123
```

## Endpoints

### Auth

```http
POST /auth/register
POST /auth/login
```

Body de login:

```json
{
  "email": "cliente@techstore.com",
  "password": "cliente123"
}
```

La respuesta incluye un JWT. Para endpoints protegidos:

```http
Authorization: Bearer <token>
```

### Google OAuth2

El login con Google usa variables de entorno. No guardar el Client Secret en el repositorio.

Variables:

```powershell
$env:GOOGLE_CLIENT_ID="<tu-client-id>"
$env:GOOGLE_CLIENT_SECRET="<tu-client-secret>"
$env:OAUTH2_SUCCESS_REDIRECT_URI="http://localhost:4200/auth/oauth2/success"
$env:OAUTH2_FAILURE_REDIRECT_URI="http://localhost:4200/login"
```

Tambien puedes poner esas variables en `.env`. El backend importa automaticamente ese archivo cuando se ejecuta desde la carpeta `backend` o desde la carpeta padre del proyecto.

En Google Cloud, el redirect URI autorizado debe ser:

```text
http://localhost:8080/login/oauth2/code/google
```

Para iniciar el flujo desde Angular, redirigir el navegador a:

```text
http://localhost:8080/oauth2/authorization/google
```

Si Google autentica correctamente, el backend crea o reutiliza el usuario por email, genera el JWT propio del sistema y redirige al frontend:

```text
http://localhost:4200/auth/oauth2/success?token=<jwt>&tokenType=Bearer
```

El frontend debe tomar ese `token` y usarlo igual que el token devuelto por `POST /auth/login`.

### Productos

```http
GET /productos?page=0&size=10&sort=name,asc
GET /productos?q=mouse&category=Mouse&minPrice=50&maxPrice=120&stockStatus=IN_STOCK&page=0&size=10
GET /productos?includeInactive=true&active=false&page=0&size=10   ADMIN
GET /productos/{id}
POST /productos            ADMIN
PUT /productos/{id}        ADMIN
DELETE /productos/{id}     ADMIN
```

`GET /productos` devuelve una pagina. La respuesta incluye `content`, `totalElements`, `totalPages`, `size` y `page`.

Filtros disponibles:

```text
q               Busca por nombre o descripcion
category        Categoria exacta
minPrice        Precio minimo
maxPrice        Precio maximo
stockStatus     IN_STOCK, OUT_OF_STOCK o LOW_STOCK
active          true/false, solo administradores
includeInactive true/false, solo administradores
createdFrom     Fecha inicial yyyy-MM-dd
createdTo       Fecha final yyyy-MM-dd
page,size,sort  Paginacion y ordenamiento
```

Body para crear/editar producto:

```json
{
  "name": "Teclado Gamer",
  "category": "Teclados",
  "description": "Teclado mecanico compacto",
  "price": 149.9,
  "stock": 15,
  "active": true
}
```

### Carrito

```http
GET /carrito
PUT /carrito/items/{productId}
DELETE /carrito/items/{productId}
DELETE /carrito/items
```

Body para crear o reemplazar la cantidad:

```json
{
  "quantity": 2
}
```

### Pedidos

```http
POST /pedidos
GET /pedidos?page=0&size=10
GET /pedidos?status=CONFIRMED&productName=mouse&minTotal=50&maxTotal=120&page=0&size=10
GET /pedidos?scope=all&userEmail=cliente&from=2026-05-01&to=2026-05-31&page=0&size=10   ADMIN
GET /pedidos/{id}
PATCH /pedidos/{id}/status   ADMIN
```

`POST /pedidos` confirma el carrito actual, valida stock, descuenta inventario y limpia el carrito.
`PATCH /pedidos/{id}/status` permite cambiar el estado del pedido. Al cancelar devuelve stock; al reconfirmar valida stock y lo descuenta.

Filtros disponibles:

```text
scope           mine o all. all solo administradores
status          CONFIRMED o CANCELLED
userName        Busca por nombre de usuario, solo administradores
userEmail       Busca por email de usuario, solo administradores
productName     Busca pedidos que contengan un producto por nombre
productId       Busca pedidos que contengan un producto por id
from            Fecha inicial yyyy-MM-dd
to              Fecha final yyyy-MM-dd
minTotal        Total minimo
maxTotal        Total maximo
page,size,sort  Paginacion y ordenamiento
```

Body para cambiar estado:

```json
{
  "status": "CANCELLED"
}
```

## Validar

```powershell
.\mvnw.cmd test
```
