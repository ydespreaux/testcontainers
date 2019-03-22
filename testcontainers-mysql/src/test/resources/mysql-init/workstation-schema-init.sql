CREATE DATABASE IF NOT EXISTS my_database;

USE my_database;

CREATE TABLE IF NOT EXISTS tb_workstation (
  id            INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(255) NOT NULL,
  serial_number    VARCHAR(255) NOT NULL
);

INSERT INTO tb_workstation (name, serial_number) VALUES ('WS10002','WS-1234-5678');
