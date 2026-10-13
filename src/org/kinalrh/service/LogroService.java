package org.kinalrh.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.dao.LogroDAO;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.dao.impl.LogroDAOImpl;
import org.kinalrh.model.Logro;
import org.kinalrh.model.LogroEmpleadoDTO;

/**
 * T3.16 Servicio que guarda y muestra logros de un colaborador.
 */
public class LogroService {

    private final LogroDAO logroDAO;
    private final EmpleadoDAO empleadoDAO;
    private final AutorizacionService autorizacionService;
    private final AuditoriaService auditoriaService;

    public LogroService() {
        this.logroDAO = new LogroDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.autorizacionService = new AutorizacionService();
        this.auditoriaService = new AuditoriaService();
    }

    private void validarPermisoConsulta(String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida.");
        }
        String r = rolActual.trim().toUpperCase();
        if (!autorizacionService.tienePermiso(rolActual, "EMPLEADO_VER")
                && !autorizacionService.tienePermiso(rolActual, "LOGRO_GESTIONAR")
                && !r.equals("VISOR") && !r.equals("ADMIN") && !r.equals("ENCARGADO") && !r.equals("RRHH")) {
            throw new SecurityException("Acceso denegado: No tiene permisos para consultar logros.");
        }
    }

    private void validarPermisoGestion(String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida.");
        }
        String r = rolActual.trim().toUpperCase();
        if (!autorizacionService.tienePermiso(rolActual, "LOGRO_GESTIONAR")
                && !autorizacionService.tienePermiso(rolActual, "EMPLEADO_EDITAR")
                && !r.equals("ADMIN") && !r.equals("ENCARGADO") && !r.equals("RRHH")) {
            throw new SecurityException("Acceso denegado: Su cuenta no tiene permiso para registrar o modificar logros.");
        }
    }

    public long guardarLogroDeColaborador(Long idEmpleado, Logro logro, String rolActual) {
        return guardarLogroDeColaborador(idEmpleado, logro, false, null, rolActual);
    }

    public long guardarLogroDeColaborador(Long idEmpleado, Logro logro, boolean destacado, String observaciones, String rolActual) {
        validarPermisoGestion(rolActual);

        if (idEmpleado == null || idEmpleado <= 0) {
            throw new IllegalArgumentException("Error: Debe seleccionar un colaborador válido.");
        }
        if (!empleadoDAO.existeReferencia("empleado", "id_empleado", idEmpleado)) {
            throw new IllegalArgumentException("Error: El colaborador seleccionado no existe en la base de datos.");
        }
        if (logro == null || logro.getNombre() == null || logro.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El nombre del logro es obligatorio.");
        }

        if (logro.getIdTipoLogro() == null || logro.getIdTipoLogro() <= 0) {
            logro.setIdTipoLogro(1L);
        }
        if (logro.getOrigen() == null || logro.getOrigen().trim().isEmpty()) {
            logro.setOrigen("OTRO");
        }
        if (logro.getFinanciadoPorKinal() == null) {
            logro.setFinanciadoPorKinal(false);
        }

        try {
            long idLogro;
            if (logro.getIdLogro() != null && logro.getIdLogro() > 0) {
                logroDAO.actualizar(logro);
                idLogro = logro.getIdLogro();
            } else {
                idLogro = logroDAO.insertar(logro);
                logro.setIdLogro(idLogro);
            }
            if (idLogro > 0) {
                logroDAO.asignarLogroAEmpleado(idLogro, idEmpleado, destacado, observaciones);
                auditoriaService.registrarEvento("GUARDAR_LOGRO_COLABORADOR", "empleado_logro", idLogro,
                        "id_empleado", null, String.valueOf(idEmpleado));
            }
            return idLogro;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el logro del colaborador: " + e.getMessage(), e);
        }
    }

    public List<Logro> listarLogrosPorColaborador(long idEmpleado, String rolActual) {
        validarPermisoConsulta(rolActual);
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("Error: El ID del colaborador debe ser positivo.");
        }
        try {
            return logroDAO.listarPorEmpleado(idEmpleado);
        } catch (SQLException e) {
            System.err.println("Error al consultar logros del colaborador: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<LogroEmpleadoDTO> listarLogrosPorColaboradorDTO(long idEmpleado, String rolActual) {
        validarPermisoConsulta(rolActual);
        if (idEmpleado <= 0) {
            throw new IllegalArgumentException("Error: El ID del colaborador debe ser positivo.");
        }
        try {
            return logroDAO.listarLogrosPorEmpleadoDTO(idEmpleado);
        } catch (SQLException e) {
            System.err.println("Error al consultar DTO de logros del colaborador: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<LogroEmpleadoDTO> listarLogrosConEmpleados(String rolActual) {
        validarPermisoConsulta(rolActual);
        try {
            return logroDAO.listarLogrosConEmpleados();
        } catch (SQLException e) {
            System.err.println("Error al listar logros con empleados: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
