# Base de datos de desarrollo

Fuente de verdad: `dbkinalrh_dev.sql`, MySQL **8.0** (verificado con **8.0.46**),
InnoDB y `utf8mb4_0900_ai_ci`. El nombre histórico es `IN4CM`. Use, por ejemplo,
host `127.0.0.1`, puerto `3306` y una copia `IN4CM_pruebas`. MySQL 5.7/MariaDB
no tienen la misma collation y no se declaran compatibles.

## Instalación en orden

1. Cree una BD descartable, con una cuenta administradora local, sin datos reales:
   `CREATE DATABASE IN4CM_pruebas CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;`.
2. Haga una copia local de `dbkinalrh_dev.sql` y sustituya **todas** las apariciones
   de `IN4CM` por `IN4CM_pruebas` antes de ejecutarla. El script contiene CREATE
   DATABASE y USE; seleccionar otra BD desde el cliente no evita su USE interno.
   Ejecute esa copia completa una sola vez sobre la BD vacía.
3. Seleccione `USE IN4CM_pruebas;` y ejecute las migraciones aprobadas en orden.
   En este repositorio está `migrations/002_exportacion_reporte.sql` (T4.22).
   No hay un archivo 001 que deba inventarse. La migración no contiene USE ni DROP
   y se aplica una vez; añade tabla, FK de usuario y folio UNIQUE.
4. Haga la misma sustitución en una copia local de `seed_dev.sql` y ejecútela.
   Esta semilla crea cuentas y un empleado ficticios para la demostración;
   no es idempotente y contiene credenciales de demostración. No capture ni
   publique sus claves, hashes ni la salida de las consultas sensibles.

En el cliente `mysql`, puede ejecutar cada copia con `SOURCE ruta/al/archivo.sql;`.
Revise siempre el nombre de BD con `SELECT DATABASE();` antes de ejecutar SQL.

## Cuenta de aplicación

Use `usuario_app_dev.example.sql` como referencia, adaptando localmente el nombre
de BD y una contraseña propia. `kinalrh_app@localhost` necesita SELECT/INSERT/UPDATE
en la BD descartable y los DELETE puntuales existentes para asociaciones; no
necesita CREATE/DROP/ALTER/GRANT. Ejecute migraciones con la cuenta administradora,
nunca desde la aplicación. No use root para el arranque normal.
`config.properties` es local e ignorado; no incluya secretos en Git.

## Comprobaciones sin datos sensibles

```sql
SELECT VERSION(), DATABASE();
SHOW TABLES;
SHOW CREATE TABLE exportacion_reporte;
SELECT nombre_usuario, activo FROM usuario
WHERE nombre_usuario IN ('encargado_demo','visor_demo');
SELECT r.nombre AS rol, p.codigo AS permiso FROM rol_permiso rp
JOIN rol r ON r.id_rol=rp.id_rol JOIN permiso p ON p.id_permiso=rp.id_permiso;
```

La semilla histórica solo concede EMPLEADO_VER al encargado. Importar requiere
IMPORTACION_EJECUTAR; exportar requiere REPORTES_XLSX y consultar EMPLEADO_VER.
Conceda únicamente los permisos acordados mediante las asociaciones existentes;
estas tarjetas no amplían automáticamente privilegios de la semilla.

Las pruebas nuevas crean `kinalrh_revision_<número>` y lo eliminan al terminar;
la prueba existente usa `kinalrh_prueba_sprint2_<número>`. Requieren una cuenta de
pruebas con CREATE/DROP exclusivamente en una instancia descartable. Nunca cambie
las pruebas para apuntarlas a datos reales. DROP solo es aceptable en una copia
segura identificada: no borre ni reinicie IN4CM ni una base compartida.

La FK de exportación usa RESTRICT al borrar usuarios para conservar el autor.
Desactive las cuentas en lugar de eliminarlas. Si falta la migración 002, la
exportación falla sin publicar un archivo nuevo ni registrar éxito parcial.
