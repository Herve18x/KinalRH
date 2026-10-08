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
