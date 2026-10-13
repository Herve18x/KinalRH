package org.kinalrh.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.model.Empleado;

public class EmpleadoService {

    private AutorizacionService autorizacionService;
    private EmpleadoDAO empleadoDAO;
    private AuditoriaService auditoriaService;

    public EmpleadoService() {
        this.autorizacionService = new AutorizacionService();
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }

    public List<Empleado> listarEmpleados(String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida (después de logout).");
        }
        if (!autorizacionService.tienePermiso(rolActual, "EMPLEADO_VER")) {
            throw new SecurityException("Acceso denegado: Su cuenta no tiene el permiso EMPLEADO_VER.");
        }
        try {
            return empleadoDAO.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Empleado buscarPorId(long idEmpleado, String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida.");
        }
        if (!autorizacionService.tienePermiso(rolActual, "EMPLEADO_VER")) {
            throw new SecurityException("Acceso denegado: Su cuenta no tiene el permiso EMPLEADO_VER.");
        }
        try {
            Optional<Empleado> opt = empleadoDAO.buscarPorId(idEmpleado);
            return opt.orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void validarPermisoEdicion(String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida.");
        }
        if (!autorizacionService.tienePermiso(rolActual, "EMPLEADO_EDITAR")
                && !autorizacionService.tienePermiso(rolActual, "EMPLEADO_CREAR")
                && !rolActual.equalsIgnoreCase("ADMIN")
                && !rolActual.equalsIgnoreCase("ENCARGADO")
                && !rolActual.equalsIgnoreCase("RRHH")) {
            throw new SecurityException("Acceso denegado: Su cuenta no tiene permiso para modificar empleados.");
        }
    }

    public void validarJefeInmediato(Long idEmpleado, Long idJefeInmediato) {
        if (idJefeInmediato == null) {
            return;
        }
        // T3.06 Regla que impide ser su propio jefe (US-18)
        if (idEmpleado != null && idEmpleado > 0 && idEmpleado.equals(idJefeInmediato)) {
            throw new IllegalArgumentException("Error: Un colaborador no puede ser asignado como su propio jefe inmediato.");
        }
        if (!empleadoDAO.existeReferencia("empleado", "id_empleado", idJefeInmediato)) {
            throw new IllegalArgumentException("Error: El jefe inmediato seleccionado no existe en la base de datos.");
        }
        // Evitar ciclo jerárquico directo (A jefe de B y B jefe de A)
        if (idEmpleado != null && idEmpleado > 0) {
            try {
                Optional<Empleado> optJefe = empleadoDAO.buscarPorId(idJefeInmediato);
                if (optJefe.isPresent() && idEmpleado.equals(optJefe.get().getIdJefeInmediato())) {
                    throw new IllegalArgumentException("Error: Ciclo jerárquico detectado. El jefe seleccionado es subordinado directo de este colaborador.");
                }
            } catch (SQLException ignored) {
            }
        }
    }
    
    public void guardarEmpleado(Empleado emp, String rolActual) {
        validarPermisoEdicion(rolActual);
        if (emp == null) {
            throw new IllegalArgumentException("Error: Los datos del empleado no pueden ser nulos.");
        }
        
        Long idExcluido = (emp.getIdEmpleado() != null && emp.getIdEmpleado() > 0) ? emp.getIdEmpleado() : null;
        
        // T3.06 Regla que impide ser su propio jefe y validación de jefe inmediato
        validarJefeInmediato(idExcluido, emp.getIdJefeInmediato());

        // T2.09 Validar duplicados (US-44)
        if (emp.getDpi() != null && empleadoDAO.existeDpi(emp.getDpi(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un empleado registrado con el DPI " + emp.getDpi());
        }
        if (emp.getNit() != null && !emp.getNit().trim().isEmpty() && empleadoDAO.existeNit(emp.getNit(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un empleado registrado con el NIT " + emp.getNit());
        }
        if (emp.getCorreoPersonal() != null && !emp.getCorreoPersonal().trim().isEmpty() && empleadoDAO.existeCorreoPersonal(emp.getCorreoPersonal(), idExcluido)) {
            throw new IllegalArgumentException("Error: Ya existe un empleado con el correo personal " + emp.getCorreoPersonal());
        }
        
        // T2.09 / T3.06 / T3.10 Validar referencias existentes (área, puesto, estado, nivel académico)
        if (emp.getIdAreaPrincipal() != null && !empleadoDAO.existeReferencia("area", "id_area", emp.getIdAreaPrincipal())) {
            throw new IllegalArgumentException("Error: El área seleccionada no existe en la base de datos.");
        }
        if (emp.getIdPuestoActual() != null && !empleadoDAO.existeReferencia("puesto", "id_puesto", emp.getIdPuestoActual())) {
            throw new IllegalArgumentException("Error: El puesto seleccionado no existe en la base de datos.");
        }
        if (emp.getIdEstadoEmpleado() != null && !empleadoDAO.existeReferencia("estado_empleado", "id_estado_empleado", emp.getIdEstadoEmpleado())) {
            throw new IllegalArgumentException("Error: El estado laboral seleccionado no existe.");
        }
        if (emp.getIdNivelAcademico() != null && !empleadoDAO.existeReferencia("nivel_academico", "id_nivel_academico", emp.getIdNivelAcademico())) {
            throw new IllegalArgumentException("Error: El nivel académico seleccionado no existe en la base de datos.");
        }
        
        try {
            if (idExcluido == null) {
                long id = empleadoDAO.insertar(emp);
                emp.setIdEmpleado(id);
                auditoriaService.registrarEvento("CREAR_EMPLEADO", "empleado", id, null, null, emp.getNombre());
            } else {
                empleadoDAO.actualizar(emp);
                auditoriaService.registrarEvento("ACTUALIZAR_EMPLEADO", "empleado", emp.getIdEmpleado(), null, null, emp.getNombre());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar en base de datos: " + e.getMessage());
        }
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
            auditoriaService.registrarEvento("ASIGNAR_JEFE", "empleado", idEmpleado, "id_jefe_inmediato",
                    anterior != null ? String.valueOf(anterior) : null,
                    idJefeInmediato != null ? String.valueOf(idJefeInmediato) : null);
        } catch (SQLException e) {
            throw new RuntimeException("Error al asignar jefe inmediato: " + e.getMessage(), e);
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
            auditoriaService.registrarEvento("ASIGNAR_AREA", "empleado", idEmpleado, "id_area_principal",
                    anterior != null ? String.valueOf(anterior) : null,
                    idAreaPrincipal != null ? String.valueOf(idAreaPrincipal) : null);
        } catch (SQLException e) {
            throw new RuntimeException("Error al asignar área principal: " + e.getMessage(), e);
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
            auditoriaService.registrarEvento("ASIGNAR_PUESTO", "empleado", idEmpleado, "id_puesto_actual",
                    anterior != null ? String.valueOf(anterior) : null,
                    idPuestoActual != null ? String.valueOf(idPuestoActual) : null);
        } catch (SQLException e) {
            throw new RuntimeException("Error al asignar puesto actual: " + e.getMessage(), e);
        }
    }

    // T3.10 Asignación de nivel académico actual en servicio
    public void asignarNivelAcademico(long idEmpleado, Long idNivelAcademico, String rolActual) {
        validarPermisoEdicion(rolActual);
        if (idNivelAcademico != null && !empleadoDAO.existeReferencia("nivel_academico", "id_nivel_academico", idNivelAcademico)) {
            throw new IllegalArgumentException("Error: El nivel académico seleccionado no existe en la base de datos.");
        }
        try {
            Empleado emp = empleadoDAO.buscarPorId(idEmpleado)
                    .orElseThrow(() -> new IllegalArgumentException("Error: El colaborador no existe."));
            Long anterior = emp.getIdNivelAcademico();
            empleadoDAO.actualizarNivelAcademico(idEmpleado, idNivelAcademico);
            auditoriaService.registrarEvento("ASIGNAR_NIVEL_ACADEMICO", "empleado", idEmpleado, "id_nivel_academico",
                    anterior != null ? String.valueOf(anterior) : null,
                    idNivelAcademico != null ? String.valueOf(idNivelAcademico) : null);
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar nivel académico: " + e.getMessage(), e);
        }
    }
}