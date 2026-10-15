package org.kinalrh.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.kinalrh.model.Auditoria;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.Conexion;
import org.kinalrh.util.SessionManager;

/**
 * T3.20 - Comprobar evento, usuario relacionado y permiso del flujo
 * Responsable: Herberth.
 */
public class AuditoriaFlujoPermisosTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   PRUEBA T3.20: COMPROBAR EVENTO, USUARIO RELACIONADO Y PERMISO ");
        System.out.println("=================================================================\n");

        AuditoriaService auditoriaService = new AuditoriaService();
        EmpleadoService empleadoService = new EmpleadoService();

        // -------------------------------------------------------------
        // 1. Iniciar sesión con usuario específico para comprobar trazabilidad
        // -------------------------------------------------------------
        System.out.println("--- 1. Configurar sesión activa para trazabilidad ---");
        long idUsuarioSesion = 1L;
        Usuario usuarioSesion = new Usuario();
        usuarioSesion.setIdUsuario(idUsuarioSesion);
        usuarioSesion.setUsername("encargado_t320");
        usuarioSesion.setRol("ADMIN");
        SessionManager.getInstance().login(usuarioSesion);
        System.out.println("   [SESIÓN INICIADA] Usuario ID: " + idUsuarioSesion + " (" + usuarioSesion.getUsername() + ")");

        long suffix = System.currentTimeMillis() % 100000;
        Long idEmpleadoCreado = null;

        try {
            // -------------------------------------------------------------
            // 2. Ejecutar operación del flujo de negocio (Alta y Edición)
            // -------------------------------------------------------------
            System.out.println("\n--- 2. Ejecución de operación en el flujo (Crear Empleado) ---");
            Empleado emp = new Empleado();
            emp.setPrimerNombre("Trazabilidad");
            emp.setPrimerApellido("Flujo");
            emp.setDpi(String.format("4010%05d0101", suffix));
            emp.setNit(String.format("300%04d-8", suffix));
            emp.setCorreoPersonal("flujo." + suffix + "@kinal.edu.gt");
            emp.setIdEstadoEmpleado(1L);
            emp.setFechaNacimiento(LocalDate.of(1993, 8, 14));

            empleadoService.guardarEmpleado(emp, "ADMIN");
            idEmpleadoCreado = emp.getIdEmpleado();
            System.out.println("   [OPERACIÓN] Empleado creado con ID: " + idEmpleadoCreado);

            // -------------------------------------------------------------
            // 3. Comprobar evento y correspondencia con el usuario en auditoría
            // -------------------------------------------------------------
            System.out.println("\n--- 3. Verificación de registro en tabla auditoría y usuario relacionado ---");
            Auditoria eventoRecuperado = buscarUltimoEventoAuditoria("empleado", idEmpleadoCreado);

            boolean correspondenciaUsuarioOk = false;
            if (eventoRecuperado != null) {
                System.out.println("   [EVENTO REGISTRADO] ID Auditoría: " + eventoRecuperado.getIdAuditoria());
                System.out.println("   Acción: " + eventoRecuperado.getAccion() + " | Entidad: " + eventoRecuperado.getEntidad());
                System.out.println("   ID Usuario en Auditoría: " + eventoRecuperado.getIdUsuario() + " (Esperado: " + idUsuarioSesion + ")");
                System.out.println("   Fecha y Hora: " + eventoRecuperado.getFechaHora());

                if (eventoRecuperado.getIdUsuario() != null && eventoRecuperado.getIdUsuario().equals(idUsuarioSesion)) {
                    correspondenciaUsuarioOk = true;
                    System.out.println("   [ÉXITO] El evento de auditoría vincula de manera exacta y unívoca al usuario de la sesión.");
                } else {
                    System.err.println("   [FALLO] El id_usuario registrado no coincide con la sesión activa.");
                }
            } else {
                System.err.println("   [FALLO] No se encontró el evento de auditoría en base de datos.");
            }

            // -------------------------------------------------------------
            // 4. Comprobar rechazo ante falta de permisos en el flujo
            // -------------------------------------------------------------
            System.out.println("\n--- 4. Comprobación de permiso del flujo ante usuario no autorizado ---");
            boolean permisoFlujoDenegado = false;
            SessionManager.getInstance().login(new org.kinalrh.dao.impl.UsuarioDAOImpl().buscarPorUsername("visor_demo"));
            try {
                // Rol VISOR no tiene permiso EMPLEADO_EDITAR / EMPLEADO_CREAR
                emp.setPrimerNombre("NombreModificadoIlegalmente");
                empleadoService.guardarEmpleado(emp, "VISOR");
                System.err.println("   [FALLO] El servicio permitió modificar sin permisos de flujo.");
            } catch (SecurityException ex) {
                permisoFlujoDenegado = true;
                System.out.println("   [ÉXITO] Operación de flujo bloqueada por falta de permiso: " + ex.getMessage());
            }

            // -------------------------------------------------------------
            // 5. Comprobar protección de datos sensibles en auditoría
            // -------------------------------------------------------------
            System.out.println("\n--- 5. Comprobación de omisión de datos sensibles (T1.22/T3.20) ---");
            SessionManager.getInstance().login(usuarioSesion);
            auditoriaService.registrarEvento("CAMBIO_PASSWORD", "usuario", idUsuarioSesion, "password_hash", "viejo_hash_123", "nuevo_hash_456");
            Auditoria eventoProtegido = buscarUltimoEventoAuditoria("usuario", idUsuarioSesion);
            boolean datosProtegidosOk = false;
            if (eventoProtegido != null && "-".equals(eventoProtegido.getCampo())
                    && "-".equals(eventoProtegido.getValorAnterior()) && "-".equals(eventoProtegido.getValorNuevo())) {
                datosProtegidosOk = true;
                System.out.println("   [ÉXITO] Los valores sensibles se excluyeron de auditoría.");
            } else {
                System.err.println("   [FALLO] Se conservaron valores sensibles en auditoría.");
            }

            // -------------------------------------------------------------
            // RESUMEN FINAL
            // -------------------------------------------------------------
            System.out.println("\n=================================================================");
            if (correspondenciaUsuarioOk && permisoFlujoDenegado && datosProtegidosOk) {
                System.out.println("   RESULTADO T3.20: COMPROBACIÓN DE AUDITORÍA Y PERMISOS [OK]    ");
            } else {
                System.err.println("   RESULTADO T3.20: FALLO EN COMPROBACIÓN DE AUDITORÍA Y PERMISOS");
                throw new AssertionError("Falló auditoría o autorización del flujo.");
            }
            System.out.println("=================================================================");

        } finally {
            if (idEmpleadoCreado != null) {
                eliminarEmpleadoYAuditoriaPrueba(idEmpleadoCreado);
                System.out.println("\n[LIMPIEZA] Registros de prueba eliminados correctamente.");
            }
            SessionManager.getInstance().logout();
        }
    }

    private static Auditoria buscarUltimoEventoAuditoria(String entidad, long registroId) {
        String sql = "SELECT id_auditoria, id_usuario, accion, entidad, registro_id, campo, valor_anterior, valor_nuevo, fecha_hora " +
                     "FROM auditoria WHERE entidad = ? AND registro_id = ? ORDER BY id_auditoria DESC LIMIT 1";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entidad);
            stmt.setLong(2, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Auditoria a = new Auditoria();
                    a.setIdAuditoria(rs.getLong("id_auditoria"));
                    long idUser = rs.getLong("id_usuario");
                    if (!rs.wasNull()) {
                        a.setIdUsuario(idUser);
                    }
                    a.setAccion(rs.getString("accion"));
                    a.setEntidad(rs.getString("entidad"));
                    long regId = rs.getLong("registro_id");
                    if (!rs.wasNull()) {
                        a.setRegistroId(regId);
                    }
                    a.setCampo(rs.getString("campo"));
                    a.setValorAnterior(rs.getString("valor_anterior"));
                    a.setValorNuevo(rs.getString("valor_nuevo"));
                    java.sql.Timestamp ts = rs.getTimestamp("fecha_hora");
                    if (ts != null) {
                        a.setFechaHora(ts.toLocalDateTime());
                    }
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar auditoria: " + e.getMessage());
        }
        return null;
    }

    private static void eliminarEmpleadoYAuditoriaPrueba(long idEmpleado) {
        try (Connection conn = Conexion.getInstancia().conectar()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM auditoria WHERE registro_id = ? AND entidad = 'empleado'")) {
                stmt.setLong(1, idEmpleado);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM empleado WHERE id_empleado = ?")) {
                stmt.setLong(1, idEmpleado);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error en limpieza de empleado y auditoria: " + e.getMessage());
        }
    }
}
