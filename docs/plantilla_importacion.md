# Importación de empleados — T4.01

`templates/empleados_plantilla.xlsx` contiene una sola hoja con estos siete
encabezados exactos, en orden y sin columnas adicionales:

| Encabezado | Obligatorio | Ejemplo ficticio / regla |
| --- | --- | --- |
| dpi | Sí | FICTICIO-001; texto de hasta 20 caracteres, clave única |
| primer_nombre | Sí | Elena; máximo 80 caracteres |
| primer_apellido | Sí | Prueba; máximo 80 caracteres |
| estado_codigo | Sí | ACTIVO; código de catálogo habilitado |
| area_codigo | No | AREA_DEMO; código de área habilitada |
| puesto_codigo | No | PUESTO_DEMO; código de puesto habilitado |
| fecha_ingreso_kinal | No | 2026-10-01; fecha válida AAAA-MM-DD |

Se lee la primera hoja y se omiten filas completamente vacías. Se conserva el
número de fila de Excel (cabecera=1). Escriba DPI como texto para preservar ceros
iniciales y evitar redondeo de Excel. Use valores literales, sin fórmulas. Se
recortan espacios de los extremos; no se corrigen encabezados automáticamente.

DPI nuevo: alta. DPI existente: actualizar únicamente columnas mapeadas no vacías.
Los cuatro campos obligatorios se deben informar también para una actualización.
Área, puesto o fecha vacíos conservan el valor anterior. Datos ajenos a la plantilla
(contacto, otros nombres, nivel académico, jefe, documentos) se conservan.
Una asignación de área sincroniza `empleado_area` en la misma transacción.

El mismo DPI repetido en un XLSX es error en todas sus filas; no se elige una fila
ganadora. Se acumulan errores de encabezados, obligatorios, longitud, fecha y
referencias desconocidas/inactivas. Cada error indica fila, columna y mensaje sin
repetir valores sensibles. Archivo inexistente, corrupto, cifrado o de otro formato
no escribe en MySQL. Cualquier error impide confirmar el lote completo.

Se requiere sesión activa y `IMPORTACION_EJECUTAR` antes de leer/previsualizar y de
confirmar, también mediante llamada directa al servicio. Cancelar invalida el lote
y no escribe. Cambiar de sesión exige una previsualización nueva. Si falla cualquier
fila, se revierte todo el lote; nunca se comunica éxito parcial.

La vista base existente permite seleccionar, validar, confirmar y cancelar.
El controlador expone `getLote()` con filas, propuesta, acción, ID y mensajes para
integración de la previsualización de T4.04; esa vista detallada sigue pendiente
de Daniel y no se crea en estas tarjetas.
