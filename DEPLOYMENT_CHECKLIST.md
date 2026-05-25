# Deployment Checklist

Use this before deploying TGrowth Pro to production.

## Required backend values

Set these in `.env.production` on the server:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL` to the real MySQL host
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` with at least 64 characters
- `ALLOWED_ORIGINS` with the real HTTPS frontend domains
- `AI_BASE_URL=https://openrouter.ai/api/v1/chat/completions`
- `AI_MODEL=deepseek/deepseek-chat-v3-0324:free`
- `AI_API_KEY` with a real OpenRouter key
- `RABBITMQ_DEFAULT_USER`
- `RABBITMQ_DEFAULT_PASS`
- `WEBSOCKET_BROKER_RELAY_CLIENT_PASSCODE`
- `WEBSOCKET_BROKER_RELAY_SYSTEM_PASSCODE`

## Required frontend values

Set these in `FE/.env.production` or as build args:

- `VITE_API_BASE_URL` to the public API origin
- `VITE_WS_URL` to the public websocket origin

## Safe production defaults

Keep these as-is for the first deploy:

- `FLYWAY_ENABLED=false`
- `JPA_DDL_AUTO=update`
- `SWAGGER_ENABLED=false`
- `RATE_LIMIT_ENABLED=true`
- `WEBSOCKET_BROKER_RELAY_ENABLED=true`
- `REDIS_HEALTH_ENABLED=true`

## Infra to prepare

- MySQL database reachable from the backend container
- Redis container or service
- RabbitMQ STOMP broker
- Public HTTPS domain for the frontend
- Public HTTPS domain for the backend API and websocket
- Writable Docker volume for `/app/uploads`

## Before going live

1. Rotate any secrets that were previously shared or stored outside the target environment.
2. Verify the backend starts with `prod` profile and reaches `/actuator/health`.
3. Verify the frontend build uses the real API and websocket URLs.
4. Verify the AI widget can call `/api/ai/chat` after login.
5. Verify mock exam uploads still write to `/uploads/mock-questions`.
