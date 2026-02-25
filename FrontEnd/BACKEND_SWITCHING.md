# Frontend Backend Switching

This frontend can target multiple backend implementations using env values.

## 1) Configure env

Copy `.env.local.example` to `.env.local`, then set:

- `NEXT_PUBLIC_BACKEND_TARGET` = `nest | spring | express | django | ktor`
- target URL variable (for example `NEXT_PUBLIC_API_URL_SPRING`)

## 2) Priority order

API base URL is resolved in this order:

1. Target-specific URL (`NEXT_PUBLIC_API_URL_<TARGET>`)
2. `NEXT_PUBLIC_API_URL`
3. `/api/v1`

## 3) Example

```env
NEXT_PUBLIC_BACKEND_TARGET=django
NEXT_PUBLIC_API_URL_DJANGO=http://localhost:8000/api/v1
```

After changing env, restart the dev server.
