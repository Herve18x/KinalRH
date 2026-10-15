package org.kinalrh.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.kinalrh.model.Empleado;
import org.kinalrh.util.Conexion;

/**
 * T2.19 - Contrastar lista con BD y probar autorización
 * Responsable: Herberth.
 */
public class EmpleadoListaAutorizacionTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   PRUEBA T2.19: CONTRASTAR LISTA CON BD Y PROBAR AUTORIZACIÓN   ");
        System.out.println("=================================================================\n");

        EmpleadoService empleadoService = new EmpleadoService();

        // -------------------------------------------------------------
        // CASO 1: Contrastar lista de servicio vs conteo real en BD
        // -------------------------------------------------------------
        System.out.println("--- CASO 1: Contraste de lista con registros reales en BD ---");
        long totalEnBD = contarEmpleadosEnBD();
        List<Empleado> listaServicio = empleadoService.listarEmpleados("ADMIN");

        System.out.println("   Total de empleados en BD (SQL directo): " + totalEnBD);
        System.out.println("   Total de empleados recuperados por servicio: " + listaServicio.size());

        boolean contrasteOk = (totalEnBD == listaServicio.size());
        if (contrasteOk) {
            System.out.println("   [ÉXITO] El conteo coincide exactamente sin pérdida de registros.");
            if (!listaServicio.isEmpty()) {
                Empleado primero = listaServicio.get(0);
                System.out.println("   [MUESTRA REGISTRO] ID: " + primero.getIdEmpleado() + 
                                   " | Nombre: " + primero.getPrimerNombre() + " " + primero.getPrimerApellido() +
                                   " | DPI: " + primero.getDpi() +
                                   " | Estado: " + (primero.getEstado() != null ? primero.getEstado() : primero.getIdEstadoEmpleado()));
            }
        } else {
            System.err.println("   [FALLO] Discrepancia entre BD y la lista del servicio.");
        }

        // -------------------------------------------------------------
        // CASO 2: Probar autorización con rol permitido (ENCARGADO)
        // -------------------------------------------------------------
        System.out.println("\n--- CASO 2: Autorización con rol con permiso (ENCARGADO) ---");
        boolean encargadoOk = false;
        try {
            List<Empleado> listaEncargado = empleadoService.listarEmpleados("ENCARGADO");
            encargadoOk = (listaEncargado.size() == totalEnBD);
            System.out.println("   [ÉXITO] Rol ENCARGADO autorizado correctamente. Registros: " + listaEncargado.size());
        } catch (SecurityException ex) {
            System.err.println("   [FALLO] Se denegó acceso a ENCARGADO: " + ex.getMessage());
        }

        // -------------------------------------------------------------
        // CASO 3: Probar autorización con rol no autorizado (SIN_ACCESO)
        // -------------------------------------------------------------
        System.out.println("\n--- CASO 3: Rechazo de rol sin permiso EMPLEADO_VER ---");
        boolean rolSinPermisoOk = false;
        try {
            empleadoService.listarEmpleados("USUARIO_SIN_PERMISO");
            System.err.println("   [FALLO] Un rol sin permiso pudo consultar la lista de colaboradores.");
        } catch (SecurityException ex) {
            rolSinPermisoOk = true;
            System.out.println("   [ÉXITO] Acceso denegado correctamente por AutorizacionService: " + ex.getMessage());
        }

        // -------------------------------------------------------------
        // CASO 4: Invocación sin sesión (sesión nula o tras logout)
        // -------------------------------------------------------------
        System.out.println("\n--- CASO 4: Rechazo ante invocación sin sesión (null / logout) ---");
        boolean sesionNulaOk = false;
        try {
            empleadoService.listarEmpleados(null);
            System.err.println("   [FALLO] Se permitió consultar empleados con rol null.");
        } catch (SecurityException ex) {
            sesionNulaOk = true;
            System.out.println("   [ÉXITO] Solicitud anónima/sin sesión rechazada: " + ex.getMessage());
        }

        // -------------------------------------------------------------
        // RESUMEN FINAL
        // -------------------------------------------------------------
        System.out.println("\n=================================================================");
        if (contrasteOk && encargadoOk && rolSinPermisoOk && sesionNulaOk) {
            System.out.println("   RESULTADO T2.19: TODAS LAS PRUEBAS PASARON EXITOSAMENTE [OK]  ");
        } else {
            System.err.println("   RESULTADO T2.19: FALLO EN AL MENOS UN CASO DE PRUEBA          ");
        }
        System.out.println("=================================================================");
    }

    private static long contarEmpleadosEnBD() {
        String sql = "SELECT COUNT(*) FROM empleado";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar empleados: " + e.getMessage());
        }
        return 0;
    }
}
