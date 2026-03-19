create database IF NOT EXISTS shop37;

GRANT ALL PRIVILEGES ON shop37.* TO 'project_bible'@'localhost';
FLUSH PRIVILEGES;

show databases;

CREATE DATABASE shop37 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON shop37.* TO 'project_bible'@'%';
FLUSH PRIVILEGES;

show USER;

SELECT CURRENT_USER(), USER();

SHOW GRANTS FOR 'project_bible'@'%';
SHOW GRANTS FOR 'project_bible'@'localhost';

GRANT ALL PRIVILEGES ON `shop37`.* TO `project_bible`@`%`

GRANT ALL PRIVILEGES ON shop37.* TO 'project_bible'@'%';
FLUSH PRIVILEGES;

GRANT ALL PRIVILEGES ON shop37.* TO 'project_bible'@'localhost';
FLUSH PRIVILEGES;

CREATE USER 'project_bible'@'%' IDENTIFIED BY 'project_bible';
CREATE USER 'project_bible'@'localhost' IDENTIFIED BY 'project_bible';


GRANT ALL PRIVILEGES ON shop37.* TO 'project_bible'@'%';
GRANT ALL PRIVILEGES ON shop37.* TO 'project_bible'@'localhost';
FLUSH PRIVILEGES;
SHOW GRANTS FOR 'project_bible'@'%';
SHOW GRANTS FOR 'project_bible'@'localhost';

use shop37;