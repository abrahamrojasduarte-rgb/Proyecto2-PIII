DROP DATABASE IF EXISTS reservas;
CREATE DATABASE reservas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE reservas;

CREATE TABLE funcionario (
    id        VARCHAR(20)  PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    clave     VARCHAR(100) NOT NULL,
    rol       ENUM('ADMINISTRADOR','FUNCIONARIO') NOT NULL DEFAULT 'FUNCIONARIO'
);

CREATE TABLE categoria (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE recurso (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    codigo        VARCHAR(30)  NOT NULL UNIQUE,
    descripcion   VARCHAR(150) NOT NULL,
    categoria_id  INT NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE TABLE reserva (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    descripcion   VARCHAR(150) NOT NULL,
    encargado_id  VARCHAR(20)  NOT NULL,
    fecha         DATE NOT NULL,
    hora_desde    TIME NOT NULL,
    hora_hasta    TIME NOT NULL,
    FOREIGN KEY (encargado_id) REFERENCES funcionario(id)
);

CREATE TABLE reserva_recurso (
    reserva_id  INT NOT NULL,
    recurso_id  INT NOT NULL,
    PRIMARY KEY (reserva_id, recurso_id),
    FOREIGN KEY (reserva_id) REFERENCES reserva(id) ON DELETE CASCADE,
    FOREIGN KEY (recurso_id) REFERENCES recurso(id)
);

CREATE TABLE actividad (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    encargado_id  VARCHAR(20)  NOT NULL,
    fecha         DATE NOT NULL,
    hora_desde    TIME NOT NULL,
    hora_hasta    TIME NOT NULL,
    FOREIGN KEY (encargado_id) REFERENCES funcionario(id)
);

-- Datos iniciales para poder probar el login
INSERT INTO funcionario (id, nombre, clave, rol)
VALUES ('111', 'Administrador', '111', 'ADMINISTRADOR'),
       ('222', 'Maria Mata', '222', 'FUNCIONARIO');