USE IN4CM;

-- 1. Insertar un empleado inventado en estado ACTIVO
INSERT INTO empleado (
    dpi, nit, codigo_empleado,
    primer_nombre, primer_apellido,
    id_estado_empleado, fecha_ingreso_kinal
) VALUES (
    '3000123451201', '123456-7', 'EMP-TEST-01',
    'Juan', 'Ficticio',
    (SELECT id_estado_empleado FROM estado_empleado WHERE codigo = 'ACTIVO'),
    '2026-01-15'
);

-- Obtener el ID del empleado recién insertado para asociarlo al usuario
SET @id_empleado_nuevo = LAST_INSERT_ID();

-- 2. Crear cuenta ficticia 1: encargado (con rol 'encargado')
INSERT INTO usuario (
    nombre_usuario, password_hash, nombre_completo, correo, activo, id_empleado
) VALUES (
    'encargado_demo', 
    '1e58a842c6ab12101a6664d452f69bd670fca2416f4ce5be9b91aee5d1711798',
    'Encargado de Demostración', 
    'encargado.demo@kinal.edu.gt', 
    TRUE, 
    @id_empleado_nuevo
);

SET @id_usuario_encargado = LAST_INSERT_ID();

-- 3. Crear cuenta ficticia 2: visor (con rol 'visor')
INSERT INTO usuario (
    nombre_usuario, password_hash, nombre_completo, correo, activo, id_empleado
) VALUES (
    'visor_demo', 
    'cbc926e1e7a561e29084172acf99db732cdad83cc706f4998e69ed27197dcb3b',
    'Visor Institucional Demo', 
    'visor.demo@kinal.edu.gt', 
    TRUE, 
    NULL
);

SET @id_usuario_visor = LAST_INSERT_ID();

-- 4. Relacionar usuarios con sus respectivos roles (usuario_rol)
INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
(@id_usuario_encargado, (SELECT id_rol FROM rol WHERE nombre = 'encargado')),
(@id_usuario_visor, (SELECT id_rol FROM rol WHERE nombre = 'visor'));

-- 5. Asegurar asignación de permisos al rol 'encargado'
INSERT IGNORE INTO rol_permiso (id_rol, id_permiso) VALUES
(
    (SELECT id_rol FROM rol WHERE nombre = 'encargado'),
    (SELECT id_permiso FROM permiso WHERE codigo = 'EMPLEADO_VER')
);
