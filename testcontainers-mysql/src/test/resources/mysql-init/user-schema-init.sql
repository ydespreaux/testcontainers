CREATE DATABASE IF NOT EXISTS my_database;

USE my_database;

CREATE TABLE IF NOT EXISTS tb_user (
  id            INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
  idRh          VARCHAR(255) NOT NULL,
  first_name    VARCHAR(255) NOT NULL,
  last_name     VARCHAR(255) NOT NULL,
  last_modified DATETIME     NULL
);

INSERT INTO tb_user (id, idRh, first_name, last_name, last_modified) VALUES (1,'XPAX624','Jean','Dupond',NOW());
