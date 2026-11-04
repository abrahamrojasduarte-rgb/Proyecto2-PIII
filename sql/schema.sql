CREATE DATABASE IF NOT EXISTS reservas CHARACTER SET utf8mb4;
USE reservas;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS reserva_recurso;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS recurso;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS funcionario;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE funcionario (
                             id       VARCHAR(20) NOT NULL,
                             nombre   VARCHAR(100) NOT NULL,
                             telefono VARCHAR(20) NOT NULL DEFAULT '',
                             clave    VARCHAR(50) NOT NULL,
                             rol      VARCHAR(20) NOT NULL CHECK (rol IN ('FUNCIONARIO', 'ADMINISTRADOR')),
                             PRIMARY KEY (id)
);

CREATE TABLE categoria (
                           id          VARCHAR(12) NOT NULL,
                           descripcion VARCHAR(100) NOT NULL,
                           PRIMARY KEY (id)
);

-- 2. Tablas Dependientes
CREATE TABLE recurso (
                         id           VARCHAR(20) NOT NULL,
                         descripcion  VARCHAR(100) NOT NULL,
                         categoria_id VARCHAR(12) NOT NULL,
                         PRIMARY KEY (id)
);

CREATE TABLE reserva (
                         id             INT AUTO_INCREMENT NOT NULL,
                         funcionario_id VARCHAR(20) NOT NULL,
                         descripcion    VARCHAR(150) NOT NULL,
                         fecha          DATE NOT NULL,
                         desde          TIME NOT NULL,
                         hasta          TIME NOT NULL,
                         PRIMARY KEY (id),
                         CHECK (desde < hasta),
                         INDEX idx_reserva_fecha (fecha)
);

CREATE TABLE reserva_recurso (
                                 reserva_id INT NOT NULL,
                                 recurso_id VARCHAR(20) NOT NULL,
                                 PRIMARY KEY (reserva_id, recurso_id)
);

ALTER TABLE recurso
    ADD FOREIGN KEY (categoria_id) REFERENCES categoria(id);

ALTER TABLE reserva
    ADD FOREIGN KEY (funcionario_id) REFERENCES funcionario(id);

ALTER TABLE reserva_recurso
    ADD FOREIGN KEY (reserva_id) REFERENCES reserva(id) ON DELETE CASCADE,
    ADD FOREIGN KEY (recurso_id) REFERENCES recurso(id);

INSERT INTO funcionario (id, nombre, telefono, clave, rol)
VALUES ('admin', 'Administrador', '', 'admin123', 'ADMINISTRADOR');