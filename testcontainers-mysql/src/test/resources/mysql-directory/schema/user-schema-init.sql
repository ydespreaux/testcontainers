CREATE DATABASE IF NOT EXISTS an_springboot_aa;

USE an_springboot_aa;

CREATE TABLE IF NOT EXISTS tb_user (
  id            INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
  idRh          VARCHAR(255) NOT NULL,
  first_name    VARCHAR(255) NOT NULL,
  last_name     VARCHAR(255) NOT NULL,
  last_modified DATETIME     NULL
);
