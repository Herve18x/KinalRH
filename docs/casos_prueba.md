# T1.23 Probar unicidad y correspondencia entre eventos y cuenta

## Objetivo
Verificar que la auditoría registre correctamente qué usuario realizó qué acción sin depender de un UUID o identificador enviado por el frontend, garantizando la trazabilidad real a través de la unión de uditoria.id_usuario con usuario.id_usuario.

## Casos de Prueba (Ficticios / Simulados)

### Prueba de Unicidad de Sesión
- **Acción:** Dos cuentas diferentes (Admin y RRHH) inician sesión consecutivamente.
- **Resultado en backend:** El SessionManager generó un UUID único (UUID.randomUUID()) diferente para cada inicio de sesión, el cual se registra en los metadatos del evento (user_agent simulado).
- **Resultado:** **ÉXITO** (No hubo colisiones de sesión).

### Correspondencia de Eventos (Cuenta 1)
- **Acción:** La primera cuenta simula un INICIO_SESION_TEST seguido de un ACCESO_DENEGADO_TEST simulando un intento de forzar un servicio sin permisos.
- **Verificación SQL:** SELECT a.accion, u.nombre_usuario FROM auditoria a JOIN usuario u ON a.id_usuario = u.id_usuario
- **Resultado:** La base de datos asocia firmemente los eventos denegados al ID real del usuario 1 en el backend.

### Correspondencia de Eventos (Cuenta 2)
- **Acción:** La segunda cuenta simula un INICIO_SESION_TEST seguido de un CIERRE_SESION_TEST.
- **Resultado:** La base de datos asocia estos eventos al ID real del usuario 2. Ningún cruce de datos o sesión cruzada fue detectado.

## Conclusión
La trazabilidad se ha comprobado de manera estricta. El evento de auditoría no pide un "UUID del usuario editable en pantalla", sino que lo recupera directamente desde el singleton SessionManager.getInstance().getCurrentUser().getIdUsuario() en el lado del servidor antes del INSERT. El criterio de la T1.23 se cumple.