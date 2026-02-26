SELECT datname FROM pg_database ORDER BY datname;

DROP DATABASE IF EXISTS nestshop;
CREATE DATABASE nestshop;

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_type = 'BASE TABLE'
  AND table_schema NOT IN ('pg_catalog', 'information_schema')
ORDER BY table_schema, table_name;

select * from users;