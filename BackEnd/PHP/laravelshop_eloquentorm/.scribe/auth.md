# Authenticating requests

To authenticate requests, include an **`Authorization`** header with the value **`"Bearer {YOUR_AUTH_KEY}"`**.

All authenticated endpoints are marked with a `requires authentication` badge in the documentation below.

인증이 필요한 엔드포인트는 `Authorization: Bearer {token}` 헤더를 사용합니다.
