# Recorrido reproducible — T4.16

Fecha de ejecución: 2026-10-14 (America/Guatemala).
Entorno: JDK 21, JavaFX 21.0.11, MySQL 8.0.46, Connector/J 8.0.33, POI 5.5.1.
Ejecutable: `org.kinalrh.service.PruebaIntegradaTest`; comandos en README.

## Estado inicial

`PruebaBD` crea un esquema único desde el SQL real y aplica la migración 002.
Catálogos ficticios AREA_A/AREA_B y PUESTO_A/PUESTO_B; empleado FICTICIO-001
con área/puesto A, apellido Original, segundo nombre y correo que se conservarán.
La cuenta ficticia `revision_visor` tiene rol **encargado** asignado en SQL para
esta prueba y permisos EMPLEADO_VER, IMPORTACION_EJECUTAR y REPORTES_XLSX.
La contraseña se genera en memoria para cada ejecución y no se imprime ni guarda
en documentación. El nombre histórico de la cuenta no determina sus permisos.

## Pasos, esperado y resultado real

| Paso | Esperado | Real |
| --- | --- | --- |
| Autenticar e iniciar sesión | Rol encargado obtenido de SQL; UUID de sesión vigente | PASÓ |
| Leer test/fixtures/lote_valido.xlsx | Primera hoja; siete encabezados; filas 2 y 3 | PASÓ |
| Previsualizar en servicio | Fila 2 ACTUALIZAR, fila 3 NUEVO; cero errores; sin escrituras | PASÓ |
| Confirmar | Un alta, una actualización; lista de IDs de ambas operaciones | PASÓ |
| Consultar ID actualizado | Apellido Actualizada; opcionales vacíos conservados | PASÓ |
| Consultar ID nuevo | Área B y puesto B existentes; ficha accesible con EMPLEADO_VER | PASÓ |
| Consultar reporte área B/puesto B | Exactamente el ID nuevo, activo | PASÓ |
| Exportar y reabrir con POI | Mismo ID/orden, cinco columnas permitidas y hoja Metadatos separada | PASÓ |
| Comprobar traza | Un registro de exportación del usuario autenticado | PASÓ |
| Logout y repetir llamadas directas | Ficha, importación y exportación denegadas aunque se conserve el rol anterior | PASÓ |

Estado final: se cierra la sesión, se elimina el XLSX temporal de la prueba y se
elimina únicamente el esquema que creó el ejecutable. No se usan datos reales.
Los IDs se obtienen de `Resultado.ids()` y no se presuponen constantes en el flujo.

`FxmlImportacionTest` cargó la vista tocada mediante FXMLLoader con sus acciones
y controlador. No abre ventanas de selección durante la prueba automatizada.
T4.04 no tiene PrevisualizacionImportacion.fxml y T4.10 no tiene ReporteController;
el recorrido visual completo queda pendiente de esas tarjetas de Daniel. Se
verifica el recorrido de servicios sin implementar sus pantallas.

## T4.22/T4.23 y límites

La versión integrada de develop contiene SessionManager con UUID y AuditoriaDAO
con autor/registro/fecha (T1.22/T1.23). No se encontró evidencia de rechazo de la
ampliación, por lo que se añadió la migración compatible 002.

ExportacionTrazabilidadTest cotejó dos folios UUID diferentes con tabla, usuario,
tipo, fecha y auditoría EXPORTACION_REPORTE, registro_id=id_exportacion. Cada XLSX
se reabre con POI; el folio no ocupa filas ni columnas de empleados. Cancelar,
fallar escritura o fallar auditoría no registra éxito. El fallo de commit inyectado
revierte la traza y restaura el archivo anterior (o retira el nuevo).

MySQL y el sistema de archivos no comparten una transacción distribuida. Se usa
un archivo temporal, movimiento atómico cuando está disponible y compensación
ante excepciones. Una caída del proceso/equipo entre publicación y commit puede
requerir cotejar el folio y restaurar un respaldo; no se promete atomicidad frente
a cortes de energía. Si falla la restauración, se conserva el respaldo temporal
y se comunica error. No hay eliminación automática de archivos ajenos.

## Cierre por tarjeta

Las rutas Java siguientes parten de `src/org/kinalrh/`; las pruebas de
`test/org/kinalrh/`. Todas las tarjetas solicitadas se completaron en su alcance.
Las pantallas de Daniel no forman parte de este cierre. No hay tarjeta solicitada
bloqueada; las dependencias visuales y riesgos se describen arriba e incidencias.md.

| Tarjeta | Estado | Archivos modificados/nuevos o reutilizados | Criterio cumplido y verificación |
| --- | --- | --- | --- |
| T3.01 | COMPLETE | dao/EmpleadoDAO.java; dao/impl/EmpleadoDAOImpl.java; service/EmpleadoService.java; service/PendientesSprint3Test.java (test) | Nombre parcial literal, DPI exacto, área/estado por ID, combinación, vacíos, parámetros y paginación estable; pruebas aisladas/combinadas pasan |
| T3.05 | COMPLETE | database/dbkinalrh_dev.sql; docs/modelo_bd.md; dao/impl/EmpleadoDAOImpl.java | FK/nulabilidad/fechas/limitaciones documentadas; asignación principal sincronizada; jefe propio rechazado; sin cambio destructivo; SQL ejecutado en MySQL |
| T3.14 | COMPLETE | model/Logro.java; dao/LogroDAO.java; dao/impl/LogroDAOImpl.java; dao/impl/EmpleadoLogroDAOImpl.java; service/LogroService.java | Campos reales y timestamps; logro/vínculo en una transacción; PK rechaza duplicados; dos logros y rollback comprobados por PendientesSprint3Test y EmpleadoLogroMultipleTest |
| T3.19 | COMPLETE | service/AuditoriaService.java; docs/campos_auditoria.md; pruebas PendientesSprint3Test y AuditoriaFlujoPermisosTest | Valores permitidos explícitos; exclusión de claves/DPI/PII/texto libre; usuario/acción/entidad/registro/fecha preservados; pruebas de regresión pasan |
| T4.01 | COMPLETE | docs/plantilla_importacion.md; templates/empleados_plantilla.xlsx; service/FixturesImportacion.java (test) | Siete columnas exactas, cuatro obligatorias, fecha ISO, DPI clave, actualización parcial y permisos documentados; plantilla reabierta con POI |
| T4.02 | COMPLETE | service/ImportacionXlsxService.java; controller/ImportacionController.java; view/Importacion.fxml; nbproject/project.properties; nbproject/dependencies-pom.xml; README.md | FileChooser xlsx, WorkbookFactory, primera hoja, cabecera/filas/números, recursos cerrados, corrupto/inexistente seguros; ImportacionXlsxServiceTest y FXMLLoader pasan |
| T4.03 | COMPLETE | service/ValidadorImportacion.java; model/FilaImportacion.java | Encabezados, obligatorios, DPI, fecha, repetidos y referencias activas; todos los errores incluyen fila/columna/mensaje; pruebas sin escrituras pasan |
| T4.05 | COMPLETE | service/ImportacionXlsxService.java; model/FilaImportacion.java | NUEVO/ACTUALIZAR/ERROR, ID existente, propuesta/mensajes y opcionales en memoria; conteo de BD intacto tras previsualizar |
| T4.06 | COMPLETE | service/ImportacionXlsxService.java; dao/impl/EmpleadoDAOImpl.java | Overloads con Connection compartida; una conexión por lote, commit/rollback total y autoCommit restaurado; trigger de fallo y conservación parcial verificados |
| T4.07 | COMPLETE | controller/ImportacionController.java; service/AutorizacionService.java; service/ImportacionXlsxService.java; dao/impl/UsuarioDAOImpl.java | Sesión y código IMPORTACION_EJECUTAR antes de lectura/preview/confirmación; revocación y llamadas directas denegadas; cancelación disponible |
| T4.08 | COMPLETE | service/ImportacionXlsxServiceTest.java (test); test/fixtures/lote_invalido.xlsx; test/fixtures/lote_valido.xlsx; docs/casos_prueba.md | Alta+actualización, encabezados/DPI repetido, cancelación y fallo intermedio/rollback reproducidos; todos pasan |
| T4.09 | COMPLETE | dao/ReporteDAO.java; dao/impl/ReporteDAOImpl.java; model/FilaReporte.java | es_estado_activo, área principal/puesto actual opcionales, parámetros, cinco campos y orden por ID; ReporteServiceTest coteja consulta control |
| T4.11 | COMPLETE | service/ExportacionXlsxService.java; service/ReporteService.java | XSSFWorkbook con mismo conjunto/orden y cinco encabezados; sin datos sensibles; cancelación false y fallos sin éxito; POI reabre resultados. ReporteController no existe, no se crea |
| T4.12 | COMPLETE | service/ReporteService.java; service/AutorizacionService.java; service/ExportacionXlsxService.java; docs/matriz_permisos.md | EMPLEADO_VER consulta y REPORTES_XLSX exporta; permiso dentro de servicios; denegación directa no crea archivo; autorizado exporta |
| T4.13 | COMPLETE | service/ReporteServiceTest.java (test); docs/casos_prueba.md | Dos áreas/puestos, retirado, general/filtros, encabezados/IDs/conteos/orden mediante POI; sin retirado ni columnas sensibles |
| T4.14 | COMPLETE | pruebas AutenticacionServiceTest.java, AutorizacionServiceTest.java, EmpleadoListaAutorizacionTest.java, AuditoriaFlujoPermisosTest.java, Sprint3BatchTest.java; service/EmpleadoService.java; service/SesionService.java | Credenciales correctas/incorrectas, inactiva, roles/permisos, revocación, logout y llamadas directas buscar/editar/importar/exportar; regresiones adaptadas y aprobadas |
| T4.15 | COMPLETE | dao/impl/EmpleadoDAOImpl.java; service/EmpleadoService.java; dao/impl/EmpleadoIntegridadTest.java (test); docs/casos_prueba.md | DPI duplicado, tres FK inválidas, nombre vacío, estado válido, mensajes seguros y datos anteriores intactos; sin huérfanos/parciales; suite y 107 comprobaciones previas pasan |
| T4.16 | COMPLETE | docs/prueba_integrada.md; test/fixtures/lote_valido.xlsx; service/PruebaIntegradaTest.java (test) | Autenticación real de cuenta ficticia/rol; importación/IDs/ficha/reporte/XLSX/logout; recorrido de servicios reproducido y documentado |
| T4.17 | COMPLETE | docs/incidencias.md; docs/prueba_integrada.md | Solo hallazgos reales, pasos/severidad/módulo/responsable/fecha/resultado; incidencia previa de Usuarios fuera de alcance documentada; sin implementar tarjetas de Daniel |
| T4.18 | COMPLETE | README.md; nbproject/project.properties; nbproject/dependencies-pom.xml; util/Conexion.java; config.properties.example | JDK/JavaFX/MySQL/JDBC/POI reales, NetBeans/VM/config/SQL/pruebas/errores; contraseña incorporada eliminada; compilación completa Ant pasa |
| T4.19 | COMPLETE | database/README.md; database/dbkinalrh_dev.sql | Orden BD descartable/base/migración/seed, MySQL/host/BD/cuenta limitada y comprobaciones; advertencia DROP; procedimiento usado en pruebas |
| T4.20 | COMPLETE | docs/guion_review.md; docs/feedback_po.md | Guion de 13–15 minutos y plantilla de observaciones PO; no se atribuye feedback ni una revisión todavía no realizada |
| T4.22 | COMPLETE | database/migrations/002_exportacion_reporte.sql; model/ExportacionReporte.java; dao/ExportacionReporteDAO.java; dao/impl/ExportacionReporteDAOImpl.java; service/AuditoriaService.java; service/ExportacionXlsxService.java | Arquitectura UUID/auditoría ya integrada, sin rechazo encontrado; tabla/FK/UNIQUE, UUID/usuario/tipo/fecha y auditoría transaccional; prueba coteja registro_id |
| T4.23 | COMPLETE | service/ExportacionXlsxService.java; service/ExportacionTrazabilidadTest.java (test) | Metadatos separado, dos folios diferentes, tabla/usuario/fecha coincidentes, cancelación sin traza, XLSX legible y fallos compensados |

## Verificación final del trabajo

- Rama conservada: `ft/sprint3-sprint4-integracion`; HEAD coincide con el develop
  integrado inspeccionado al empezar. Sin cambio de rama ni modificación de historial.
- Compilación completa: Ant `jar` con directorio de salida nuevo, JDK 21; además
  `javac` de todos los archivos de src y test. Resultado correcto.
- Diez ejecutables principales aprobados (incluido FXMLLoader y la suite previa
  de 107 comprobaciones) y diez ejecutables históricos ejecutados en aislamiento,
  incluidos agregados. No hay fallos funcionales pendientes en estas tarjetas.
- Incidencia abierta fuera de alcance: INC-09, mensaje SQL en duplicado de correo
  de usuarios. Aviso de proveedor Log4j ausente: no impide leer/escribir XLSX.
- Migración 002 aplicada y comprobada solo en BD descartable; debe aplicarse
  explícitamente en cada instalación antes de exportar.
- Git: 24 archivos existentes modificados, 36 nuevos, 60 en total; sin staged.
  `git diff --stat` de los archivos ya seguidos no incluye los nuevos sin seguimiento.
- Se revisaron conflictos, espacios, credenciales y referencias prohibidas en
  adiciones; no se crearon archivos de contexto ni instrucciones del repositorio.
- Cambios sin commit para revisión. No se hizo commit, push, merge ni rebase.
