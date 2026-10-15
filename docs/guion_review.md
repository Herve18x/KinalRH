# Sprint Review — T4.20 (13 minutos)

Preparación: BD descartable, migración 002, fixtures, cuentas ficticias con y sin
permisos, aplicación compilada. No mostrar configuración, claves, hashes ni datos
reales. Mantener a mano los resultados reproducibles de prueba_integrada.md.

| Minuto | Demostración | Evidencia / responsable |
| --- | --- | --- |
| 0–2 | Login correcto, incorrecto y cuenta inactiva | AutenticacionServiceTest / Alexis |
| 2–3 | Operación permitida y denegada con sesión real | AutorizacionServiceTest y matriz / Alexis |
| 3–5 | Alta y edición parcial; conservar campos omitidos; rechazo de duplicado | Ficha existente y pruebas DAO / Herberth y Zabala |
| 5–7 | Filtros combinados y dos logros distintos; vínculo repetido rechazado | PendientesSprint3Test / Zabala |
| 7–10 | Seleccionar XLSX, validar, ver conteos y errores, cancelar; luego confirmar lote válido | Importacion.fxml, fixtures y servicio / Keneth y Herberth |
| 10–12 | Reporte de activos con filtros; abrir XLSX y cotejar folio | ReporteServiceTest, ExportacionTrazabilidadTest / Keneth y Alexis |
| 12–13 | Logout; repetir llamada protegida; recoger observaciones | PruebaIntegradaTest y feedback_po.md / Herberth |

Si Daniel ya integró T4.04, usar su previsualización en el bloque 7–10; mientras
esté pendiente, mostrar el contrato de filas y los resultados de servicio sin
declarar terminada esa pantalla. T4.10 permanece pendiente: demostrar el reporte
y su exportación mediante el ejecutable hasta integrar el controlador existente.
No desarrollar la interfaz durante la revisión.

Reservar hasta dos minutos adicionales para preguntas (máximo 15 minutos).
Registrar únicamente observaciones efectivamente recibidas, sin decisiones
atribuidas al PO antes de la reunión.
