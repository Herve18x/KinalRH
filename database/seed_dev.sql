-- T1.16 / T1.18: datos ficticios para la demo del Sprint 1.
-- Ejecutar en la base de desarrollo IN4CM, después de crear su esquema.
-- Cada ejecución renueva las claves temporales de las dos cuentas demo.

USE IN4CM;
START TRANSACTION;

-- El esquema ya define estos roles, el permiso y el estado ACTIVO.
UPDATE rol SET activo = TRUE WHERE nombre IN ('encargado', 'visor');
UPDATE permiso SET activo = TRUE WHERE codigo = 'EMPLEADO_VER';

SET @demo_rol_encargado = (SELECT id_rol FROM rol WHERE nombre = 'encargado');
SET @demo_rol_visor = (SELECT id_rol FROM rol WHERE nombre = 'visor');
SET @demo_permiso_ver = (SELECT id_permiso FROM permiso WHERE codigo = 'EMPLEADO_VER');
SET @demo_estado_activo = (SELECT id_estado_empleado FROM estado_empleado WHERE codigo = 'ACTIVO');

-- SecurityUtil.hashSHA256() utiliza SHA-256 hexadecimal en el código actual.
-- Las claves se generan al ejecutar la semilla; no quedan fijadas en Git.
SET @demo_clave_encargado = CONCAT('Demo-', HEX(RANDOM_BYTES(12)));
SET @demo_clave_visor = CONCAT('Demo-', HEX(RANDOM_BYTES(12)));

-- Crear el empleado ficticio únicamente si todavía no existe.
INSERT INTO empleado (
    dpi, nit, codigo_empleado, primer_nombre, primer_apellido,
    nombre_completo, id_estado_empleado, fecha_ingreso_kinal
)
SELECT
    '3000123451201', '123456-7', 'EMP-TEST-01', 'Juan', 'Ficticio',
    'Juan Ficticio', @demo_estado_activo, '2026-01-15'
FROM (SELECT 1 AS semilla) AS demo
LEFT JOIN empleado AS existente ON existente.dpi = '3000123451201'
WHERE existente.id_empleado IS NULL;

UPDATE empleado
SET primer_nombre = 'Juan',
    primer_apellido = 'Ficticio',
    nombre_completo = 'Juan Ficticio',
    id_estado_empleado = @demo_estado_activo
WHERE dpi = '3000123451201';

SET @demo_empleado = (SELECT id_empleado FROM empleado WHERE dpi = '3000123451201');

-- Crear las cuentas únicamente si todavía no existen.
INSERT INTO usuario (
    nombre_usuario, password_hash, nombre_completo, correo, activo, id_empleado
)
SELECT
    'encargado_demo', SHA2(@demo_clave_encargado, 256),
    'Encargado de Demostración', 'encargado.demo@kinal.edu.gt', TRUE, @demo_empleado
FROM (SELECT 1 AS semilla) AS demo
LEFT JOIN usuario AS existente ON existente.nombre_usuario = 'encargado_demo'
WHERE existente.id_usuario IS NULL;

UPDATE usuario
SET password_hash = SHA2(@demo_clave_encargado, 256),
    nombre_completo = 'Encargado de Demostración',
    activo = TRUE,
    id_empleado = @demo_empleado
WHERE nombre_usuario = 'encargado_demo';

INSERT INTO usuario (
    nombre_usuario, password_hash, nombre_completo, correo, activo, id_empleado
)
SELECT
    'visor_demo', SHA2(@demo_clave_visor, 256),
    'Visor Institucional Demo', 'visor.demo@kinal.edu.gt', TRUE, NULL
FROM (SELECT 1 AS semilla) AS demo
LEFT JOIN usuario AS existente ON existente.nombre_usuario = 'visor_demo'
WHERE existente.id_usuario IS NULL;

UPDATE usuario
SET password_hash = SHA2(@demo_clave_visor, 256),
    nombre_completo = 'Visor Institucional Demo',
    activo = TRUE,
    id_empleado = NULL
WHERE nombre_usuario = 'visor_demo';

SET @demo_usuario_encargado = (SELECT id_usuario FROM usuario WHERE nombre_usuario = 'encargado_demo');
SET @demo_usuario_visor = (SELECT id_usuario FROM usuario WHERE nombre_usuario = 'visor_demo');

-- Cada cuenta demo recibe solamente el rol que corresponde a su prueba.
-- Esto evita que visor_demo herede EMPLEADO_VER mediante otro rol.
DELETE FROM usuario_rol
WHERE id_usuario IN (@demo_usuario_encargado, @demo_usuario_visor);

INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
(@demo_usuario_encargado, @demo_rol_encargado),
(@demo_usuario_visor, @demo_rol_visor);

-- El visor carece de este permiso; los demás permisos no se alteran.
DELETE FROM rol_permiso
WHERE id_rol = @demo_rol_visor AND id_permiso = @demo_permiso_ver;

-- Conceder EMPLEADO_VER al encargado sin duplicar la asociación.
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol AS r
JOIN permiso AS p ON p.codigo = 'EMPLEADO_VER'
LEFT JOIN rol_permiso AS existente
    ON existente.id_rol = r.id_rol AND existente.id_permiso = p.id_permiso
WHERE r.nombre = 'encargado' AND existente.id_rol IS NULL;

COMMIT;

-- RESULTADO 1: claves temporales para probar el ingreso en esta ejecución.
-- No adjuntar este resultado como evidencia ni guardar las claves en Git.
SELECT 'encargado_demo' AS usuario, @demo_clave_encargado AS clave_temporal
UNION ALL
SELECT 'visor_demo', @demo_clave_visor;

-- RESULTADO 2: asociaciones usuario_rol esperadas (exactamente dos filas).
SELECT u.nombre_usuario AS usuario, r.nombre AS rol, u.activo AS cuenta_activa
FROM usuario AS u
JOIN usuario_rol AS ur ON ur.id_usuario = u.id_usuario
JOIN rol AS r ON r.id_rol = ur.id_rol
WHERE u.nombre_usuario IN ('encargado_demo', 'visor_demo')
ORDER BY u.nombre_usuario, r.nombre;

-- RESULTADO 3: diferencia de rol_permiso para los dos roles de la demo.
SELECT r.nombre AS rol, 'EMPLEADO_VER' AS permiso,
    CASE WHEN EXISTS (
        SELECT 1
        FROM rol_permiso AS rp
        JOIN permiso AS p ON p.id_permiso = rp.id_permiso
        WHERE rp.id_rol = r.id_rol AND p.codigo = 'EMPLEADO_VER' AND p.activo = TRUE
    ) AND r.activo = TRUE THEN 'ASIGNADO' ELSE 'SIN_ASIGNAR' END AS asociacion
FROM rol AS r
WHERE r.nombre IN ('encargado', 'visor')
ORDER BY r.nombre;

-- RESULTADO 4: permiso efectivo de cada cuenta, comprobando todos sus roles.
SELECT u.nombre_usuario AS usuario, 'EMPLEADO_VER' AS permiso,
    CASE WHEN u.activo = TRUE AND EXISTS (
        SELECT 1
        FROM usuario_rol AS ur
        JOIN rol AS r ON r.id_rol = ur.id_rol AND r.activo = TRUE
        JOIN rol_permiso AS rp ON rp.id_rol = r.id_rol
        JOIN permiso AS p ON p.id_permiso = rp.id_permiso AND p.activo = TRUE
        WHERE ur.id_usuario = u.id_usuario AND p.codigo = 'EMPLEADO_VER'
    ) THEN 'PERMITIDO' ELSE 'DENEGADO' END AS resultado
FROM usuario AS u
WHERE u.nombre_usuario IN ('encargado_demo', 'visor_demo')
ORDER BY u.nombre_usuario;

SET @demo_clave_encargado = NULL;
SET @demo_clave_visor = NULL;
