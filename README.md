# Kinal RH

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

## Requisitos para ejecutar el proyecto

- JDK compatible con la versión de Java utilizada por el equipo.
- JavaFX SDK compatible con el JDK.
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

**Precaución:** El script SQL de desarrollo recibido para Kinal RH contiene una instrucción para eliminar y recrear `dbkinalrh`. Revisalo antes de ejecutarlo y **nunca lo corras sobre una base de datos con información real**.

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

**Última actualización de este README:** 24 de septiembre de 2026.
