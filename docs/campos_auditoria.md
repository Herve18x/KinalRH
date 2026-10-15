# Campos de auditoría — T3.19

Cada evento conserva `id_usuario` (nullable para intentos anónimos), `accion`,
`entidad`, `registro_id` y `fecha_hora`. El DAO conserva el UUID de sesión en
`user_agent`. Las llamadas existentes de alta/edición conservan su evento y autor.

Solo se conservan valores de identificadores internos: `id_estado_empleado`,
`id_area_principal`, `id_puesto_actual`, `id_jefe_inmediato`, `id_nivel_academico`,
`id_empleado`, `id_rol`, `id_tipo_logro`; banderas `activo`, `es_estado_activo`,
`destacado`; y códigos de `permiso`. Los valores se limitan a su formato esperado.
Los campos no incluidos se descartan junto con sus valores anterior/nuevo.

Nunca se registran contraseña, password, password_hash, DPI, nombres, correos,
direcciones, teléfonos, salarios ni texto libre de alta/edición o login fallido.
Los errores de persistencia de auditoría usan mensajes fijos sin SQL ni valores.
Esta política aplica a eventos nuevos; no modifica ni elimina auditoría histórica.
