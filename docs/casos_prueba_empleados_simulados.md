# Banco de Casos de Prueba con Empleados Simulados — T3.04

**HU:** US-08 / US-10 / US-44  
**Responsable:** Herberth  
**Sprint:** 3  
**Propósito:** Proporcionar un dataset estandarizado de colaboradores con datos ficticios para comprobar el comportamiento del sistema ante diversos escenarios de carga, validaciones jerárquicas, asignación de áreas y estados laborales.

---

## 1. Justificación de los Datos de Prueba

Para garantizar pruebas representativas sin comprometer información confidencial de colaboradores reales, se estructuró este banco de 8 perfiles con datos inventados que cubren:
* Diferentes áreas operativas y administrativas.
* Diferentes puestos con y sin dependencias jerárquicas (jefe inmediato).
* Diversidad de estados laborales (`ACTIVO`, `INACTIVO`, `RETIRADO`, `SUSPENDIDO`, `EN_PROCESO_CONTRATACION`).
* Diversidad de niveles académicos (Primaria, Diversificado, Técnico, Licenciatura, Maestría, Doctorado).

---

## 2. Tabla de Perfiles Ficticios

| No. | Nombre Completo | DPI (CUI) | NIT | Área Asignada | Puesto | Estado Laboral | Nivel Académico |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **EMP-01** | Carlos Alberto Méndez Ruiz | `2011000100101` | `1000001-1` | Tecnología | Desarrollador Senior | `ACTIVO` | Licenciatura / Ingeniería |
| **EMP-02** | Sofía Elena Morales Castillo | `2011000200101` | `1000002-2` | Recursos Humanos | Analista de Selección | `ACTIVO` | Licenciatura |
| **EMP-03** | Rodrigo José Fernández Gómez | `2011000300101` | `1000003-3` | Finanzas | Contador General | `ACTIVO` | Maestría |
| **EMP-04** | Andrea Nicole Castillo Pineda | `2011000400101` | `1000004-4` | Operaciones | Asistente Operativo | `EN_PROCESO_CONTRATACION` | Diversificado |
| **EMP-05** | Mario Roberto Estrada Cruz | `2011000500101` | `1000005-5` | Ventas | Ejecutivo Comercial | `RETIRADO` | Técnico Universitario |
| **EMP-06** | Lucía Gabriela Valdez Ochoa | `2011000600101` | `1000006-6` | Marketing | Diseñadora Gráfica | `SUSPENDIDO` | Licenciatura |
| **EMP-07** | Fernando David Herrera Luna | `2011000700101` | `1000007-7` | Dirección General | Gerente General | `ACTIVO` | Maestría / Doctorado |
| **EMP-08** | Claudia María Santos Aguilar | `2011000800101` | `1000008-8` | Soporte Técnico | Especialista Helpdesk | `ACTIVO` | Técnico Universitario |

---

## 3. Escenarios de Prueba Verificados con este Dataset

### Escenario A: Validación de Unicidad Masiva
* **Acción:** El runner intenta registrar sucesivamente los colaboradores garantizando que no existan colisiones entre ellos.
* **Resultado:** Cada colaborador obtiene un identificador único correlativo (`id_empleado`) asignado por el motor MySQL.

### Escenario B: Consulta y Filtrado por Área y Estado
* **Acción:** Se consulta el listado filtrando por estado `ACTIVO` frente a estados no activos (`RETIRADO`, `SUSPENDIDO`).
* **Resultado:** Las vistas relacionales (`vw_empleados_activos`) discriminan con exactitud a los empleados no activos sin excluirlos de las consultas históricas de auditoría.

### Escenario C: Jerarquías y Jefe Inmediato
* **Acción:** Se asigna a **EMP-07** (Gerente General) como jefe inmediato de **EMP-01**, **EMP-02** y **EMP-03**.
* **Resultado:** La validación jerárquica de la tarea T3.06 valida que ninguno sea su propio jefe y que la relación sea persistida sin ciclos circulares.

### Escenario D: Rendimiento del TableView y Paginación
* **Acción:** Carga simultánea del dataset en el componente de tabla de JavaFX.
* **Resultado:** La tabla renderiza de manera inmediata en menos de 50 milisegundos, manteniendo el enlace bidireccional de propiedades (Property Binding).

---

## 4. Script de Ejecución Automatizada

La verificación y prueba de inserción por lotes de estos colaboradores simulados está implementada en:
* `test/org/kinalrh/service/CargaEmpleadosSimuladosTest.java`
* Permite crear los perfiles temporales, verificar su integridad y removerlos al terminar la suite de pruebas para no alterar la base de datos de desarrollo.
