SELECT datname FROM pg_database ORDER BY datname;

DROP DATABASE IF EXISTS nestshop;
CREATE DATABASE nestshop;

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_type = 'BASE TABLE'
  AND table_schema NOT IN ('pg_catalog', 'information_schema')
ORDER BY table_schema, table_name;

select * from users;

UPDATE users
SET
  name = '서하원',
  phone = '01084276691',
  nickname = 'hawonoj7468',
  bio = '소개글 수정',
  profile_image_url = 'https://example.com/profile.png',
  preferred_locale = 'ko',
  preferred_currency = 'KRW',
  updated_at = NOW(),
  email_verified = true
WHERE id = 7;  -- 또는 WHERE email = '...'
