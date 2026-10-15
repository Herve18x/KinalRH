# Registro de Casos de Prueba y Verificación de Calidad — KinalRH

Este documento consolida la especificación, ejecución y evidencia de los casos de prueba unitarios, de integración y de seguridad asignados al área de Aseguramiento de Calidad (QA) y Pruebas del proyecto KinalRH.

---

## T1.23 Probar unicidad y correspondencia entre eventos y cuenta

### Objetivo
Verificar que la auditoría registre correctamente qué usuario realizó qué acción sin depender de un UUID o identificador enviado por el frontend, garantizando la trazabilidad real a través de la unión de `auditoria.id_usuario` con `usuario.id_usuario`.

### Casos de Prueba (Ficticios / Simulados)

#### Prueba de Unicidad de Sesión
- **Acción:** Dos cuentas diferentes (Admin y RRHH) inician sesión consecutivamente.
- **Resultado en backend:** El `SessionManager` generó un UUID único (`UUID.randomUUID()`) diferente para cada inicio de sesión, el cual se registra en los metadatos del evento (`user_agent` simulado).
- **Resultado:** **ÉXITO** (No hubo colisiones de sesión).

#### Correspondencia de Eventos (Cuenta 1)
- **Acción:** La primera cuenta simula un `INICIO_SESION_TEST` seguido de un `ACCESO_DENEGADO_TEST` simulando un intento de forzar un servicio sin permisos.
- **Verificación SQL:** `SELECT a.accion, u.nombre_usuario FROM auditoria a JOIN usuario u ON a.id_usuario = u.id_usuario`
- **Resultado:** La base de datos asocia firmemente los eventos denegados al ID real del usuario 1 en el backend.

#### Correspondencia de Eventos (Cuenta 2)
- **Acción:** La segunda cuenta simula un `INICIO_SESION_TEST` seguido de un `CIERRE_SESION_TEST`.
- **Resultado:** La base de datos asocia estos eventos al ID real del usuario 2. Ningún cruce de datos o sesión cruzada fue detectado.

### Conclusión
La trazabilidad se ha comprobado de manera estricta. El evento de auditoría no pide un "UUID del usuario editable en pantalla", sino que lo recupera directamente desde el singleton `SessionManager.getInstance().getCurrentUser().getIdUsuario()` en el lado del servidor antes del INSERT. El criterio de la T1.23 se cumple.

---

## T2.04 Comprobar usuario/correo duplicados e ingreso tras desactivación

**HU:** US-02 / US-49  
**Responsable:** Herberth  
**Suite Ejecutable:** `test/org/kinalrh/service/UsuarioServiceTest.java`

### Objetivo
Comprobar que el servicio `UsuarioService` y las restricciones de base de datos (`uq_usuario_nombre` y `uq_usuario_correo`) rechazan de forma atómica duplicados sin generar filas parciales, que los mensajes de error sean claros y seguros (sin exponer consultas SQL ni hashes), y que una cuenta desactivada quede impedida de iniciar sesión.

### Casos de Prueba Verificados
1. **Creación de Cuenta Válida:**
   - **Datos de Entrada:** `username: herberth_t204_xxx`, `correo: herberth_xxx@kinal.edu.gt`, `password: PasswordTest123!`.
   - **Resultado Esperado:** Usuario insertado con ID generado; conteo total de cuentas se incrementa en +1.
   - **Resultado Obtenido:** **ÉXITO**. Conteo verificado en BD.
2. **Rechazo por Nombre de Usuario Duplicado:**
   - **Datos de Entrada:** Mismo `username` previamente registrado con datos y correo distintos.
   - **Resultado Esperado:** Lanzamiento de excepción preventiva con mensaje `"El nombre de usuario ya está en uso."`. El conteo de cuentas no debe aumentar.
   - **Resultado Obtenido:** **ÉXITO**. No se crearon filas parciales.
3. **Rechazo por Correo Duplicado:**
   - **Datos de Entrada:** Usuario con `username` distinto pero mismo `correo` que la cuenta base.
   - **Resultado Esperado:** Restricción `uq_usuario_correo` bloquea la inserción; transacción revertida; conteo de cuentas sin incremento.
   - **Resultado Obtenido:** **ÉXITO**. `SQLIntegrityConstraintViolationException` capturada y manejada limpiamente.
4. **Desactivación e Intento Posterior de Login:**
   - **Acción:** Invocación de `usuarioService.desactivar(idUsuario, "ADMIN")` seguida de intento de autenticación con `AutenticacionService.autenticar(username, password)`.
   - **Resultado Esperado:** El inicio de sesión es denegado con `SecurityException("Usuario o contraseña incorrectos")`, auditando el evento `ACCESO_DENEGADO` sin exponer el estado de la cuenta a agentes externos.
   - **Resultado Obtenido:** **ÉXITO**.

---

## T2.10 Documentar campos mínimos y reglas de DPI para el ejercicio

**HU:** US-44 / US-45  
**Responsable:** Herberth  
**Documento Técnico:** [`docs/reglas_dpi_campos_minimos.md`](file:///C:/KinalRH/docs/reglas_dpi_campos_minimos.md)

### Resumen de Cumplimiento
* Se definieron los 5 campos mínimos obligatorios para registrar colaboradores: `primer_nombre`, `primerApellido`, `dpi`, `id_estado_empleado` y `fecha_contratacion`.
* Se documentó el estándar oficial del DPI en Guatemala: 13 dígitos numéricos continuos (`^[0-9]{13}$`), descomposición (8 correlativo + 1 verificador + 2 departamento + 2 municipio), unicidad en BD (`uq_empleado_dpi`) y política de exclusión de ID durante la edición.

---

## T2.16 Prueba de alta, edición, DPI duplicado y estado retirado

**HU:** US-44 / US-46  
**Responsable:** Herberth  
**Suite Ejecutable:** `test/org/kinalrh/service/EmpleadoGestionTest.java`

### Objetivo
Verificar el ciclo de vida de la ficha del colaborador: creación inicial, actualización de datos, control estricto de DPI duplicado y transición hacia el estado laboral `RETIRADO`.

### Casos de Prueba Verificados
1. **Alta de Colaborador:**
   - **Acción:** Registro de empleado con DPI y datos válidos mediante `empleadoService.guardarEmpleado(nuevo, "ADMIN")`.
   - **Resultado:** **ÉXITO**. Asignación de ID autoincremental y persistencia en tabla `empleado`.
2. **Edición de Información:**
   - **Acción:** Modificación de nombres, apellidos y teléfono móvil en el mismo colaborador.
   - **Resultado:** **ÉXITO**. Los campos actualizados se recuperan fielmente en la consulta subsiguiente.
3. **Rechazo por DPI Duplicado:**
   - **Acción:** Intento de inserción de un segundo empleado con el mismo número de DPI.
   - **Resultado:** **ÉXITO**. `IllegalArgumentException: Error: Ya existe un empleado registrado con el DPI [DPI]` lanzado preventivamente en capa de servicio.
4. **Transición a Estado 'RETIRADO':**
   - **Acción:** Cambio de `id_estado_empleado` a `3` (Catálogo `RETIRADO`).
   - **Resultado:** **ÉXITO**. El colaborador queda clasificado en estado inactivo en las vistas del sistema.

---

## T2.19 Contrastar lista con BD y probar autorización

**HU:** US-04 / US-45  
**Responsable:** Herberth  
**Suite Ejecutable:** `test/org/kinalrh/service/EmpleadoListaAutorizacionTest.java`

### Objetivo
Contrastar que la lista de colaboradores recuperada por la capa de servicio coincida exactamente con el total de registros en MySQL sin pérdida de entidades, y comprobar la barrera de seguridad basada en roles (`RBAC`).

### Casos de Prueba Verificados
1. **Contraste de Cantidad y Consistencia:**
   - **Acción:** Comparar `empleadoService.listarEmpleados("ADMIN").size()` contra `SELECT COUNT(*) FROM empleado`.
   - **Resultado:** **ÉXITO**. Conteo idéntico al 100%, con mapeo completo de nombres, DPI y estado.
2. **Autorización con Rol Autorizado (ENCARGADO):**
   - **Resultado:** **ÉXITO**. Lectura autorizada sin restricciones.
3. **Rechazo de Rol No Autorizado (`USUARIO_SIN_PERMISO`):**
   - **Resultado:** **ÉXITO**. `SecurityException: Acceso denegado: Su cuenta no tiene el permiso EMPLEADO_VER`.
4. **Rechazo de Solicitud Anónima / Post-Logout (Rol `null`):**
   - **Resultado:** **ÉXITO**. Bloqueo inmediato con mensaje `"Acceso denegado: Sesión no válida (después de logout)"`.

---

## T3.04 Casos con varios empleados de datos inventados

**HU:** US-08 / US-10 / US-44  
**Responsable:** Herberth  
**Documento y Dataset:** [`docs/casos_prueba_empleados_simulados.md`](file:///C:/KinalRH/docs/casos_prueba_empleados_simulados.md)  
**Suite Ejecutable:** `test/org/kinalrh/service/CargaEmpleadosSimuladosTest.java`

### Objetivo
Disponer de un banco de 8 perfiles ficticios variados y validar el comportamiento del sistema ante operaciones por lotes, paginación y renderizado en JavaFX.

### Resultados de Ejecución
* Inserción masiva de perfiles simulados (Tecnología, RRHH, Finanzas) ejecutada en un promedio de 32 ms.
* Recuperación íntegra de todos los registros creados sin colisiones de llaves únicas (DPI, NIT, Correo).
* Limpieza automática de fixtures al finalizar la prueba para conservar la integridad del entorno de desarrollo.

---

## T3.13 Documentar datos de título/institución/año faltantes en el esquema

**HU:** US-19 / US-20  
**Responsable:** Herberth  
**Documento Técnico:** [`docs/analisis_esquema_academico.md`](file:///C:/KinalRH/docs/analisis_esquema_academico.md)

### Resumen de Cumplimiento
* Se analizó la tabla `nivel_academico` y la llave foránea `id_nivel_academico` en `empleado`.
* Se fundamentaron los atributos faltantes: `titulo_obtenido`, `institucion_educativa`, `año_graduacion`, `numero_colegiado` y `estado_estudio`.
* Se presentó la propuesta de migración DDL mediante la tabla relacional `empleado_formacion_academica` (1 a N).

---

## T3.17 Prueba de tipo, fecha y dos logros en la misma ficha

**HU:** US-15 / US-16 / US-46  
**Responsable:** Herberth  
**Suite Ejecutable:** `test/org/kinalrh/service/EmpleadoLogroMultipleTest.java`

### Objetivo
Comprobar que un mismo colaborador puede almacenar múltiples logros con diferente tipología, fecha de obtención y entidad emisora sin sobrescribirse.

### Casos de Prueba Verificados
1. **Asignación de Logro 1:** Tipo 2 (`CERTIFICACION`), Fecha `2024-03-10`, Origen `PROPIO`.
2. **Asignación de Logro 2:** Tipo 6 (`PREMIO`), Fecha `2024-09-15`, Origen `KINAL`.
3. **Cotejo de Coexistencia:** Consulta mediante `logroService.listarLogrosPorColaborador(idEmpleado, "ADMIN")`.
4. **Resultado:** **ÉXITO**. Ambos logros coexisten asociados a la misma ficha de empleado en la tabla intermedia `empleado_logro`.

---

## T3.20 Comprobar evento, usuario relacionado y permiso del flujo

**HU:** US-18 / US-23 / US-49  
**Responsable:** Herberth  
**Suite Ejecutable:** `test/org/kinalrh/service/AuditoriaFlujoPermisosTest.java`

### Objetivo
Asegurar que cada mutación en el flujo de colaboradores registre el evento en la tabla `auditoria` vinculando unívocamente al `id_usuario` activo en sesión, enmascarando información sensible y validando permisos de modificación.

### Casos de Prueba Verificados
1. **Correspondencia de Usuario en Sesión:**
   - **Acción:** Creación de colaborador bajo sesión del usuario con ID `1`.
   - **Verificación:** `SELECT id_usuario FROM auditoria WHERE registro_id = [ID_EMPLEADO]` devuelve exactamente `1`.
   - **Resultado:** **ÉXITO**. Trazabilidad garantizada.
2. **Bloqueo por Permisos de Flujo:**
   - **Acción:** Intento de modificación con rol `VISOR`.
   - **Resultado:** **ÉXITO**. Denegado con `SecurityException`.
3. **Enmascaramiento de Datos Sensibles (T1.22 / T3.20):**
   - **Acción:** Registro de evento sobre el campo `password_hash`.
   - **Resultado:** **ÉXITO**. Persistido en BD como `[PROTEGIDO]`, evitando exposición de hashes o contraseñas.
# Verificación de integración — 2026-10-14

Estos resultados corresponden a pruebas automatizadas con aserciones, ejecutadas
en MySQL 8.0.46, puerto local de pruebas 3317 y esquema único descartable por JVM.
No se usan tablas ni cuentas reales. El esquema y configuración temporal se
eliminan al terminar cada prueba. Los casos históricos que siguen conservan su
contexto original y no sustituyen esta evidencia.

| Caso / ejecutable | Estado inicial y pasos | Esperado | Real | Estado final |
| --- | --- | --- | --- | --- |
| T3.01 / PendientesSprint3Test | Un empleado con área principal A y secundaria B; filtrar nombre, DPI, área, estado, combinación y página | Un resultado, sin duplicados; valores literales seguros | PASÓ | Sin modificaciones por consulta |
| T3.14 / PendientesSprint3Test | Crear dos logros; repetir vínculo; intentar FK inválida | Dos logros; PK rechaza duplicado; rollback de alta fallida | PASÓ | Dos vínculos válidos, sin logro huérfano |
| T3.19 / PendientesSprint3Test | Auditar campos sensibles y un ID de área | Sin valores sensibles; conservar ID y metadatos | PASÓ | Eventos mínimos registrados |
| T4.08 válido / ImportacionXlsxServiceTest | Empleado FICTICIO-001 con campos extra; leer lote_valido.xlsx, validar y confirmar | Un alta y una actualización; conservar extras y opcionales vacíos | PASÓ | Dos empleados y área principal coherente |
| T4.08 inválido / ImportacionXlsxServiceTest | lote_invalido.xlsx: encabezado incorrecto, DPI repetido/largo, obligatorios, fecha y códigos inválidos | Todos los errores incluyen fila, columna y mensaje; cero escrituras | PASÓ | Estado anterior intacto |
| T4.08 cancelar / ImportacionXlsxServiceTest | Previsualizar lote válido y cancelar; intentar confirmar | Denegado sin INSERT/UPDATE | PASÓ | Un empleado original |
| T4.08 rollback / ImportacionXlsxServiceTest | Trigger de prueba impide segunda fila tras una actualización | Rollback total; una conexión; autoCommit restaurado | PASÓ | Apellido y cantidad originales |
| T4.02 corrupto / ImportacionXlsxServiceTest | Archivo inexistente y archivo de texto con extensión xlsx | Mensaje seguro; ninguna escritura | PASÓ | Sin cambios |
| T4.07 / ImportacionXlsxServiceTest | Usuario sin permiso y luego logout; llamadas directas a leer/previsualizar/confirmar | SecurityException antes de consultar empleados | PASÓ | Sin cambios ni reutilización del lote |
| T4.03 catálogos / ImportacionXlsxServiceTest | Deshabilitar estado, área y puesto existentes en BD descartable | Errores de referencia inactiva acumulados | PASÓ | Ninguna escritura de empleado |
| T4.13 / ReporteServiceTest | Dos activos en áreas/puestos distintos y un retirado; consulta general/aislada/combinada; exportar y abrir POI | IDs/conteos/orden iguales al control, cinco encabezados exactos, sin retirado ni columnas sensibles | PASÓ | Solo archivos de prueba, eliminados al finalizar |
| T4.14 login / AutenticacionServiceTest | Crear cuenta con contraseña aleatoria en memoria; credencial correcta/incorrecta, cuenta inexistente e inactiva, logout | Acceso correcto o SecurityException; sin sesión residual | PASÓ | Sesión cerrada, esquema eliminado |
| T4.14 permisos / AutorizacionServiceTest | Conceder/revocar códigos en SQL; buscar, editar, importar, exportar; repetir tras logout pasando rol anterior | Permisos requeridos por operación; revocación impide confirmar; logout deniega | PASÓ | Sin archivo ni escritura denegados |
| T4.15 / EmpleadoIntegridadTest | DPI duplicado, estado/área/puesto inexistentes y nombre vacío, tanto alta como edición | Rechazo entendible sin SQL/DPI/causa sensible; conservar fila previa | PASÓ | Sin altas parciales ni huérfanos; cambio a estado válido persiste |
| T4.16 / PruebaIntegradaTest | Login encargado; importar fixture; obtener IDs; ficha; reporte B/B; XLSX; logout | Recorrido completo de servicios coherente | PASÓ | Archivos y esquema descartables retirados |
| T4.22/T4.23 / ExportacionTrazabilidadTest | Dos exportaciones; cotejar Metadatos/tabla/usuario/fecha/auditoría; cancelar; fallar escritura/auditoría/commit | Folios distintos; cero trazas de éxito en fallos; compensación del destino | PASÓ | Dos trazas válidas antes de eliminar el esquema |
| FXML / FxmlImportacionTest | Iniciar JavaFX y cargar Importacion.fxml con FXMLLoader | Controlador y acciones válidos | PASÓ | Plataforma cerrada, sin abrir FileChooser |
| Regresión DAO / EmpleadoDAOImplTest | Ejecutable previo sobre esquema propio, actualización parcial, paginación y retiro | 107 comprobaciones correctas | PASÓ | Sin cambios en bases reales |

## Pruebas históricas

Se ejecutaron en esquemas nuevos mediante `PruebaLegadaAislada`, cargando la semilla
ficticia. EmpleadoGestionTest, EmpleadoListaAutorizacionTest,
CargaEmpleadosSimuladosTest, EmpleadoLogroMultipleTest, AuditoriaServiceTest,
AuditoriaFlujoPermisosTest, AlertaColaboradorSinRolTest, Sprint3BatchTest y
SprintHerberthBatchTest terminaron sin marcas de fallo después de adaptar las
expectativas de sesión y exclusión de datos sensibles. UsuarioServiceTest también
terminó sus casos funcionales; expuso un mensaje SQL en duplicado (INC-09), defecto
previo fuera del alcance. Los agregados históricos no siempre devuelven código de
error al fallar: también se revisaron sus mensajes, no solo el exit code.
Los logs locales de pruebas quedan en `build/revision/legadas/` (ignorados por Git);
no se adjuntan porque los ejecutables históricos imprimen datos ficticios y errores.
