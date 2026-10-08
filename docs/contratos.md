# Contratos de DAO y servicio — T1.10

HU: US-45 / US-46. Responsable: Herberth.

## Firmas compartidas

Paquete: `org.kinalrh.dao`.

| Interfaz | Método | Resultado |
| --- | --- | --- |
| UsuarioDAO | buscarPorNombreUsuario(String nombreUsuario) | Optional<Usuario> |
| UsuarioDAO | obtenerPermisos(long idUsuario) | Set<String> |
| EmpleadoDAO | buscarPorId(long idEmpleado) | Optional<Empleado> |
| EmpleadoDAO | insertar(Empleado empleado) | long |
| EmpleadoDAO | actualizar(Empleado empleado) | boolean |

Todos los métodos declaran `throws SQLException`.

## Resultados y errores

- Las búsquedas sin coincidencias devuelven `Optional.empty()`, nunca `null`.
- La búsqueda de usuario incluye su hash y estado. El servicio verifica la contraseña y rechaza las cuentas inactivas.
- Los permisos son códigos de `permiso.codigo`, sin duplicados. Solo se consideran usuarios, roles y permisos activos. La ausencia de permisos devuelve un conjunto vacío, nunca `null`.
- Insertar devuelve el ID generado por MySQL. Una inserción fallida o sin clave generada lanza `SQLException`.
- Actualizar usa `empleado.getIdEmpleado()`. Devuelve `true` si el registro existe y la operación tiene éxito, aunque sus datos ya fueran iguales; devuelve `false` si el ID no existe.
- Los errores de conexión, consultas o restricciones se propagan como `SQLException`. No se convierten en resultados vacíos ni en `false`.
- El servicio captura `SQLException` y comunica un error entendible al controlador. Los mensajes de la interfaz no incluyen consultas, credenciales ni hashes.

## Responsabilidades

### DAO e implementaciones

- Consultar o modificar la base de datos y mapear los resultados a los modelos.
- Abrir cada conexión mediante `Conexion.getInstancia().conectar()`.
- Cerrar `Connection`, `PreparedStatement` y `ResultSet` con `try-with-resources`.
- Utilizar consultas parametrizadas con `PreparedStatement` y recuperar las claves generadas en las inserciones.
- Consultar permisos mediante `usuario_rol`, `rol`, `rol_permiso` y `permiso`.
- Propagar `SQLException` al servicio.

### Servicios

- Validar los datos antes de llamar al DAO: nombre de usuario requerido, IDs positivos y empleado no nulo.
- Validar DPI, primer nombre, primer apellido y estado del empleado; para actualizar, exigir un ID de empleado válido.
- Comprobar las referencias de área y puesto cuando se proporcionen. Las relaciones utilizan IDs `Long`, compatibles con `BIGINT`.
- Verificar contraseña y estado de la cuenta durante la autenticación.
- Comprobar sesión activa y los permisos de la operación. La consulta del resumen requiere `EMPLEADO_VER`.
- Convertir los errores de persistencia en mensajes entendibles y construir el resumen con nombres de los catálogos.

### Controladores

- Recoger datos de la interfaz, llamar al servicio y presentar resultados o errores.
- Mantener las operaciones JDBC y la validación de contraseñas en las capas anteriores.

## Campos exactos del resumen inicial de prueba

El resumen corresponde al empleado consultado.

| Campo | Tipo Java | Origen | Puede ser nulo |
| --- | --- | --- | --- |
| idEmpleado | Long | empleado.id_empleado | No |
| nombreVisible | String | primerNombre y primerApellido de Empleado, separados por un espacio | No |
| estado | String | estado_empleado.nombre, mediante empleado.id_estado_empleado | No |
| area | String | area.nombre, mediante empleado.id_area_principal | Sí |
| puesto | String | puesto.nombre, mediante empleado.id_puesto_actual | Sí |

- El resumen inicial usa los nombres disponibles en el modelo mínimo `Empleado` de T1.08.
- Estado, área y puesto muestran nombres de sus catálogos. Las relaciones se guardan mediante sus IDs.
- Cuando área o puesto sean nulos, la interfaz muestra `Sin área` o `Sin puesto`; esos textos no se guardan en la base de datos.
- El estado laboral procede de `estado_empleado`; la tabla `empleado` no tiene una columna `activo`.
- El resumen no incluye contraseñas, hashes ni datos de la cuenta de usuario.

## Dependencias e integración

- `UsuarioDAO` depende de los modelos de T1.07: `Usuario`, `Rol` y `Permiso`.
- `EmpleadoDAO` depende del modelo `Empleado` de T1.08.
- T1.10 entrega estas dos interfaces y este documento. Las implementaciones se desarrollan en las tareas correspondientes.
- En T1.13, `UsuarioDAOImpl` debe implementar `UsuarioDAO` y respetar sus métodos y excepciones. La validación de credenciales que actualmente realiza `autenticar(...)` se trasladará al servicio de autenticación.
- Alexis, Keneth, Zabala y Daniel utilizarán estas mismas firmas. No se crearán métodos alternativos para la misma operación.
