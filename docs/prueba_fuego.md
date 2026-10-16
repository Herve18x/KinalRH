# Prueba de fuego — 15 de octubre de 2026

Trabajo realizado únicamente en `ft/prueba-fuego`, sin commit, push, merge,
rebase ni cambio de rama. No se modificaron esquemas ni datos reales.

Resultado final: **28 suites PASS**, compilación completa con `javac` y
empaquetado `ant jar` correctos. Se añadieron ocho ejecutables de regresión.
Se reprodujeron y corrigieron los siguientes grupos de defectos antes de
continuar con las demás pruebas.

## Defectos comprobados y corregidos

| ID | Reproducción / comportamiento anterior | Corrección y evidencia |
| --- | --- | --- |
| PF-01 | Usuarios, catálogos, logros y lectura de auditoría avanzaban sin sesión o con un visor pasando `ADMIN`. La matriz nueva detectó 59 fallos, incluido el helper de permisos. | Autorización mediante sesión y asociaciones activas de SQL; `AutorizacionModulosTest` verifica 58 rechazos de operaciones, el helper y concesión/revocación. |
| PF-02 | Una sesión admin conservaba acceso al desactivar su cuenta, desactivar el rol o eliminar su asociación; bastaba marcar el objeto de sesión como admin. | El acceso total exige una asociación admin activa en SQL, también en las comprobaciones con conexión transaccional. `RevocacionAdminTest`. |
| PF-03 | `SesionService.tienePermiso` conservaba un permiso eliminado de SQL durante la sesión. | Comprobación vigente en cada consulta. `RevocacionAdminTest`. |
| PF-04 | Directorio, formularios de empleado/usuario, logros, roles y bandeja entregaban datos a un visor sin permisos mediante DAO directos. | Guardas antes de consultar; directorio usa el servicio de empleados. `FxmlRecorridoTest` reproduce las seis filtraciones y comprueba su cierre. Bandeja también verifica permisos antes de crear o resolver. |
| PF-05 | La última cuenta admin podía desactivarse con `cambiarEstado` o edición, o perder su rol con asignación o edición. Solo `desactivar` tenía una guarda. | Invariante en las transacciones del DAO, compartida por las cinco vías y serializada por el rol admin. `UsuarioIntegridadSeguridadTest` y diez carreras de `UsuarioConcurrenciaTest`. |
| PF-06 | Las altas de usuarios almacenaban SHA-256 simple aunque existía el verificador PBKDF2. | Altas y cambios de contraseña usan PBKDF2; se conserva la compatibilidad de autenticación con cuentas antiguas. `UsuarioIntegridadSeguridadTest`, `UsuarioTransaccionTest` y suites de autenticación. |
| PF-07 | El error de correo duplicado publicaba el correo, constraint y excepción SQL en consola y causa de la excepción. | Errores de usuarios sin valores privados ni causas SQL expuestas; rollback explícito. `UsuarioIntegridadSeguridadTest` captura y revisa mensajes y causas. Resuelve la incidencia histórica INC-09. |
| PF-08 | Un correo con espacios en el dominio era aceptado. | Validación de formato y longitud; correo opcional vacío se normaliza a NULL. `UsuarioIntegridadSeguridadTest`. |
| PF-09 | Crear una cuenta con rol inactivo persistía la asociación. | Validación de rol existente/activo dentro de la transacción de alta, edición o asignación. `UsuarioIntegridadSeguridadTest`. |
| PF-10 | Listar usuarios con varios roles duplicaba cuentas. Los roles inactivos también se mapeaban como vigentes. | Una cuenta por ID, lista de roles activos y prioridad estable para admin. `UsuarioIntegridadSeguridadTest`; revocación cubierta adicionalmente por `RevocacionAdminTest`. |
| PF-11 | Leer una ruta raíz como `C:/` producía `NullPointerException`. | Validación de nombre de archivo antes de usarlo. `ImportacionBordesTest`. |
| PF-12 | Un DPI numérico de Excel pasaba la previsualización, pudiendo importar una representación redondeada o científica como otro identificador. | Se exige celda de texto para DPI y se muestra error por fila. `ImportacionBordesTest`. |
| PF-13 | La fecha `0000-01-01` pasaba la previsualización. | Validación del año entre 1000 y 9999. `ImportacionBordesTest`. |
| PF-14 | La jerarquía permitía A → B → C → A. | Recorrido completo de la cadena de jefes y detección de repetidos. `JerarquiaEmpleadoTest`. |
| PF-15 | Una excepción SQL al verificar la jerarquía se ignoraba. | El error interrumpe la validación y no autoriza guardar. `JerarquiaEmpleadoTest`, con fallo controlado del DAO. |
| PF-16 | Editar un empleado inexistente indicaba éxito y generaba auditoría de edición. | Se comprueba el resultado del UPDATE antes de auditar. `JerarquiaEmpleadoTest`. |
| PF-17 | Tras fallar una escritura de usuario/rol, reintentar con el mismo objeto volvía a hashear el hash y alteraba la contraseña efectiva. | Se restaura la entrada de contraseña ante error. `UsuarioTransaccionTest` fuerza fallos con triggers, comprueba rollback y autentica después del reintento de alta y edición. |

Las pruebas históricas de usuarios y alerta se preparan ahora con una sesión
administradora explícita. Sus fixtures tienen una asociación admin en el esquema
descartable; cambiar un texto de rol en memoria ya no basta. Se corrigieron además
salidas de éxito falsas en `AlertaColaboradorSinRolTest`, `UsuarioServiceTest` y
`SprintHerberthBatchTest`: los errores comprobados devuelven fallo del proceso.
La semilla y los permisos de la aplicación no se ampliaron.

## Matriz final

Cada fila corresponde a un ejecutable completo, no a una sola aserción.
Las diez pruebas históricas se ejecutaron mediante `PruebaLegadaAislada`.

| Grupo | Ejecutables | Estado |
| --- | --- | --- |
| Servicios existentes | ImportacionXlsxServiceTest, ReporteServiceTest, AutenticacionServiceTest, AutorizacionServiceTest, PendientesSprint3Test, ExportacionTrazabilidadTest, PruebaIntegradaTest | 7 PASS |
| DAO | EmpleadoIntegridadTest, EmpleadoDAOImplTest | 2 PASS; el segundo informa 107 comprobaciones |
| Históricas aisladas | UsuarioServiceTest, EmpleadoGestionTest, EmpleadoLogroMultipleTest, EmpleadoListaAutorizacionTest, CargaEmpleadosSimuladosTest, AuditoriaServiceTest, AuditoriaFlujoPermisosTest, AlertaColaboradorSinRolTest, Sprint3BatchTest, SprintHerberthBatchTest | 10 PASS |
| Regresiones de servicios | AutorizacionModulosTest, UsuarioIntegridadSeguridadTest, UsuarioConcurrenciaTest, ImportacionBordesTest, RevocacionAdminTest, JerarquiaEmpleadoTest, UsuarioTransaccionTest | 7 PASS |
| JavaFX | FxmlImportacionTest, FxmlRecorridoTest | 2 PASS; recorrido de 17 FXML, CSS/layout y siete vistas restringidas |
| Construcción | Compilar todas las fuentes de src/test; ant jar | PASS |
| Revisión del diff | git diff --check | PASS |
| Limpieza de datos de prueba | Consultar esquemas kinalrh_revision_* y kinalrh_prueba_sprint2_* al terminar | PASS; cero esquemas restantes |

No se consideró suficiente un exit code de cero: se revisaron también los logs
finales buscando aserciones, excepciones no controladas y marcadores de fallo de
las pruebas históricas. Apache POI emite el aviso ya conocido de proveedor Log4j
ausente; no impidió lectura, escritura ni validación de los XLSX.

## Aislamiento y bloqueos

Se creó una instancia MySQL 8.0.46 nueva en
`build/prueba-fuego/mysql-data`, enlazada a `127.0.0.1:3317`, sin servicio Windows.
Se comprobó `@@datadir` y `@@port` antes de ejecutar SQL. Cada prueba creó y
eliminó su esquema propio; la configuración temporal permaneció dentro de
`build/prueba-fuego`. No se usó el servicio MySQL80 del equipo para las pruebas.
La instancia descartable se cerró al terminar.

| Operación | Estado | Motivo / continuación |
| --- | --- | --- |
| Consultar procesos con Get-CimInstance Win32_Process | BLOCKED | Windows devolvió acceso denegado. Se continuó con comprobaciones independientes del servidor descartable. |
| Lanzar un archivo de automatización PowerShell .ps1 | BLOCKED | La política de Windows deshabilita scripts. No se cambió ni omitió esa política; el runner no ejecutado no se incorporó al repositorio. Las suites Java y Ant se verificaron como pruebas independientes. |

No hubo solicitudes de autorización, elevación de privilegios ni cambios de
política. No se realizó una sesión manual de clics ni se probaron caídas del
sistema operativo durante un commit. La carga de FXML y las pruebas de servicios
no sustituyen esas comprobaciones. El flujo completo de aprobaciones de varias
etapas y la concurrencia de ediciones jerárquicas no están certificados por esta
ejecución.

## Repetición y evidencia

La preparación general está en el README. Se necesitan JDK 21, JavaFX 21.0.11 y
los JAR existentes en `lib`. Compilar desde la raíz:

```powershell
New-Item -ItemType Directory -Force build/prueba-fuego/classes | Out-Null
$fuentesFuego = Get-ChildItem src,test -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -cp 'C:/javafx-sdk-21.0.11/lib/*;lib/*' -d build/prueba-fuego/classes $fuentesFuego
```

Usar exclusivamente una instancia MySQL descartable y verificar su directorio
antes de ejecutar pruebas. Desde `build/prueba-fuego`, sin configuración previa,
los nuevos ejecutables se invocan igual que los existentes, uno después de otro:

```powershell
java -cp 'classes;../../lib/*' org.kinalrh.service.AutorizacionModulosTest C:/Users/herbe/Documents/KinalRH
java -cp 'classes;../../lib/*' org.kinalrh.service.UsuarioIntegridadSeguridadTest C:/Users/herbe/Documents/KinalRH
java -cp 'classes;../../lib/*' org.kinalrh.service.UsuarioConcurrenciaTest C:/Users/herbe/Documents/KinalRH
java -cp 'classes;../../lib/*' org.kinalrh.service.ImportacionBordesTest C:/Users/herbe/Documents/KinalRH
java -cp 'classes;../../lib/*' org.kinalrh.service.RevocacionAdminTest C:/Users/herbe/Documents/KinalRH
java -cp 'classes;../../lib/*' org.kinalrh.service.JerarquiaEmpleadoTest C:/Users/herbe/Documents/KinalRH
java -cp 'classes;../../lib/*' org.kinalrh.service.UsuarioTransaccionTest C:/Users/herbe/Documents/KinalRH
java --module-path C:/javafx-sdk-21.0.11/lib --add-modules javafx.controls,javafx.fxml -cp 'classes;../../src;../../lib/*' org.kinalrh.service.FxmlRecorridoTest C:/Users/herbe/Documents/KinalRH
```

Los ejecutables usan `KINALRH_TEST_HOST/PORT/USER/PASSWORD`, según el README.
No ejecutar procesos de prueba simultáneos en el mismo directorio: comparten el
archivo temporal `config.properties`. La concurrencia interna de
`UsuarioConcurrenciaTest` está controlada por su propio fixture.

Los logs locales, ignorados por Git, están en `build/prueba-fuego/logs`:
`antes-*.log` conserva reproducciones; `despues-*.log` las verificaciones de los
fixes; `final-*.log` la ejecución final de las 28 suites y `final-ant-jar.log`
el empaquetado. Los logs históricos contienen solo datos ficticios de fixtures.
El intento de runner bloqueado está en `build/prueba-fuego/ejecucion-final.log`.

## Segunda auditoría

Fecha: **15 de octubre de 2026**. Ejecución real con escritura en
`ft/prueba-fuego`. Se conservaron los cambios pendientes de la primera prueba.
No se hicieron commit, push, merge, rebase, cambio de rama ni cambios en Trello.
Las conclusiones de la auditoría anterior en modo de solo lectura no se usaron
como evidencia. Resultado: **32 suites PASS en cuatro ejecuciones completas
consecutivas (128/128)**; compilación de las **109 fuentes** de `src` y `test`
correcta y `ant jar` terminado con **BUILD SUCCESSFUL**.

### Hallazgos investigados y correcciones

| Caso | Resultado reproducido | Corrección / comprobación |
| --- | --- | --- |
| A / SA-01 | **CONFIRMADO**: con solo `EMPLEADO_CREAR`, `FXMLLoader` rechazaba `FormularioEmpleado.fxml` por faltar `EMPLEADO_VER`. | La carga de catálogos acepta VER, CREAR o EDITAR. El servicio sigue exigiendo CREAR para altas y EDITAR para modificaciones. Se probó abrir y guardar desde el controlador con solo CREAR, solo VER, ambos y ninguno. |
| B / SA-02 | **CONFIRMADO**: alta y edición con `idEmpleado=0` fallaban por la FK. El DAO de HEAD convertía los valores no positivos a SQL NULL en ambas rutas. `usuario.id_empleado` es nullable y referencia a `empleado`. | Se restauró esa normalización en ambos PreparedStatement. Cero era una entrada aceptada por el DAO para «sin empleado»; no era una FK válida en SQL. La prueba verifica el NULL recuperado de la BD. |
| C / SA-03 | **CONFIRMADO**: un usuario con `CAMBIO_APROBAR_RH` y sin `CAMBIO_CREAR` era autorizado, pero se insertaba un falso `ACCESO_DENEGADO`. El rechazo total también generaba un evento por alternativa. | Las alternativas se comprueban sin auditar cada rechazo parcial. Si ninguna autoriza, se registra un único evento, con el primer permiso solicitado como referencia. La prueba consulta el conteo real de auditoría antes y después de autorización y rechazo. |
| D / SA-04 | **CONFIRMADO en el fixture**: `PruebaLegadaAislada` añadía admin a `encargado_demo`, haciendo que la comprobación de acceso utilizara el bypass administrador. Esto invalidaba la evidencia específica del perfil encargado. | Se eliminó la asociación admin y se declararon permisos SQL por ejecutable según las operaciones que ejercita. El wrapper verifica que no exista ninguna asociación admin. Las diez suites históricas pasan así; no se demostró un defecto adicional de negocio que estuviera siendo ocultado. |
| SA-05, nuevo | **CONFIRMADO**: dos asignaciones recíprocas de jefe podían validar la jerarquía inicial antes de guardar y confirmar ambas escrituras, formando A → B → A. | El DAO revalida la cadena dentro de la transacción de alta/edición, con una lectura vigente `FOR UPDATE` de la jerarquía ordenada por ID. El segundo cambio incompatible se rechaza y revierte. Una barrera en el fixture fuerza las dos validaciones iniciales y ordena los commits para reproducir la carrera de manera determinista. |

No se descartó completamente ninguno de los cuatro hallazgos A–D. Sí quedaron
**DESCARTADAS** estas hipótesis: que cero fuese una entrada incompatible con el
contrato anterior del DAO; que admin fuese imprescindible para las pruebas
históricas; y que las nuevas guardas necesariamente bloquearan también sus rutas
autorizadas. Estas últimas funcionan con permisos SQL específicos. No se afirma
que el fixture anterior ocultase un bug adicional de permisos ya identificado.

Las reproducciones anteriores a los fixes fallaron mediante aserciones en los
ejecutables nuevos, usando las clases de producción compiladas antes de los
cambios. Evidencia local: `antes-regresiones.log`, `antes-fxml.log` y
`antes-concurrencia.log`, dentro de `build/segunda-auditoria/logs`. Un primer
intento de carrera jerárquica simultánea terminó con un error transaccional;
la reproducción determinista posterior es la que demostró la doble confirmación.
Una selección incorrecta de solicitud en el test de bandeja se corrigió en el
propio test; no se contó como bug de la aplicación.

### Revisión de pruebas y autorización

Se revisaron los diffs de las pruebas modificadas en la primera prueba de fuego.
No se retiraron aserciones ni se sustituyeron errores por warnings. Se mantuvo
el fallo del proceso ante errores del lote y se reforzaron comprobaciones que
solo imprimían mensajes: aparición/desaparición de la alerta, cuenta desactivada
persistida, ausencia de filas parciales, cuatro eventos de auditoría asociados a
sus cuentas/UUID y los resultados de catálogos, niveles y auditoría de
`Sprint3BatchTest`. Sus excepciones de ejecución y limpieza ahora fallan la prueba.
La carrera del último admin comprueba además que el rechazo se deba a esa
invariante, no a cualquier error SQL.

`encargado_demo` conserva el perfil encargado. El wrapper añade únicamente
permisos explícitos de los módulos ejercitados por cada suite; el lote recibe
la unión necesaria para sus subpruebas. La prueba de lista del encargado conserva
solo su permiso original `EMPLEADO_VER`. Las pruebas históricas que pasan el texto
`ADMIN` en memoria no obtienen por ello permisos SQL. El test DAO del retiro usa
un visor con `EMPLEADO_CAMBIAR_ESTADO`, sin admin. Los fixtures que prueban
explícitamente administradores y último admin sí conservan ese rol.

`SegundaAutorizacionFxmlTest` carga las vistas con un actor sin admin y comprueba
datos reales; después revoca el permiso e invoca de nuevo la guarda esperando
`SecurityException`. Se ejercitaron ambos sentidos de todas las guardas nuevas:

| Controlador | Guardas / rutas comprobadas con y sin permiso |
| --- | --- |
| BandejaController | Cargar solicitudes con cada una de las tres alternativas; obtener empleados, abrir/cancelar el diálogo y guardar solicitud con CREAR; resolver con cada uno de los dos permisos de aprobación. Alta y resoluciones se verifican en SQL. |
| DirectorioController | `cargarEmpleados` con `EMPLEADO_VER`. |
| EmpleadoController | `cargarCatalogosFiltros` con `EMPLEADO_VER`; inicialización con lista de empleados. |
| EmpleadoFormController | `cargarCatalogos`; solo CREAR abre y guarda; solo VER abre y rechaza el alta; ambos abren y guardan; ninguno rechaza la carga. |
| LogroController | `cargarEmpleados` con VER o GESTIONAR, y rechazo sin ambos. |
| RolesController | `cargarRoles` y `cargarPermisosDeRol` con `ROL_GESTIONAR`, y rechazo al revocarlo. |
| UsuarioController | `configurarFormulario` con `USUARIO_GESTIONAR`, y rechazo al revocarlo. |

Las suites de servicios volvieron a comprobar login correcto/incorrecto, logout,
sesión nula, revocación de permisos/asociación, cuenta y rol desactivados durante
sesión, último administrador, altas/ediciones/cambio de rol de usuario, altas y
ediciones de empleado, filtros, logros, jerarquía, importación, reportes,
exportación, auditoría y rollback con fallos forzados por triggers/proxies.

### Suites, repeticiones y evidencia

Se ejecutaron las **28 suites de la matriz anterior**, incluyendo las diez
históricas a través de `PruebaLegadaAislada`, y cuatro nuevas:

- `SegundaRegresionesTest`: normalización cero en alta/edición y auditoría de alternativas.
- `SegundaAutorizacionFxmlTest`: guardas positivas/negativas y cuatro perfiles del formulario.
- `SegundaConcurrenciaTest`: revocar/asignar admin, dos ediciones del mismo usuario y jerarquía.
- `ImportacionMilFilasTest`: 1000 altas, 1000 actualizaciones posteriores, IDs únicos y vínculos de área.

Tras los cambios se realizó una ejecución completa y **tres repeticiones
completas adicionales**, siempre secuenciales para no compartir configuración
entre procesos. Cada ejecución terminó **32/32 PASS**; no se observaron fallos
intermitentes. Los logs finales son `final1-*.log` hasta `final4-*.log`; los 128
códigos de salida están en `final-resultados.txt`. Se revisaron también los
mensajes buscando fallos y excepciones no controladas. El recorrido FXML imprime
una `SecurityException` capturada por el controlador de logros al probar el visor
sin permisos; corresponde a la prueba negativa, no a una carga autorizada fallida.
Permanece el aviso de Apache POI por ausencia de proveedor Log4j.

**Concurrencia:** cada ejecución incluye diez carreras del último admin y diez
de cada escenario adicional: revocar/asignar admin, edición del mismo usuario y
cambio jerárquico recíproco. Son **160 carreras en las cuatro ejecuciones**.
Se conservó al menos un admin, cada edición de usuario dejó una fila/rol
coherentes y los cambios jerárquicos incompatibles no confirmaron un ciclo.

**XLSX:** archivo válido, inválido, corrupto, DPI numérico, fecha inválida,
cancelación, rollback íntegro, carga de 1000 filas y actualización de esas mismas
1000 filas, exportaciones consecutivas y folios distintos vinculados con usuario,
fecha y auditoría. Todos se repitieron en las cuatro ejecuciones.

**FXML:** carga real de los **17 archivos**, `applyCss` y layout; **199 fx:id**
resueltos, comprobación de inyección donde existe campo de controlador,
**53 onAction** enlazados y **15 referencias CSS** legibles. `fx:controller`
coincide con el controlador cargado; las dos vistas estáticas sin controlador
se validan como tales. No se añadieron controladores a esas vistas estáticas.

### Entorno, construcción, seguridad y riesgos restantes

Se inicializó otra instancia MySQL 8.0.46 exclusivamente descartable, sin servicio
Windows, en `build/segunda-auditoria/mysql-data`, ligada a `127.0.0.1:3318`.
Se verificaron `@@datadir` y `@@port` antes de las pruebas. No se utilizó la BD
real ni la instancia de la primera auditoría. Al terminar se comprobaron **cero
esquemas de prueba restantes**, ausencia de `config.properties` temporal y cierre
correcto de la instancia. No hubo operaciones BLOCKED en esta segunda ejecución.

Se compilaron las 109 fuentes con JDK 21 y JavaFX 21.0.11. El `ant jar` final
se ejecutó usando el Ant incluido en NetBeans 21, con **BUILD SUCCESSFUL**;
evidencia en `final-ant-jar.log`. La configuración temporal de las pruebas usó
`KINALRH_TEST_PORT=3318`; los demás parámetros son los del fixture descartable.

`git diff --check` sin errores. La búsqueda de marcadores de conflicto solo
encontró el separador decorativo existente de `styles.css`, sin conflictos.
Se revisaron los archivos versionados/no ignorados buscando claves privadas,
patrones de tokens y asignaciones de credenciales: no se detectaron secretos
reales nuevos. La coincidencia de contraseña literal corresponde a un fixture
ficticio de usuarios. No se publicaron sus valores. Los logs y datos de servidor
permanecen bajo `build`, ignorados por Git.

Riesgos / límites todavía presentes:

- La protección jerárquica bloquea la jerarquía completa al modificar un jefe.
  Evita la carrera reproducida, pero puede aumentar la contención en plantillas
  grandes. No se certificó rendimiento de cambios jerárquicos masivos ni todos
  los órdenes posibles de escrituras concurrentes de otros módulos.
- Dos ediciones válidas del mismo usuario se serializan y la última escritura
  prevalece; no hay detección de formularios desactualizados ni fusión de campos.
- Se probaron controladores y diálogos mediante JavaFX, no una sesión manual
  completa de navegación. El flujo de aprobaciones de múltiples etapas y las
  caídas del sistema operativo durante un commit siguen sin certificarse.
- El escaneo de secretos es una revisión local por patrones y contexto, no una
  garantía sobre toda la historia del repositorio.

No quedaron regresiones reproducidas sin corregir dentro de los escenarios
ejecutados. **Listo para integrar: SÍ CON RIESGOS**, por los límites anteriores.
