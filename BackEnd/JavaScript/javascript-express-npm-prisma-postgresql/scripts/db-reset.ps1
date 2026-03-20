$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$repoRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $projectRoot))
$databaseRoot = Join-Path $repoRoot "Database\postgresql"
$container = if ($env:DB_CONTAINER) { $env:DB_CONTAINER } else { "projectbible-postgres" }
$dbName = if ($env:DB_NAME) { $env:DB_NAME } else { "pbdb" }
$dbUser = if ($env:DB_USER) { $env:DB_USER } else { "project_bible" }

$schemaFile = Join-Path $databaseRoot "postgres_table.sql"
$sampleFile = Join-Path $databaseRoot "sample_data.sql"
$schemaTarget = "/tmp/pb-postgres-table.sql"
$sampleTarget = "/tmp/pb-sample-data.sql"

Write-Host "Dropping and recreating public schema in $dbName"
docker exec $container psql -U $dbUser -d $dbName -c "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public;"
if ($LASTEXITCODE -ne 0) {
  throw "Failed to reset public schema."
}

Write-Host "Reapplying shared PostgreSQL schema"
docker cp $schemaFile "${container}:$schemaTarget"
docker exec $container psql -U $dbUser -d $dbName -f $schemaTarget
if ($LASTEXITCODE -ne 0) {
  throw "Failed to apply shared schema after reset."
}

Write-Host "Reapplying shared PostgreSQL sample data"
docker cp $sampleFile "${container}:$sampleTarget"
docker exec $container psql -U $dbUser -d $dbName -f $sampleTarget
if ($LASTEXITCODE -ne 0) {
  throw "Failed to apply shared sample data after reset."
}

Write-Host "Database reset completed."
