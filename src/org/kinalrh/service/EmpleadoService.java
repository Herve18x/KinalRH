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

    public EmpleadoService() {
        this.autorizacionService = new AutorizacionService();
        this.empleadoDAO = new EmpleadoDAOImpl();
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
    
    public void guardarEmpleado(Empleado emp, String rolActual) {
        if (rolActual == null || rolActual.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado: Sesión no válida.");
        }
        if (!autorizacionService.tienePermiso(rolActual, "EMPLEADO_EDITAR") && !rolActual.equalsIgnoreCase("ADMIN") && !rolActual.equalsIgnoreCase("RRHH")) {
            throw new SecurityException("Acceso denegado: Su cuenta no tiene permiso para modificar empleados.");
        }
        
        Long idExcluido = (emp.getIdEmpleado() != null && emp.getIdEmpleado() > 0) ? emp.getIdEmpleado() : null;
        
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
        
        // T2.09 Validar referencias existentes
        if (emp.getIdAreaPrincipal() != null && !empleadoDAO.existeReferencia("area", "id_area", emp.getIdAreaPrincipal())) {
            throw new IllegalArgumentException("Error: El área seleccionada no existe en la base de datos.");
        }
        if (emp.getIdPuestoActual() != null && !empleadoDAO.existeReferencia("puesto", "id_puesto", emp.getIdPuestoActual())) {
            throw new IllegalArgumentException("Error: El puesto seleccionado no existe en la base de datos.");
        }
        if (emp.getIdEstadoEmpleado() != null && !empleadoDAO.existeReferencia("estado_empleado", "id_estado_empleado", emp.getIdEstadoEmpleado())) {
            throw new IllegalArgumentException("Error: El estado laboral seleccionado no existe.");
        }
        
        try {
            if (idExcluido == null) {
                long id = empleadoDAO.insertar(emp);
                emp.setIdEmpleado(id);
            } else {
                empleadoDAO.actualizar(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar en base de datos: " + e.getMessage());
        }
    }
}