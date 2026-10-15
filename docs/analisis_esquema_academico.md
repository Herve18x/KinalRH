# Análisis de Brecha en el Esquema de Formación Académica — T3.13

**HU:** US-19 / US-20  
**Responsable:** Herberth  
**Sprint:** 3  
**Módulo:** Gestión del Perfil Académico del Colaborador (KinalRH)

---

## 1. Contexto y Diagnóstico del Modelo Actual

En la versión actual de la base de datos (`database/dbkinalrh_dev.sql`), la relación entre el colaborador y su formación académica se modela a través de una única llave foránea en la tabla `empleado`:

```sql
ALTER TABLE empleado 
ADD COLUMN id_nivel_academico BIGINT UNSIGNED NULL,
ADD CONSTRAINT fk_empleado_nivel_academico 
    FOREIGN KEY (id_nivel_academico) REFERENCES nivel_academico(id_nivel_academico);
```

Y el catálogo `nivel_academico` únicamente cuenta con los siguientes atributos:
* `id_nivel_academico`: Identificador numérico.
* `codigo`: Código nemotécnico (ej. `LIC`, `MAE`, `TEC`).
* `nombre`: Descripción del nivel (ej. `Licenciatura`, `Maestría`).
* `orden_jerarquico`: Nivel de prelación académica.
* `activo`: Bandera de vigencia del catálogo.

---

## 2. Datos Faltantes Detectados en el Esquema Actual

El diseño actual solo responde a la pregunta *"¿Cuál es el nivel educativo más alto alcanzado por el colaborador?"*, pero carece de la información específica requerida por el departamento de Recursos Humanos para auditorías institucionales y perfiles de puesto.

Los atributos críticos faltantes son:

| Atributo Faltante | Tipo Propuesto | Justificación de Negocio en RRHH |
| :--- | :--- | :--- |
| **`titulo_obtenido`** | `VARCHAR(150)` | Nombre oficial de la carrera, diplomado o título obtenido (ej. *Ingeniería en Ciencias y Sistemas*, *Licenciatura en Pedagogía*, *Perito en Dibujo Técnico*). |
| **`institucion_educativa`** | `VARCHAR(180)` | Nombre de la universidad, instituto o centro de estudios que emitió la acreditación (ej. *Universidad de San Carlos de Guatemala*, *Kinal*, *Universidad Rafael Landívar*). |
| **`año_graduacion`** | `INT` / `YEAR` | Año o fecha exacta de graduación/egreso, indispensable para calcular antigüedad y vigencia de conocimientos. |
| **`estado_estudio`** | `ENUM / VARCHAR(30)` | Permite distinguir entre *Graduado*, *Cierre de Pensum*, *En Curso* o *Trunco*. |
| **`numero_colegiado`** | `VARCHAR(40)` | Número de colegiado profesional en Guatemala (Colegio de Ingenieros, Humanistas, Abogados, etc.), indispensable para cargos docentes y directivos. |
| **`fecha_colegiacion`** | `DATE` | Fecha de colegiación profesional y verificación de colegiado activo. |

---

## 3. Limitaciones Funcionales Provocadas por la Brecha

1. **Imposibilidad de registrar múltiples títulos:**  
   Un colaborador puede tener Diversificado como Perito en Electrónica, Licenciatura en Administración y Maestría en Finanzas. El esquema actual de 1 a 1 (`empleado -> nivel_academico`) sobrescribe los niveles anteriores y pierde el historial académico.
2. **Falta de respaldo documental:**  
   No existe trazabilidad de la institución de procedencia para cotejar la autenticidad de los diplomas ante el Ministerio de Educación o el Consejo de la Enseñanza Privada Superior (CEPS).
3. **Reportes institucionales incompletos:**  
   En la vista `vw_empleados_por_nivel_academico` solo es posible agrupar cuántos colaboradores tienen "Licenciatura", pero no es posible filtrar quiénes son ingenieros, administradores o psicólogos.

---

## 4. Propuesta de Migración y Solución DDL

Para solucionar esta brecha sin romper la compatibilidad regresiva con el código existente de los Sprints 1 y 2, se proponen dos alternativas:

### Alternativa A (Inmediata / Columnas Directas en `empleado`):
Adecuada para un solo grado académico representativo:
```sql
ALTER TABLE empleado
ADD COLUMN titulo_obtenido VARCHAR(150) NULL AFTER id_nivel_academico,
ADD COLUMN institucion_educativa VARCHAR(180) NULL AFTER titulo_obtenido,
ADD COLUMN anio_graduacion SMALLINT UNSIGNED NULL AFTER institucion_educativa,
ADD COLUMN numero_colegiado VARCHAR(40) NULL AFTER anio_graduacion;
```

### Alternativa B (Recomendada / Tabla Relacional 1 a N):
Permite almacenar el historial educativo completo de cada colaborador:
```sql
CREATE TABLE IF NOT EXISTS empleado_formacion_academica (
    id_formacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_empleado BIGINT UNSIGNED NOT NULL,
    id_nivel_academico BIGINT UNSIGNED NOT NULL,
    titulo_obtenido VARCHAR(150) NOT NULL,
    institucion_educativa VARCHAR(180) NOT NULL,
    anio_graduacion SMALLINT UNSIGNED NULL,
    estado_estudio ENUM('GRADUADO', 'CIERRE_PENSUM', 'EN_CURSO', 'TRUNCO') NOT NULL DEFAULT 'GRADUADO',
    numero_colegiado VARCHAR(40) NULL,
    es_grado_maximo BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_formacion_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_formacion_nivel
        FOREIGN KEY (id_nivel_academico) REFERENCES nivel_academico(id_nivel_academico)
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 5. Conclusión de la Tarea T3.13
El diagnóstico queda concluido. El esquema actual soporta la clasificación jerárquica general del colaborador (`id_nivel_academico`), pero requiere la adición de los campos `titulo_obtenido`, `institucion_educativa`, `año_graduacion` y `numero_colegiado` para cumplir los requisitos de expedientes de talento humano en los sprints posteriores.
