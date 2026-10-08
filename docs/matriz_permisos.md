# Matriz provisional de permisos — Sprint 1

Tarea: T1.18. HU: US-03 / US-04. Responsable: Herberth.

## Alcance de la demostración

Se prueba el permiso `EMPLEADO_VER` para la consulta completa del resumen inicial de un empleado ficticio. Esta matriz define los resultados esperados; la evidencia real se registra después de ejecutar la semilla en la base de desarrollo.

## Roles definidos en el SQL

| Rol | Prueba de EMPLEADO_VER en el Sprint 1 | Resultado esperado | Otros permisos |
| --- | --- | --- | --- |
| admin | No se prueba | Pendiente de definición | Pendientes de definición |
| encargado | Se prueba con encargado_demo | Permitido | Pendientes de definición |
| gerenteArea | No se prueba | Pendiente de definición | Pendientes de definición |
| gerenteGeneral | No se prueba | Pendiente de definición | Pendientes de definición |
| jefe | No se prueba | Pendiente de definición | Pendientes de definición |
| visor | Se prueba con visor_demo | Denegado | Pendientes de definición |

El nombre de un rol no concede permisos automáticamente. Solo las asociaciones activas de `usuario_rol`, `rol_permiso`, `rol` y `permiso` determinan los permisos efectivos de una cuenta activa. Los permisos pendientes no se presumen permitidos.

## Cuentas ficticias de prueba

| Cuenta | Rol único | Cuenta activa | EMPLEADO_VER | Consulta del resumen |
| --- | --- | --- | --- | --- |
| encargado_demo | encargado | Sí | Asignado | Permitida |
| visor_demo | visor | Sí | Sin asignar | Denegada |

- Las dos cuentas pueden probar el ingreso con sus claves temporales. Que `visor_demo` pueda autenticarse no autoriza la consulta del resumen.
- `encargado_demo` está asociado al empleado ficticio con DPI `3000123451201`, código `EMP-TEST-01` y nombre `Juan Ficticio`.
- `visor_demo` no está asociado a un empleado. La denegación se debe a la falta de `EMPLEADO_VER`, no a esa ausencia de asociación.
- Cada ejecución de la semilla renueva las claves de ambas cuentas. Las claves aparecen en el primer resultado del script y no se guardan en este documento.
- Los hashes de la semilla usan SHA-256 hexadecimal para coincidir con `SecurityUtil.hashSHA256()` del código actual. Si T1.12 cambia ese algoritmo, se deberá actualizar también la semilla.

## Preparación y consultas de verificación

1. Usar la base de desarrollo descartable `IN4CM`, con el esquema y sus catálogos ya creados.
2. Abrir `database/seed_dev.sql` en MySQL Workbench y ejecutarlo completo en una misma conexión.
3. Si alguna sentencia falla, ejecutar `ROLLBACK;` y corregir el error antes de continuar o ejecutar `COMMIT`.
4. Conservar las claves del primer resultado solamente para esta prueba local.
5. Revisar los tres resultados siguientes, que consultan las asociaciones y el permiso efectivo.

### Resultado esperado de usuario_rol

| usuario | rol | cuenta_activa |
| --- | --- | --- |
| encargado_demo | encargado | 1 |
| visor_demo | visor | 1 |

### Resultado esperado de rol_permiso

| rol | permiso | asociacion |
| --- | --- | --- |
| encargado | EMPLEADO_VER | ASIGNADO |
| visor | EMPLEADO_VER | SIN_ASIGNAR |

### Resultado esperado de permisos efectivos

| usuario | permiso | resultado |
| --- | --- | --- |
| encargado_demo | EMPLEADO_VER | PERMITIDO |
| visor_demo | EMPLEADO_VER | DENEGADO |

## Comprobación en la aplicación

- Con `encargado_demo`, el panel permite solicitar el resumen y el servicio comprueba `EMPLEADO_VER` antes de entregar datos.
- Con `visor_demo`, el panel oculta o deshabilita la consulta. Una llamada directa al servicio también debe denegarse sin entregar el resumen.
- Ambos perfiles deben poder cerrar sesión. Después del cierre, una llamada protegida requiere una nueva sesión válida.
- Esta prueba de interfaz y servicio depende de T1.17 y T1.20. La semilla y sus consultas no prueban por sí solas esas capas.

## Evidencia y criterio de cierre

| Comprobación | Estado inicial | Evidencia por registrar |
| --- | --- | --- |
| Asociaciones usuario_rol | Pendiente de ejecutar en la BD del equipo | Resultado 2 de la semilla |
| Diferencia de rol_permiso | Pendiente de ejecutar en la BD del equipo | Resultado 3 de la semilla |
| Permiso efectivo de cada cuenta | Pendiente de ejecutar en la BD del equipo | Resultado 4 de la semilla |
| Autorización en interfaz y servicio | Pendiente de T1.17 / T1.20 | Capturas o casos de prueba |

Adjuntar únicamente los resultados de asociaciones y permisos. No incluir el resultado que contiene las claves temporales. T1.18 se cierra cuando la ejecución real de la semilla y las consultas devuelve exactamente la diferencia de permisos descrita en esta matriz.
