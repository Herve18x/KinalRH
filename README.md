# Kinal RH

## Dependencias XLSX

La importación usa Apache POI **5.5.1** (`poi-ooxml`, `WorkbookFactory`), versión
publicada en el [sitio oficial de Apache POI](https://poi.apache.org/download.cgi).
`nbproject/dependencies-pom.xml` fija esta versión y Connector/J **8.0.33**.
Ejecute `mvn -f nbproject/dependencies-pom.xml dependency:copy-dependencies` para
preparar `lib/` con sus dependencias transitivas antes de compilar en NetBeans/Ant.
Los binarios no se versionan. Consulte `docs/plantilla_importacion.md` para el
formato y los permisos requeridos.

**Sistema de gestión de Recursos Humanos · Fundación Kinal**  
**Estado:** En desarrollo · **Modalidad:** Aplicación de escritorio

## Descripción

Kinal RH es un proyecto de software para centralizar la información de colaboradores y facilitar su consulta y administración. Busca reducir la dispersión de registros y apoyar el trabajo del área de Recursos Humanos mediante una aplicación de escritorio con acceso controlado.

El equipo desarrollará la interfaz con **JavaFX** y utilizará **MySQL** para almacenar los datos. No se contempla una aplicación web en el alcance técnico propuesto.

## Objetivos

- Organizar la información de colaboradores en una base de datos centralizada.
- Facilitar la búsqueda, consulta y actualización de registros.
- Establecer autenticación y controles de acceso según los permisos definidos.
- Ofrecer una interfaz clara para las operaciones de Recursos Humanos.

## Tecnologías

| Tecnología | Uso |
| --- | --- |
| Java | Lógica de la aplicación |
| JavaFX y FXML | Interfaz de escritorio |
| MySQL | Base de datos |
| JDBC | Comunicación entre Java y MySQL |
| Apache NetBeans | Entorno de desarrollo |
| Git y GitHub | Control de versiones y colaboración |

## Alcance y planificación

El proyecto se organiza con **Scrum** en una propuesta de **cuatro Sprints**, a ajustar según la revisión del equipo y del profesor. Las siguientes son metas de trabajo, no funcionalidades que se declaren terminadas:

| Sprint | Meta prevista |
| --- | --- |
| 1 | Base de datos de desarrollo, modelos iniciales, conexión, login y navegación básica. |
| 2 | Gestión básica de usuarios, catálogos y fichas de colaboradores. |
| 3 | Consultas, filtros, organización de colaboradores y registro de información complementaria priorizada. |
| 4 | Importación acotada desde Excel, reportes básicos, pruebas e integración final. |

> El alcance final se ajustará a los requisitos aceptados y al tiempo disponible. Las funciones no aprobadas o no implementadas no deben marcarse como completas.

## Arranque verificado — Sprint 4

Se compiló con **JDK 21**, **JavaFX SDK 21.0.11**, **MySQL 8.0.46**,
**Connector/J 8.0.33** y **Apache POI 5.5.1**. El proyecto requiere Java 21
(`javac.source/target=21`). Ant incluido en NetBeans 21 también fue verificado.

1. Instale JDK 21 y defina `JAVA_HOME` apuntando al JDK, no a su carpeta `bin`.
2. Descargue JavaFX 21 para su plataforma. En NetBeans, abra esta carpeta como
   proyecto Java con Ant y seleccione JDK 21 en Propiedades → Libraries.
3. Prepare las dependencias con Maven (el incluido en NetBeans también sirve):
   `mvn -f nbproject/dependencies-pom.xml dependency:copy-dependencies`.
   Se copian los JAR a `lib/`, ignorados por Git. No borre `lib/` al limpiar build.
4. Ajuste `javafx.lib.dir` en `nbproject/private/private.properties` a la carpeta
   `lib` de su SDK. La ruta compartida por defecto es `C:/javafx-sdk-21.0.11/lib`.
5. Prepare la BD descartable siguiendo [database/README.md](database/README.md):
   crear BD → script base adaptado → migración 002 → semilla adaptada.
6. Copie `config.properties.example` como `config.properties` en la raíz; configure
   host, puerto, BD descartable, cuenta limitada y contraseña **solo localmente**.
   La aplicación no incorpora una contraseña de conexión por defecto.
7. Ejecute `ant jar` o Clean and Build en NetBeans, y luego Run. Configure estas
   opciones de VM (NetBeans ya usa `javafx.lib.dir`):

   ```text
   --module-path "C:/ruta/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml
   ```

### Pruebas reproducibles

Las pruebas son ejecutables Java con aserciones, no JUnit. Después de preparar
`lib/`, estos comandos PowerShell compilan todo sin datos reales:

```powershell
New-Item -ItemType Directory -Force build/revision/classes | Out-Null
$fuentesPrueba = Get-ChildItem src,test -Recurse -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -cp "C:/javafx-sdk-21.0.11/lib/*;lib/*" -d build/revision/classes $fuentesPrueba
```

Use una instancia MySQL **exclusiva de pruebas**. Los ejecutables nuevos usan
`KINALRH_TEST_HOST` (127.0.0.1), `KINALRH_TEST_PORT` (3317), `KINALRH_TEST_USER`
(root solo en la instancia descartable) y `KINALRH_TEST_PASSWORD` (configurada
localmente). La cuenta de pruebas necesita CREATE/DROP; la de aplicación no.
Desde `build/revision`, sin `config.properties` previo:

```powershell
$raizPrueba = (Resolve-Path ../..).Path
java -cp "classes;../../lib/*" org.kinalrh.service.ImportacionXlsxServiceTest $raizPrueba
java -cp "classes;../../lib/*" org.kinalrh.service.ReporteServiceTest $raizPrueba
java -cp "classes;../../lib/*" org.kinalrh.service.AutenticacionServiceTest $raizPrueba
java -cp "classes;../../lib/*" org.kinalrh.service.AutorizacionServiceTest $raizPrueba
java -cp "classes;../../lib/*" org.kinalrh.dao.impl.EmpleadoIntegridadTest $raizPrueba
java -cp "classes;../../lib/*" org.kinalrh.service.PendientesSprint3Test $raizPrueba
java -cp "classes;../../lib/*" org.kinalrh.service.ExportacionTrazabilidadTest $raizPrueba
java -cp "classes;../../lib/*" org.kinalrh.service.PruebaIntegradaTest $raizPrueba
java -cp "classes;../../lib/*" org.kinalrh.dao.impl.EmpleadoDAOImplTest "$raizPrueba/database/dbkinalrh_dev.sql" "jdbc:mysql://127.0.0.1:3317/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
java --module-path C:/javafx-sdk-21.0.11/lib --add-modules javafx.controls,javafx.fxml -cp "classes;../../src;../../lib/*" org.kinalrh.service.FxmlImportacionTest
```

Cada proceso crea y elimina su propio esquema; ejecute secuencialmente porque
el archivo de configuración temporal es compartido por directorio. Los fixtures
y la plantilla se pueden regenerar con `org.kinalrh.service.FixturesImportacion`
pasando la raíz como argumento, desde una carpeta de trabajo local.
Para ejecutar un caso histórico de forma aislada, use por ejemplo
`java -cp "classes;../../lib/*" org.kinalrh.service.PruebaLegadaAislada $raizPrueba org.kinalrh.service.EmpleadoGestionTest`.
Resultados: [docs/casos_prueba.md](docs/casos_prueba.md) y
[docs/prueba_integrada.md](docs/prueba_integrada.md).

### Errores comunes

- `JAVA_HOME` inválido o `invalid source release: 21`: seleccione JDK 21 también
  para Maven/Ant y reinicie la terminal o NetBeans.
- `JavaFX runtime components are missing`, `Module javafx.controls not found`:
  compruebe la ruta del SDK y las opciones de VM, no solo el classpath del editor.
- `ClassNotFoundException: com.mysql.cj.jdbc.Driver` o clases `org.apache.poi`:
  prepare `lib/` mediante el POM y reconstruya; no basta con copiar un único JAR POI.
- `Communications link failure` o `Access denied`: revise servicio MySQL, puerto,
  base, cuenta limitada y configuración local. No publique la excepción completa.
- `Unknown table exportacion_reporte`: aplique la migración 002 en la BD elegida.
- POI puede indicar que no encontró un proveedor Log4j. La lectura/escritura sigue
  funcionando; no registre datos de empleados para diagnosticarlo.

La previsualización detallada T4.04 y el controlador/vista funcional de reportes
T4.10 continúan a cargo de Daniel. Los servicios y sus contratos se prueban
directamente; no se declara terminado el recorrido visual de esas tarjetas.

## Requisitos para ejecutar el proyecto

- JDK 21.
- JavaFX SDK 21.0.11.
- Apache NetBeans.
- MySQL Server.
- Controlador MySQL Connector/J configurado en el proyecto.
- Acceso a una base de datos **local de desarrollo**.

## Configuración local

1. Cloná el repositorio desde su página de GitHub y abrí el proyecto en NetBeans.
2. Configurá el JDK y las librerías de JavaFX.
3. En **Propiedades del proyecto → Run → VM Options**, ajustá la ruta de JavaFX para tu computadora:

   ```text
   --module-path "C:\ruta\a\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml
   ```

4. Revisá el script SQL del proyecto y prepará una base de datos **exclusiva para pruebas** en MySQL.
5. Configurá localmente la conexión a la base de datos y ejecutá la aplicación desde NetBeans.

**Precaución:** `dbkinalrh_dev.sql` crea y selecciona `IN4CM` con `USE`. Adapte una copia al nombre de la BD descartable siguiendo `database/README.md`; **nunca lo ejecute sobre una base con información real**. Cualquier DROP debe limitarse a una copia segura identificada.

No subás contraseñas, archivos `.env` ni configuraciones privadas al repositorio. Las rutas de JavaFX y las credenciales de MySQL pueden ser diferentes en cada computadora.

## Equipo de desarrollo

| Integrante | Responsabilidad inicial |
| --- | --- |
| Herberth | Scrum Master, coordinación e implementación de entidades iniciales |
| Zabala | SQL, base de datos y conexión MySQL |
| Alexis | Login y autenticación |
| Keneth | Entidades iniciales y apoyo en desarrollo |
| Daniel | Interfaz gráfica y seguridad |

Las responsabilidades pueden ajustarse durante la planificación y el desarrollo.

## Seguimiento del proyecto

El trabajo se organiza con **Product Backlog**, **Sprint Backlog**, revisión de cambios en Git y validación de entregas con el profesor. Este README se actualizará cuando cambien las instrucciones de instalación, el alcance o las funcionalidades efectivamente terminadas.

**Última actualización de este README:** 14 de octubre de 2026.
