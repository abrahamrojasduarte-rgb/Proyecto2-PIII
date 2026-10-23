CREATE DATABASE IF NOT EXISTS reservas CHARACTER SET utf8mb4;
USE reservas;

CREATE TABLE funcionario (
                             id        VARCHAR(20)  PRIMARY KEY,
                             nombre    VARCHAR(100) NOT NULL,
                             telefono  VARCHAR(20)  NOT NULL DEFAULT '',
                             clave     VARCHAR(50)  NOT NULL,
                             rol       VARCHAR(20)  NOT NULL
);

CREATE TABLE categoria (
                           id          VARCHAR(12)  PRIMARY KEY,        -- CAT-000001
                           descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE recurso (
                         id           VARCHAR(20)  PRIMARY KEY,       -- número de activo
                         descripcion  VARCHAR(100) NOT NULL,
                         categoria_id VARCHAR(12)  NOT NULL,
                         FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE TABLE reserva (
                         id             INT AUTO_INCREMENT PRIMARY KEY,
                         funcionario_id VARCHAR(20)  NOT NULL,
                         descripcion    VARCHAR(150) NOT NULL,
                         fecha          DATE NOT NULL,
                         desde          TIME NOT NULL,
                         hasta          TIME NOT NULL,
                         FOREIGN KEY (funcionario_id) REFERENCES funcionario(id)
);

CREATE TABLE reserva_recurso (
                                 reserva_id INT         NOT NULL,
                                 recurso_id VARCHAR(20) NOT NULL,
                                 PRIMARY KEY (reserva_id, recurso_id),
                                 FOREIGN KEY (reserva_id) REFERENCES reserva(id) ON DELETE CASCADE,
                                 FOREIGN KEY (recurso_id) REFERENCES recurso(id)
);

INSERT INTO funcionario (id, nombre, telefono, clave, rol)
VALUES ('admin', 'Administrador', '', 'admin123', 'ADMINISTRADOR');