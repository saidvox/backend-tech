# Deploy backend en Coolify + Cloudflare

## Objetivo

Publicar el backend Spring Boot en Coolify usando el puerto interno `8080`, una base PostgreSQL privada y un dominio HTTPS público para Mercado Pago.

Ejemplo recomendado:

- Frontend/ngrok: `https://your-frontend.ngrok-free.app`
- Backend/API: `https://api.your-domain.com`
- Webhook Mercado Pago: `https://api.your-domain.com/pagos/webhooks/mercado-pago`

## Coolify

1. Crea un nuevo recurso para el backend desde el repositorio.
2. Usa como build context la carpeta `backend`.
3. Coolify debe detectar el `Dockerfile`.
4. Expón el puerto interno `8080`.
5. Configura un dominio para la app, por ejemplo `api.your-domain.com`.
6. Crea una base PostgreSQL administrada por Coolify o un servicio PostgreSQL privado.
7. En las variables de entorno, usa `.env.production.example` como plantilla.

Variables mínimas:

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_URL=jdbc:postgresql://<host-postgres>:5432/techstore
DB_USERNAME=techstore
DB_PASSWORD=<password>
APP_CORS_ALLOWED_ORIGINS=https://your-frontend.ngrok-free.app
JWT_SECRET=<secret-largo>
MERCADO_PAGO_MODE=sandbox
MERCADO_PAGO_ACCESS_TOKEN=<access-token>
MERCADO_PAGO_NOTIFICATION_URL=https://api.your-domain.com/pagos/webhooks/mercado-pago
MERCADO_PAGO_SUCCESS_URL=https://your-frontend.ngrok-free.app/checkout/mercado-pago/retorno
MERCADO_PAGO_FAILURE_URL=https://your-frontend.ngrok-free.app/checkout/mercado-pago/retorno
MERCADO_PAGO_PENDING_URL=https://your-frontend.ngrok-free.app/checkout/mercado-pago/retorno
```

## Cloudflare

1. Crea un registro DNS para el backend:
   - Tipo: `A`
   - Nombre: `api`
   - Contenido: IP publica del VPS
   - Proxy: activado si Coolify/Traefik tiene HTTPS correcto; desactivado primero si necesitas validar el certificado.
2. En SSL/TLS usa `Full` o `Full (strict)` cuando Traefik tenga certificado valido.
3. Verifica que `https://api.your-domain.com/health` responda:

```json
{"status":"UP"}
```

Cloudflare solo proxyfica tráfico HTTP/HTTPS para registros como `A`, `AAAA` y `CNAME`; por eso el backend debe estar detrás de Traefik/Coolify por `80/443`.

## Mercado Pago

En la aplicación de Mercado Pago configura:

- URL de notificación/webhook: `https://api.your-domain.com/pagos/webhooks/mercado-pago`
- Success URL: `https://your-frontend.ngrok-free.app/checkout/mercado-pago/retorno`
- Failure URL: `https://your-frontend.ngrok-free.app/checkout/mercado-pago/retorno`
- Pending URL: `https://your-frontend.ngrok-free.app/checkout/mercado-pago/retorno`

El backend guarda el pago como pendiente antes de redirigir. Luego el retorno/webhook sincroniza el estado.
