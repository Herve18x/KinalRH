package org.kinalrh.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.dao.NivelAcademicoDAO;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.dao.impl.NivelAcademicoDAOImpl;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;

/**
 * Servicio para gestión y lógica de negocio de colaboradores.
 * T3.06 Regla que impide ser su propio jefe y validaciones jerárquicas.
 * T3.10 Asignación de nivel académico actual.
 * T3.12 Verificar que solo se guarda un nivel académico válido y activo.
 * T3.18 Registrar alta y edición de empleado con autor y fecha.
 */
public class EmpleadoService {

    private final AutorizacionService autorizacionService;
    private final EmpleadoDAO empleadoDAO;
    private final NivelAcademicoDAO nivelAcademicoDAO;
    private final AuditoriaService auditoriaService;

    public EmpleadoService() {
        this(new EmpleadoDAOImpl());
    }

    public EmpleadoService(EmpleadoDAO empleadoDAO) {
        if (empleadoDAO == null) {
            throw new IllegalArgumentException("El DAO de empleados es obligatorio.");
        }
        this.autorizacionService = new AutorizacionService();
        this.empleadoDAO = empleadoDAO;
        this.nivelAcademicoDAO = new NivelAcademicoDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }

    public void retirarEmpleado(long idEmpleado) {
        autorizacionService.exigirPermiso("EMPLEADO_CAMBIAR_ESTADO");
        try {
            if (!empleadoDAO.retirar(idEmpleado)) {
                throw new IllegalArgumentException("El colaborador no existe.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cambiar el estado laboral del colaborador.", e);
        }
    }

    public List<Empleado> listarEmpleados(String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida (después de logout).");
        }
        autorizacionService.exigirPermiso("EMPLEADO_VER");
        try {
            return empleadoDAO.listarTodos();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar empleados.");
        }
    }
    
    public Empleado buscarPorId(long idEmpleado, String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida.");
        }
        autorizacionService.exigirPermiso("EMPLEADO_VER");
        try {
            Optional<Empleado> opt = empleadoDAO.buscarPorId(idEmpleado);
            return opt.orElse(null);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el empleado.");
        }
    }

    private void validarPermisoEdicion(String rolActual) {
        autorizacionService.exigirPermiso("EMPLEADO_EDITAR");
    }

    public List<Empleado> buscar(String nombre, String dpi, Long idArea, Long idEstado, int limite, int offset) {
        autorizacionService.exigirPermiso("EMPLEADO_VER");
        try { return empleadoDAO.buscar(nombre,dpi,idArea,idEstado,limite,offset); }
        catch (SQLException ex) { throw new IllegalStateException("No se pudo buscar empleados."); }
    }

    public void validarJefeInmediato(Long idEmpleado, Long idJefeInmediato) {
        if (idJefeInmediato == null) {
            return;
        }
        // T3.06 Regla que impide ser su propio jefe (US-18)
        if (idEmpleado != null && idEmpleado > 0 && idEmpleado.equals(idJefeInmediato)) {
            throw new IllegalArgumentException("Error: Un colaborador no puede ser asignado como su propio jefe inmediato.");
        }
        java.util.Set<Long> visitados = new java.util.HashSet<>();
        Long actual = idJefeInmediato;
        try {
            while (actual != null) {
                if (actual.equals(idEmpleado) || !visitados.add(actual)) {
                    throw new IllegalArgumentException("Error: Ciclo jerárquico detectado en la cadena de jefes.");
                }
                Empleado jefe = empleadoDAO.buscarPorId(actual).orElseThrow(
                        () -> new IllegalArgumentException("Error: El jefe inmediato seleccionado no existe en la base de datos."));
                actual = jefe.getIdJefeInmediato();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo validar la jerarquía. No se guardaron cambios.");
        }
    }

    private Long resolverIdAutor() {
        Usuario u = SessionManager.getInstance().getCurrentUser();
        if (u == null) {
            u = SesionService.getInstance().getUsuarioAutenticado();
        }
        return u != null ? u.getIdUsuario() : null;
    }
    
    public void guardarEmpleado(Empleado emp, String rolActual) {
        guardarEmpleado(emp, rolActual, resolverIdAutor());
    }

    public void guardarEmpleado(Empleado emp, String rolActual, Long idAutor) {
        autorizacionService.exigirPermiso(emp == null || emp.getIdEmpleado() == null || emp.getIdEmpleado() <= 0
                ? "EMPLEADO_CREAR" : "EMPLEADO_EDITAR");
        idAutor = resolverIdAutor();
        if (emp == null) {
            throw new IllegalArgumentException("Error: Los datos del empleado no pueden ser nulos.");
        }
        
        Long idExcluido = (emp.getIdEmpleado() != null && emp.getIdEmpleado() > 0) ? emp.getIdEmpleado() : null;
        
        // T3.06 Regla que impide ser su propio jefe y validación de jefe inmediato
        validarJefeInmediato(idExcluido, emp.getIdJefeInmediato());

        // T2.09 Validar duplicados (US-44)
        if (emp.getDpi() != null && empleadoDAO.existeDpi(emp.getDpi(), idExcluido)) {
            throw new IllegalArgumentException("Ya existe un empleado registrado con ese DPI.");
        }
        if (emp.getNit() != null && !emp.getNit().trim().isEmpty() && empleadoDAO.existeNit(emp.getNit(), idExcluido)) {
            throw new IllegalArgumentException("Ya existe un empleado registrado con ese NIT.");
        }
        if (emp.getCorreoPersonal() != null && !emp.getCorreoPersonal().trim().isEmpty() && empleadoDAO.existeCorreoPersonal(emp.getCorreoPersonal(), idExcluido)) {
            throw new IllegalArgumentException("Ya existe un empleado con ese correo personal.");
        }
        
        // T2.09 / T3.06 / T3.10 Validar referencias existentes (área, puesto, estado)
        if (emp.getIdAreaPrincipal() != null && !empleadoDAO.existeReferencia("area", "id_area", emp.getIdAreaPrincipal())) {
            throw new IllegalArgumentException("Error: El área seleccionada no existe en la base de datos.");
        }
        if (emp.getIdPuestoActual() != null && !empleadoDAO.existeReferencia("puesto", "id_puesto", emp.getIdPuestoActual())) {
            throw new IllegalArgumentException("Error: El puesto seleccionado no existe en la base de datos.");
        }
        if (emp.getIdEstadoEmpleado() != null && !empleadoDAO.existeReferencia("estado_empleado", "id_estado_empleado", emp.getIdEstadoEmpleado())) {
            throw new IllegalArgumentException("Error: El estado laboral seleccionado no existe.");
        }

        // T3.12 Verificar que solo se guarda un nivel académico válido y activo
        if (emp.getIdNivelAcademico() != null) {
            if (!nivelAcademicoDAO.esNivelValidoYActivo(emp.getIdNivelAcademico())) {
                throw new IllegalArgumentException("Error: El nivel académico seleccionado no es válido o no está activo.");
            }
        }
        
        try {
            String detalle = (emp.getNombreCompleto() != null && !emp.getNombreCompleto().trim().isEmpty())
                    ? emp.getNombreCompleto()
                    : emp.getNombre();

            if (idExcluido == null) {
                // T3.18 Registrar alta de empleado con autor y fecha
                long id = empleadoDAO.insertar(emp);
                emp.setIdEmpleado(id);
                auditoriaService.auditarAltaEmpleado(idAutor, id, detalle != null ? detalle : ("Empleado #" + id));
                auditoriaService.registrarEvento(idAutor, "CREAR_EMPLEADO", "empleado", id, null, null, detalle);
            } else {
                // T3.18 Registrar edición de empleado con autor y fecha
                if (!empleadoDAO.actualizar(emp)) throw new IllegalArgumentException("El empleado a actualizar no existe.");
                auditoriaService.auditarEdicionEmpleado(idAutor, emp.getIdEmpleado(), detalle != null ? detalle : ("Empleado #" + emp.getIdEmpleado()));
                auditoriaService.registrarEvento(idAutor, "ACTUALIZAR_EMPLEADO", "empleado", emp.getIdEmpleado(), null, null, detalle);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el empleado. Revise sus datos y referencias.");
        }
    }

    public void registrarAltaEmpleado(Empleado emp, String rolActual, Long idAutor) {
        if (emp != null) {
            emp.setIdEmpleado(null);
        }
        guardarEmpleado(emp, rolActual, idAutor);
    }

    public void registrarEdicionEmpleado(Empleado emp, String rolActual, Long idAutor) {
        if (emp == null || emp.getIdEmpleado() == null || emp.getIdEmpleado() <= 0) {
            throw new IllegalArgumentException("Error: Para editar un empleado debe proporcionar un ID válido.");
        }
        guardarEmpleado(emp, rolActual, idAutor);
    }

    // T3.06 Asignaciones organizacionales en servicio
    public void asignarJefeInmediato(long idEmpleado, Long idJefeInmediato, String rolActual) {
        validarPermisoEdicion(rolActual);
        validarJefeInmediato(idEmpleado, idJefeInmediato);
        try {
            Empleado emp = empleadoDAO.buscarPorId(idEmpleado)
                    .orElseThrow(() -> new IllegalArgumentException("Error: El colaborador no existe."));
            Long anterior = emp.getIdJefeInmediato();
            emp.setIdJefeInmediato(idJefeInmediato);
            empleadoDAO.actualizar(emp);
            Long idAutor = resolverIdAutor();
            auditoriaService.registrarEvento(idAutor, "ASIGNAR_JEFE", "empleado", idEmpleado, "id_jefe_inmediato",
                    anterior != null ? String.valueOf(anterior) : null,
                    idJefeInmediato != null ? String.valueOf(idJefeInmediato) : null);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo asignar el jefe inmediato.");
        }
    }

    public void asignarAreaPrincipal(long idEmpleado, Long idAreaPrincipal, String rolActual) {
        validarPermisoEdicion(rolActual);
        if (idAreaPrincipal != null && !empleadoDAO.existeReferencia("area", "id_area", idAreaPrincipal)) {
            throw new IllegalArgumentException("Error: El área seleccionada no existe en la base de datos.");
        }
        try {
            Empleado emp = empleadoDAO.buscarPorId(idEmpleado)
                    .orElseThrow(() -> new IllegalArgumentException("Error: El colaborador no existe."));
            Long anterior = emp.getIdAreaPrincipal();
            emp.setIdAreaPrincipal(idAreaPrincipal);
            empleadoDAO.actualizar(emp);
            Long idAutor = resolverIdAutor();
            auditoriaService.registrarEvento(idAutor, "ASIGNAR_AREA", "empleado", idEmpleado, "id_area_principal",
                    anterior != null ? String.valueOf(anterior) : null,
                    idAreaPrincipal != null ? String.valueOf(idAreaPrincipal) : null);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo asignar el área principal.");
        }
    }

    public void asignarPuestoActual(long idEmpleado, Long idPuestoActual, String rolActual) {
        validarPermisoEdicion(rolActual);
        if (idPuestoActual != null && !empleadoDAO.existeReferencia("puesto", "id_puesto", idPuestoActual)) {
            throw new IllegalArgumentException("Error: El puesto seleccionado no existe en la base de datos.");
        }
        try {
            Empleado emp = empleadoDAO.buscarPorId(idEmpleado)
                    .orElseThrow(() -> new IllegalArgumentException("Error: El colaborador no existe."));
            Long anterior = emp.getIdPuestoActual();
            emp.setIdPuestoActual(idPuestoActual);
            empleadoDAO.actualizar(emp);
            Long idAutor = resolverIdAutor();
            auditoriaService.registrarEvento(idAutor, "ASIGNAR_PUESTO", "empleado", idEmpleado, "id_puesto_actual",
                    anterior != null ? String.valueOf(anterior) : null,
                    idPuestoActual != null ? String.valueOf(idPuestoActual) : null);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo asignar el puesto actual.");
        }
    }

    // T3.10 Asignación de nivel académico actual en servicio
    // T3.12 Verificar que solo se guarda un nivel académico válido y activo
    public void asignarNivelAcademico(long idEmpleado, Long idNivelAcademico, String rolActual) {
        validarPermisoEdicion(rolActual);
        if (idNivelAcademico != null && !nivelAcademicoDAO.esNivelValidoYActivo(idNivelAcademico)) {
            throw new IllegalArgumentException("Error: El nivel académico seleccionado no es válido o no está activo.");
        }
        try {
            Empleado emp = empleadoDAO.buscarPorId(idEmpleado)
                    .orElseThrow(() -> new IllegalArgumentException("Error: El colaborador no existe."));
            Long anterior = emp.getIdNivelAcademico();
            empleadoDAO.actualizarNivelAcademico(idEmpleado, idNivelAcademico);
            Long idAutor = resolverIdAutor();
            auditoriaService.registrarEvento(idAutor, "ASIGNAR_NIVEL_ACADEMICO", "empleado", idEmpleado, "id_nivel_academico",
                    anterior != null ? String.valueOf(anterior) : null,
                    idNivelAcademico != null ? String.valueOf(idNivelAcademico) : null);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo asignar el nivel académico.");
        }
    }
}
