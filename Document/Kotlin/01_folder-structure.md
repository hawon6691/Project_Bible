# Kotlin Folder Structure

## Baseline Path

`BackEnd/Kotlin/kotlin-ktor-gradle-exposeddao-postgresql`

## 주요 구조

- `src/main/kotlin/.../config`: 설정 계층
- `src/main/kotlin/.../db`: DB bootstrap / health / factory
- `src/main/kotlin/.../common`: envelope / JSON helper / error helper
- `src/main/kotlin/.../security`: role / auth helper
- `src/main/kotlin/.../api`: endpoint catalog / OpenAPI generator
- `src/main/kotlin/.../plugins`: serialization / http / routing
- `src/main/kotlin/.../docs`: docs export CLI
- `src/test/kotlin/...`: domain + platform tests
- `test/performance`: perf smoke/extended assets
- `test/scripts`: migration/live smoke/stability assets
