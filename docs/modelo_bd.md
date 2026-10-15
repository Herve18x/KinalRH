# Documentación del Modelo de Base de Datos - IN4CM (KinalRH)

## Resumen de Verificación (Tarea T1.03)
- **Script Principal:** `database/dbkinalrh_dev.sql`
- **Motor:** MySQL 8.x (InnoDB, utf8mb4)

## Catálogos Principales
### Roles del Sistema
- `admin`: Administración técnica y configuración general.
- `encargado`: Gestión operativa de Recursos Humanos.
- `gerenteArea`: Consulta y aprobaciones de área.
- `gerenteGeneral`: Consulta institucional y reportes globales.
- `jefe`: Consulta de colaboradores a su cargo.
- `visor`: Consulta de Imagen Institucional.

### Estados del Empleado
- `ACTIVO` (Activo en sistema)
- `INACTIVO`
- `RETIRADO`
- `SUSPENDIDO`
- `EN_PROCESO_CONTRATACION`
- `FINALIZADO`

## Llaves Únicas e Índices Clave
- `empleado`: DPI, NIT, Código de Empleado.
- `usuario`: Nombre de usuario, Correo electrónico, ID Empleado.
- `area` / `puesto`: Código y Nombre.
- `permiso`: Código único de permiso.

## Vistas Incluidas
1. `vw_empleados_activos`
2. `vw_empleados_por_area`
3. `vw_empleados_por_puesto`
4. `vw_empleados_por_nivel_academico`
5. `vw_logros_por_empleado`

## Mapeo Cruzado SQL - Java (Tabla: usuario)

| Columna SQL | Tipo SQL | Atributo Java | Tipo Java | Restricción / Relación |
| :--- | :--- | :--- | :--- | :--- |
| `id_usuario` | BIGINT UNSIGNED | `idUsuario` | `Long` | PRIMARY KEY |
| `nombre_usuario` | VARCHAR(80) | `nombreUsuario` | `String` | UNIQUE (`uq_usuario_nombre`) |
| `password_hash` | VARCHAR(255) | `passwordHash` | `String` | NOT NULL |
| `nombre_completo` | VARCHAR(160) | `nombreCompleto` | `String` | NOT NULL |
| `correo` | VARCHAR(150) | `correo` | `String` | UNIQUE (`uq_usuario_correo`) |
| `activo` | BOOLEAN | `activo` | `boolean` | DEFAULT TRUE |
| `id_empleado` | BIGINT UNSIGNED | `idEmpleado` | `Long` | FK (`empleado.id_empleado`) |
| `ultimo_acceso_en` | DATETIME | `ultimoAccesoEn` | `Timestamp` | NULL |
| `creado_en` | TIMESTAMP | `creadoEn` | `Timestamp` | DEFAULT CURRENT_TIMESTAMP |
| `actualizado_en` | TIMESTAMP | `actualizadoEn` | `Timestamp` | ON UPDATE |
# Relaciones organizacionales — T3.05

Fuente: `database/dbkinalrh_dev.sql`, MySQL 8, tablas InnoDB.

| Columna | FK | Nulabilidad | Borrado / actualización |
| --- | --- | --- | --- |
| empleado.id_area_principal | area.id_area | NULL | SET NULL / CASCADE |
| empleado.id_puesto_actual | puesto.id_puesto | NULL | SET NULL / CASCADE |
| empleado.id_jefe_inmediato | empleado.id_empleado | NULL | SET NULL / CASCADE |
| empleado_area.id_empleado | empleado.id_empleado | NOT NULL | CASCADE / CASCADE |
| empleado_area.id_area | area.id_area | NOT NULL | RESTRICT / CASCADE |

`empleado.id_area_principal` es la referencia actual para filtros y reportes.
El DAO sincroniza su vínculo `empleado_area` en la misma transacción: desmarca
las demás áreas y marca la seleccionada. `fecha_inicio` y `fecha_fin` son DATE
nullable. Una asignación principal actual debe tener `fecha_fin` NULL y
`fecha_inicio` no posterior a hoy. El puesto/jefe actuales no tienen fechas propias.

La PK `(id_empleado,id_area)` permite una sola fila por pareja, no un historial
de múltiples periodos. No existe constraint de una sola bandera principal, de
igualdad con `id_area_principal`, ni de orden de fechas. SQL externo puede romper
esa coherencia. La aplicación debe escribir ambos lados transaccionalmente.
No se agrega un historial ni se alteran tablas destructivamente.

La FK de jefe garantiza existencia, pero permite jefe igual al propio empleado.
`EmpleadoService.validarJefeInmediato` lo impide y rechaza ciclos directos de dos
empleados. No hay trigger ni detección de ciclos arbitrarios en el esquema real.
Una sola columna por empleado representa un puesto, jefe y área principal actuales.
