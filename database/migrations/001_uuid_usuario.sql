-- ============================================================
-- Kinal RH - Migración de Base de Datos
-- Tarea: T1.21 · HU US-34 (ampliación)
-- Responsable: Zabala
-- Propósito: Agregar uuid_usuario a la tabla usuario, garantizando
--            que sea aleatorio, único e inmutable por cuenta.
-- ============================================================

USE IN4CM;

-- FASE 1: Agregar la columna uuid_usuario permitiendo NULL temporalmente
-- para dar lugar al script de migración en Java (UuidUsuarioMigracion)
-- que poblará los usuarios ficticios existentes con UUID.randomUUID().
ALTER TABLE usuario 
ADD COLUMN uuid_usuario VARCHAR(36) NULL AFTER id_usuario;

-- ============================================================
-- [PASO INTERMEDIO]:
-- Ejecutar org.kinalrh.util.UuidUsuarioMigracion para asignar UUIDs
-- a todas las cuentas existentes (encargado_demo, visor_demo, etc.).
-- ============================================================

-- FASE 2: Exigir valor no nulo (NOT NULL) una vez poblada la información.
ALTER TABLE usuario 
MODIFY COLUMN uuid_usuario VARCHAR(36) NOT NULL;

-- FASE 3: Crear restricción e índice UNIQUE para evitar duplicados.
ALTER TABLE usuario 
ADD CONSTRAINT uq_usuario_uuid UNIQUE (uuid_usuario);
