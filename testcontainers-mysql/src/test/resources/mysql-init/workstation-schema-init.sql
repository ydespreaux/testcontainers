CREATE DATABASE IF NOT EXISTS an_springboot_aa;

USE an_springboot_aa;

CREATE TABLE IF NOT EXISTS tb_workstation (
  id            INT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(255) NOT NULL,
  serial_number    VARCHAR(255) NOT NULL
);

INSERT INTO tb_workstation (name, serial_number) VALUES ('WS10002','WS-1234-5678');
