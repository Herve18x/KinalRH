# T1.19 Casos de Prueba de Autorización y Cuentas Inactivas

## Descripción General
Esta suite documenta los escenarios donde se intenta consumir los servicios del sistema (específicamente EmpleadoService) evadiendo la interfaz gráfica para verificar la robustez del backend.

## Casos de Prueba

### Caso 1: Servicio invocado con rol inactivo / sin permisos reconocidos
- **Preparación:** Se invoca EmpleadoService.listarEmpleados(rolActual) simulando un usuario inactivo o un rol sin mapeo (VISOR_INACTIVO).
- **Llamada:** empleadoService.listarEmpleados("VISOR_INACTIVO")
- **Resultado Esperado:** El sistema debe lanzar SecurityException bloqueando el acceso a la capa de datos.
- **Resultado Real:** SecurityException lanzada con éxito ("Acceso denegado: Su cuenta no tiene el permiso EMPLEADO_VER.").
- **Discrepancia:** Ninguna.

### Caso 2: Intento sin el permiso EMPLEADO_VER
- **Preparación:** Se simula un rol existente (ej. EXTERNO) que en la base de datos de permisos no tiene asociado el código EMPLEADO_VER.
- **Llamada:** empleadoService.listarEmpleados("EXTERNO")
- **Resultado Esperado:** Lanzamiento de SecurityException.
- **Resultado Real:** SecurityException lanzada exitosamente protegiendo la confidencialidad de los datos.
- **Discrepancia:** Ninguna. (Se corrigió la falla de seguridad anterior donde el sistema solo imprimía un warning en consola y entregaba los datos de todos modos).

### Caso 3: Invocación de servicio después de Logout (Sesión Nula)
- **Preparación:** El controlador de sesión se limpia (Logout). El parámetro de rol que llega al servicio es 
ull o vacío.
- **Llamada:** empleadoService.listarEmpleados(null)
- **Resultado Esperado:** Denegado inmediatamente por falta de contexto de sesión válida.
- **Resultado Real:** SecurityException lanzada ("Acceso denegado: Sesión no válida (después de logout)").
- **Discrepancia:** Ninguna.

### Caso 4: Cuenta con permiso válido
- **Preparación:** Se inyecta un rol con privilegios amplios reconocidos en DB (ej. ADMIN).
- **Llamada:** empleadoService.listarEmpleados("ADMIN")
- **Resultado Esperado:** Retorna la lista List<Empleado>.
- **Resultado Real:** Lista recuperada exitosamente.
- **Discrepancia:** Ninguna.

## Conclusión
La validación backend ahora es estricta. Ninguna de las 3 rutas inseguras entrega el resumen protegido. El criterio de aceptación del ticket **T1.19** se ha cumplido satisfactoriamente.