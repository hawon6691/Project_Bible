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

if (-not (Test-Path $schemaFile)) {
  throw "Schema file not found: $schemaFile"
}

if (-not (Test-Path $sampleFile)) {
  throw "Sample data file not found: $sampleFile"
}

Write-Host "Applying shared PostgreSQL schema from $schemaFile"
docker exec $container psql -U $dbUser -d $dbName -tAc "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'users');" | Out-File -FilePath "$env:TEMP\pb-users-table-check.txt" -Encoding ascii
$usersTableExists = (Get-Content "$env:TEMP\pb-users-table-check.txt" -Raw).Trim()
Remove-Item "$env:TEMP\pb-users-table-check.txt" -ErrorAction SilentlyContinue

if ($usersTableExists -eq "t") {
  throw "Shared schema already exists in '$dbName'. Use 'npm run db:reset' instead of 'npm run db:init'."
}

docker cp $schemaFile "${container}:$schemaTarget"
docker exec $container psql -U $dbUser -d $dbName -f $schemaTarget
if ($LASTEXITCODE -ne 0) {
  throw "Failed to apply shared schema."
}

Write-Host "Applying shared PostgreSQL sample data from $sampleFile"
docker cp $sampleFile "${container}:$sampleTarget"
docker exec $container psql -U $dbUser -d $dbName -f $sampleTarget
if ($LASTEXITCODE -ne 0) {
  throw "Failed to apply shared sample data."
}

Write-Host "Shared PostgreSQL schema and sample data applied successfully."
