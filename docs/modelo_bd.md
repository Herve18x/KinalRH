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
