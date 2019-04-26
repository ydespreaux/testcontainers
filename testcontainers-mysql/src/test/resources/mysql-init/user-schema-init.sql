CREATE DATABASE IF NOT EXISTS my_database;

USE my_database;

CREATE TABLE IF NOT EXISTS tb_user (
  id            INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
  idRh          VARCHAR(255) NOT NULL,
  first_name    VARCHAR(255) NOT NULL,
  last_name     VARCHAR(255) NOT NULL,
  last_modified DATETIME     NULL
);
