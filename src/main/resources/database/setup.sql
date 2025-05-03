-- Script adaptado para SQLite con triggers desactivados

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    contrasenia TEXT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de tareas (movimientos, ingresos y gastos)
CREATE TABLE IF NOT EXISTS tarea (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    concepto TEXT NOT NULL,
    cantidad REAL NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado TEXT NOT NULL CHECK (estado IN ('INGRESO', 'GASTO')),
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla de estadísticas
CREATE TABLE IF NOT EXISTS estadistica (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    total_ingreso REAL DEFAULT 0,
    total_gasto REAL DEFAULT 0,
    usuario_id INTEGER NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Tabla de objetivos financieros
CREATE TABLE IF NOT EXISTS objetivo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    descripcion TEXT NOT NULL,
    cantidad_objetivo REAL NOT NULL,
    cantidad_actual REAL DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_objetivo DATE,
    completado BOOLEAN DEFAULT 0,
    usuario_id INTEGER NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Crear índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_tarea_usuario ON tarea(usuario_id);
CREATE INDEX IF NOT EXISTS idx_tarea_fecha ON tarea(fecha);
CREATE INDEX IF NOT EXISTS idx_tarea_estado ON tarea(estado);
CREATE INDEX IF NOT EXISTS idx_objetivo_usuario ON objetivo(usuario_id);
CREATE INDEX IF NOT EXISTS idx_objetivo_completado ON objetivo(completado);

-- Los triggers están comentados para evitar la doble actualización
/*
-- Trigger para cuando se inserta una nueva tarea
CREATE TRIGGER IF NOT EXISTS after_insert_tarea
AFTER INSERT ON tarea
BEGIN
    -- Verificar si existen estadísticas para este usuario
    UPDATE estadistica 
    SET 
        total_ingreso = CASE 
            WHEN NEW.estado = 'INGRESO' THEN total_ingreso + NEW.cantidad 
            ELSE total_ingreso 
        END,
        total_gasto = CASE 
            WHEN NEW.estado = 'GASTO' THEN total_gasto + NEW.cantidad 
            ELSE total_gasto 
        END
    WHERE usuario_id = NEW.usuario_id;
    
    -- Si no hay filas afectadas, significa que no existía estadística
    INSERT INTO estadistica (usuario_id, total_ingreso, total_gasto)
    SELECT 
        NEW.usuario_id,
        CASE WHEN NEW.estado = 'INGRESO' THEN NEW.cantidad ELSE 0 END,
        CASE WHEN NEW.estado = 'GASTO' THEN NEW.cantidad ELSE 0 END
    WHERE (SELECT changes() = 0);
END;

-- Trigger para cuando se actualiza una tarea
CREATE TRIGGER IF NOT EXISTS after_update_tarea
AFTER UPDATE ON tarea
WHEN OLD.cantidad <> NEW.cantidad OR OLD.estado <> NEW.estado
BEGIN
    -- Actualizar estadísticas
    UPDATE estadistica SET
        -- Si cambia de ingreso a gasto: restar del ingreso, sumar al gasto
        -- Si cambia de gasto a ingreso: restar del gasto, sumar al ingreso
        -- Si solo cambia cantidad, actualizar el total correspondiente
        total_ingreso = CASE
            WHEN OLD.estado = 'INGRESO' AND NEW.estado = 'GASTO' THEN total_ingreso - OLD.cantidad
            WHEN OLD.estado = 'GASTO' AND NEW.estado = 'INGRESO' THEN total_ingreso + NEW.cantidad
            WHEN OLD.estado = 'INGRESO' AND NEW.estado = 'INGRESO' THEN total_ingreso - OLD.cantidad + NEW.cantidad
            ELSE total_ingreso
        END,
        total_gasto = CASE
            WHEN OLD.estado = 'GASTO' AND NEW.estado = 'INGRESO' THEN total_gasto - OLD.cantidad
            WHEN OLD.estado = 'INGRESO' AND NEW.estado = 'GASTO' THEN total_gasto + NEW.cantidad
            WHEN OLD.estado = 'GASTO' AND NEW.estado = 'GASTO' THEN total_gasto - OLD.cantidad + NEW.cantidad
            ELSE total_gasto
        END
    WHERE usuario_id = NEW.usuario_id;
END;

-- Trigger para cuando se elimina una tarea
CREATE TRIGGER IF NOT EXISTS after_delete_tarea
AFTER DELETE ON tarea
BEGIN
    -- Actualizar estadísticas según el tipo de tarea eliminada
    UPDATE estadistica SET
        total_ingreso = CASE 
            WHEN OLD.estado = 'INGRESO' THEN total_ingreso - OLD.cantidad 
            ELSE total_ingreso 
        END,
        total_gasto = CASE 
            WHEN OLD.estado = 'GASTO' THEN total_gasto - OLD.cantidad 
            ELSE total_gasto 
        END
    WHERE usuario_id = OLD.usuario_id;
END;
*/

-- Insertar datos iniciales
INSERT OR IGNORE INTO usuario (id, nombre, email, contrasenia) VALUES 
(1, 'admin', 'admin@example.com', '1234');

-- Insertar estadística inicial para el usuario
INSERT OR IGNORE INTO estadistica (usuario_id, total_ingreso, total_gasto)
VALUES (1, 0, 0);

-- Insertar algunas tareas de ejemplo
INSERT OR IGNORE INTO tarea (id, concepto, cantidad, fecha, estado, usuario_id) VALUES 
(1, 'Salario', 1500.0, CURRENT_TIMESTAMP, 'INGRESO', 1),
(2, 'Alquiler', 700.0, CURRENT_TIMESTAMP, 'GASTO', 1),
(3, 'Compra supermercado', 150.0, CURRENT_TIMESTAMP, 'GASTO', 1);

-- Insertar objetivo de ejemplo
INSERT OR IGNORE INTO objetivo (id, descripcion, cantidad_objetivo, cantidad_actual, usuario_id) VALUES
(1, 'Fondo de emergencia', 3000.0, 500.0, 1);
