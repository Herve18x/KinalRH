# Reglas de DPI y Campos Mínimos del Colaborador — T2.10

**HU:** US-44 / US-45  
**Responsable:** Herberth  
**Sprint:** 2  
**Módulo:** Gestión de Colaboradores (KinalRH)

---

## 1. Objetivo del Documento

Establecer la especificación técnica y de negocio respecto a los **campos mínimos obligatorios** para registrar un colaborador en el sistema KinalRH, así como las **reglas de validación, formato y unicidad del Documento Personal de Identificación (DPI / CUI)** conforme a la normativa guatemalteca (RENAP) y el modelo de datos relacional.

---

## 2. Campos Mínimos Obligatorios del Colaborador

Para que la creación de un colaborador sea admitida por el servicio `EmpleadoService` y persistida en la tabla `empleado`, deben proporcionarse obligatoriamente los siguientes campos:

| Campo BD | Atributo Java | Tipo de Dato | Restricción | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `primer_nombre` | `primerNombre` | `VARCHAR(80)` | `NOT NULL` | Primer nombre legal del colaborador. No admite cadenas vacías. |
| `primer_apellido` | `primerApellido` | `VARCHAR(80)` | `NOT NULL` | Primer apellido legal del colaborador. No admite cadenas vacías. |
| `dpi` | `dpi` | `VARCHAR(20)` | `NOT NULL, UNIQUE` | Código Único de Identificación (CUI) de 13 dígitos numéricos. |
| `id_estado_empleado`| `idEstadoEmpleado` | `BIGINT` | `NOT NULL, FK` | Estado laboral inicial (por defecto `ACTIVO`, referenciando el catálogo `estado_empleado`). |
| `fecha_contratacion`| `fechaContratacion`| `DATE` | `NOT NULL` | Fecha en la que el colaborador inicia funciones laborales. |

### 2.1 Campos Complementarios y Opcionales

Aunque no bloquean la inserción inicial, se recomienda su captura para completar la ficha institucional:
* `segundo_nombre`, `otros_nombres`, `segundo_apellido`, `apellido_casada`.
* `nit`: Obligatorio si se emitirán pagos o planillas. Si se proporciona, debe ser único en BD (`uq_empleado_nit`).
* `correo_personal` / `correo_institucional`: Deben cumplir con formato de correo electrónico estándar.
* `telefono_personal` / `telefono_emergencia`.
* `id_area_principal`, `id_puesto_actual`, `id_nivel_academico`, `id_jefe_inmediato`: Llaves foráneas a sus respectivos catálogos.

---

## 3. Reglas de Validación del DPI (CUI)

El DPI en Guatemala está compuesto por el Código Único de Identificación (CUI) emitido por el Registro Nacional de las Personas (RENAP).

### 3.1 Longitud y Formato
1. **Longitud Exacta:** Debe contener exactamente **13 dígitos numéricos**.
2. **Expresión Regular:** `^[0-9]{13}$`
3. **Estructura Interna:**
   * **Dígitos 1 al 8:** Número correlativo de inscripción registral.
   * **Dígito 9:** Dígito verificador generado por algoritmo módulo 11 de RENAP.
   * **Dígitos 10 y 11:** Código de departamento de origen (rango de `01` a `22`).
   * **Dígitos 12 y 13:** Código de municipio dentro del departamento seleccionado.

### 3.2 Reglas de Almacenamiento y Presentación
* **En Base de Datos (Persistencia):** Se almacena como una cadena continua de 13 dígitos sin espacios ni guiones (ej. `2548912340101`).
* **En Interfaz de Usuario (Vista):** Se admite visualización con máscara de lectura para el usuario final: `2548 91234 0101`. Al procesar el guardado, la capa de controlador o servicio limpia los separadores.

### 3.3 Regla de Unicidad Estricta
* **Nivel Base de Datos:** La tabla `empleado` cuenta con la restricción declarativa:
  ```sql
  CONSTRAINT uq_empleado_dpi UNIQUE (dpi)
  ```
* **Nivel Servicio (`EmpleadoService`):** Antes de enviar el comando `INSERT` o `UPDATE` a MySQL, el servicio consulta preventivamente:
  ```java
  if (emp.getDpi() != null && empleadoDAO.existeDpi(emp.getDpi(), idExcluido)) {
      throw new IllegalArgumentException("Error: Ya existe un empleado registrado con el DPI " + emp.getDpi());
  }
  ```
* **En Edición (`UPDATE`):** El parámetro `idExcluido` contiene el `id_empleado` actual, permitiendo que el usuario edite otros campos de su ficha sin que su propio DPI genere un falso positivo de duplicidad.

---

## 4. Política de Manejo de Errores

1. **Mensajes Comprensibles:** Ante un intento de registrar un DPI ya existente, el usuario recibe el mensaje de alerta claro:  
   `"Error: Ya existe un empleado registrado con el DPI [DPI_INGRESADO]"`
2. **Seguridad y Trazabilidad:**
   * No se exponen trazas internas de MySQL, nombres de tablas ni cadenas de conexión en las alertas visuales.
   * Toda colisión o rechazo es registrado en la auditoría del sistema cuando aplica un intento no autorizado.
3. **Atomicidad:** Al detectarse duplicidad en la capa de servicio, la transacción no llega a ejecutarse en la base de datos, garantizando que el correlativo de llaves primarias no sufra incrementos innecesarios ni se generen filas parciales o huérfanas.
