$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$container = if ($env:DB_CONTAINER) { $env:DB_CONTAINER } else { "projectbible-postgres" }
$dbName = if ($env:DB_NAME) { $env:DB_NAME } else { "pbdb" }
$dbUser = if ($env:DB_USER) { $env:DB_USER } else { "project_bible" }

Write-Host "Checking PostgreSQL container: $container"
docker ps --filter "name=$container" --format "{{.Names}}" | Select-String "^$container$" | Out-Null
if ($LASTEXITCODE -ne 0) {
  throw "Container '$container' is not running. Start it with 'npm run db:docker:up'."
}

Write-Host "Checking database connection: $dbName"
docker exec $container psql -U $dbUser -d $dbName -c "SELECT current_database(), current_user;"
if ($LASTEXITCODE -ne 0) {
  throw "Failed to connect to PostgreSQL database '$dbName' as '$dbUser'."
}

Write-Host "PostgreSQL container and database are ready."
