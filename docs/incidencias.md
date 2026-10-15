# Incidencias de integración — T4.17

Fecha local de verificación: 2026-10-14. Se distinguen defectos comprobables de la
implementación integrada y problemas observados al ejecutar herramientas. Las
pruebas citadas verifican la corrección; no se presenta una ejecución previa
fallida si no fue registrada.

| ID | Caso / pasos reproducibles | Severidad | Módulo | Responsable | Fecha corrección | Resultado |
| --- | --- | --- | --- | --- | --- | --- |
| INC-01 | Crear logro y fallar FK al vincular empleado. El flujo integrado abría conexiones independientes | Alta | Logros T3.14 | Zabala | 2026-10-14 | Corregido; PendientesSprint3Test comprueba rollback sin logro huérfano |
| INC-02 | Repetir la pareja empleado/logro. El INSERT integrado usaba ON DUPLICATE KEY UPDATE | Media | Logros T3.14 | Zabala | 2026-10-14 | Corregido; PK rechaza vínculo duplicado; edición explícita conserva actualización de metadatos |
| INC-03 | Auditar alta con texto libre o login fallido. La implementación conservaba nombre/usuario intentado | Alta | Auditoría T3.19 | Zabala | 2026-10-14 | Lista permitida, sin texto libre; PendientesSprint3Test comprueba exclusiones |
| INC-04 | Logout y buscar/editar pasando un rol previamente autorizado. El servicio aceptaba el parámetro de rol | Alta | Autorización T4.14 | Alexis | 2026-10-14 | Se exige sesión actual; AutorizacionServiceTest comprueba llamadas directas tras logout |
| INC-05 | Conceder IMPORTACION_EJECUTAR y consultar obtenerPermisos. El SELECT devolvía nombre descriptivo en vez de código | Alta | Permisos T4.07/T4.12 | Alexis | 2026-10-14 | Consulta por codigo y catálogos activos; pruebas de autorización e integración pasan |
| INC-06 | Revisar arranque sin config local: había una contraseña incorporada en el fallback de conexión | Alta | Configuración T4.18 | Keneth | 2026-10-14 | Eliminada; cuenta limitada por defecto y contraseña solo local, sin reproducir el valor anterior |
| INC-07 | Ejecutar Maven con JAVA_HOME heredado inválido | Baja | Arranque T4.18 | Keneth | 2026-10-14 | Corregido en entorno de ejecución; README indica JDK 21 y raíz del JDK |
| INC-08 | Resolver dependencias Maven en entorno restringido; caché .m2 no escribible | Baja | Entorno de compilación | Keneth | 2026-10-14 | Descarga autorizada; compilación y pruebas posteriores pasan |
| INC-09 | UsuarioServiceTest: intentar correo de usuario duplicado; mensaje/log conserva texto SQL de la constraint | Media | Usuarios, fuera de estas tarjetas | Por asignar | Pendiente | Defecto existente observado en esquema descartable; no corregido para respetar el alcance; no se publican valores del log |
| INC-10 | Pruebas históricas de lista/auditoría dependían del parámetro de rol y del enmascaramiento anterior; al exigir sesión vigente fallaban | Baja | Pruebas T3.19/T4.14 | Herberth y Alexis | 2026-10-14 | Adaptadas a sesiones reales y exclusión de valores; listas, auditoría y agregados vuelven a pasar |

Dependencias observadas, sin implementar tarjetas ajenas: falta la vista detallada
T4.04 (Daniel) y falta ReporteController de T4.10 (Daniel); Reportes.fxml existe
como vista estática. No se clasifican como defectos inventados: se registra el
estado real de integración en prueba_integrada.md. T4.21 no se implementó.
