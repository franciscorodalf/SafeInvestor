-- Eliminar tablas si existen
DROP TABLE IF EXISTS tarea;
DROP TABLE IF EXISTS estadistica;
DROP TABLE IF EXISTS usuario;

-- Crear tabla usuario (necesaria para las relaciones)
CREATE TABLE usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    contrasenia TEXT NOT NULL
);

-- Crear tabla tarea
CREATE TABLE tarea (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    concepto TEXT NOT NULL,
    cantidad REAL NOT NULL CHECK (cantidad != 0),
    fecha DATE NOT NULL,
    estado TEXT NOT NULL CHECK (estado IN ('INGRESO', 'GASTO')),
    usuario_id INTEGER,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Crear tabla estadistica
CREATE TABLE estadistica (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    total_ingreso REAL NOT NULL DEFAULT 0 CHECK (total_ingreso >= 0),
    total_gasto REAL NOT NULL DEFAULT 0 CHECK (total_gasto >= 0),
    usuario_id INTEGER UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Añadir índices para mejorar el rendimiento
CREATE INDEX idx_tarea_usuario ON tarea(usuario_id);
CREATE INDEX idx_tarea_fecha ON tarea(fecha);
CREATE INDEX idx_estadistica_usuario ON estadistica(usuario_id);
