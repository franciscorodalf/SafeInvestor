CREATE DATABASE IF NOT EXISTS safeinvestor;
USE safeinvestor;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contrasenia VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de tareas (movimientos, ingresos y gastos)
CREATE TABLE IF NOT EXISTS tarea (
    id INT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(255) NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(10) NOT NULL,  -- 'INGRESO' o 'GASTO'
    usuario_id INT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla de estadísticas
CREATE TABLE IF NOT EXISTS estadistica (
    id INT AUTO_INCREMENT PRIMARY KEY,
    total_ingreso DECIMAL(10,2) DEFAULT 0,
    total_gasto DECIMAL(10,2) DEFAULT 0,
    usuario_id INT NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla de objetivos financieros
CREATE TABLE IF NOT EXISTS objetivo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL,
    cantidad_objetivo DECIMAL(10,2) NOT NULL,
    cantidad_actual DECIMAL(10,2) DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_objetivo DATE,
    completado BOOLEAN DEFAULT FALSE,
    usuario_id INT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Añadir índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_tarea_usuario ON tarea(usuario_id);
CREATE INDEX IF NOT EXISTS idx_tarea_fecha ON tarea(fecha);
CREATE INDEX IF NOT EXISTS idx_tarea_estado ON tarea(estado);
CREATE INDEX IF NOT EXISTS idx_objetivo_usuario ON objetivo(usuario_id);
CREATE INDEX IF NOT EXISTS idx_objetivo_completado ON objetivo(completado);

-- Insertar usuario de prueba
INSERT INTO usuario (nombre, email, contrasenia) VALUES 
('admin', 'admin@example.com', '1234');

-- Insertar estadística inicial para el usuario
INSERT INTO estadistica (total_ingreso, total_gasto, usuario_id)
VALUES (0, 0, 1);

-- Insertar algunas tareas de ejemplo
INSERT INTO tarea (concepto, cantidad, fecha, estado, usuario_id) VALUES 
('Salario', 1500.0, NOW(), 'INGRESO', 1),
('Alquiler', 700.0, NOW(), 'GASTO', 1),
('Compra supermercado', 150.0, NOW(), 'GASTO', 1);

-- Insertar objetivo de ejemplo
INSERT INTO objetivo (descripcion, cantidad_objetivo, cantidad_actual, usuario_id) VALUES
('Fondo de emergencia', 3000.0, 500.0, 1);
