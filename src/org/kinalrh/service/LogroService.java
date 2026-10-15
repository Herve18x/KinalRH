package org.kinalrh.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.dao.LogroDAO;
import org.kinalrh.dao.TipoLogroDAO;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.dao.impl.LogroDAOImpl;
import org.kinalrh.dao.impl.TipoLogroDAOImpl;
import org.kinalrh.model.Logro;
import org.kinalrh.model.LogroEmpleadoDTO;
import org.kinalrh.model.TipoLogro;

/**
 * T3.16 Servicio que guarda y muestra logros de un colaborador.
 * T3.09 Lectura/gestión básica de tipo de logro.
 */
public class LogroService {

    private final LogroDAO logroDAO;
    private final EmpleadoDAO empleadoDAO;
    private final TipoLogroDAO tipoLogroDAO;
    private final AutorizacionService autorizacionService;
    private final AuditoriaService auditoriaService;

    public LogroService() {
        this.logroDAO = new LogroDAOImpl();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.tipoLogroDAO = new TipoLogroDAOImpl();
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

    public List<TipoLogro> listarTiposLogro() {
        return tipoLogroDAO.listarActivos();
    }

    public List<TipoLogro> listarTodosLosTiposLogro(String rolActual) {
        validarPermisoConsulta(rolActual);
        try {
            return tipoLogroDAO.listarTodos();
        } catch (SQLException e) {
            System.err.println("Error al listar tipos de logro: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public long guardarTipoLogro(TipoLogro tipo, String rolActual) {
        validarPermisoGestion(rolActual);
        if (tipo == null || tipo.getCodigo() == null || tipo.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El código del tipo de logro es obligatorio.");
        }
        if (tipo.getNombre() == null || tipo.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Error: El nombre del tipo de logro es obligatorio.");
        }
        Long idExcluido = (tipo.getIdTipoLogro() != null && tipo.getIdTipoLogro() > 0) ? tipo.getIdTipoLogro() : null;
        if (tipoLogroDAO.existeCodigo(tipo.getCodigo(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un tipo de logro con el código " + tipo.getCodigo());
        }
        if (tipoLogroDAO.existeNombre(tipo.getNombre(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un tipo de logro con el nombre " + tipo.getNombre());
        }
        try {
            if (idExcluido == null) {
                long id = tipoLogroDAO.insertar(tipo);
                auditoriaService.registrarEvento("CREAR_TIPO_LOGRO", "tipo_logro", id, null, null, tipo.getNombre());
                return id;
            } else {
                tipoLogroDAO.actualizar(tipo);
                auditoriaService.registrarEvento("ACTUALIZAR_TIPO_LOGRO", "tipo_logro", tipo.getIdTipoLogro(), null, null, tipo.getNombre());
                return tipo.getIdTipoLogro();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar tipo de logro: " + e.getMessage(), e);
        }
    }

    public void cambiarEstadoTipoLogro(Long idTipoLogro, boolean activo, String rolActual) {
        validarPermisoGestion(rolActual);
        if (idTipoLogro == null || idTipoLogro <= 0) {
            throw new IllegalArgumentException("Error: ID de tipo de logro inválido.");
        }
        try {
            tipoLogroDAO.cambiarEstado(idTipoLogro, activo);
            auditoriaService.registrarEvento("ESTADO_TIPO_LOGRO", "tipo_logro", idTipoLogro, "activo", null, String.valueOf(activo));
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar estado de tipo de logro: " + e.getMessage(), e);
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
        } else {
            TipoLogro tipo = tipoLogroDAO.buscarPorId(logro.getIdTipoLogro());
            if (tipo == null || !tipo.isActivo()) {
                throw new IllegalArgumentException("Error: El tipo de logro seleccionado no es válido o no está activo.");
            }
        }

        if (logro.getOrigen() == null || logro.getOrigen().trim().isEmpty()) {
            logro.setOrigen("OTRO");
        }
        if (logro.getFinanciadoPorKinal() == null) {
            logro.setFinanciadoPorKinal(false);
        }

        try {
            long idLogro = logroDAO.guardarConEmpleado(logro, idEmpleado, destacado, observaciones);
            if (idLogro > 0) {
                auditoriaService.registrarEvento("GUARDAR_LOGRO_COLABORADOR", "empleado_logro", idLogro,
                        "id_empleado", null, String.valueOf(idEmpleado));
            }
            return idLogro;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el logro del colaborador.");
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
