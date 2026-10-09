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
        // Asumiendo que el permiso es el mismo o requiere otro superior
        if (!autorizacionService.tienePermiso(rolActual, "EMPLEADO_EDITAR") && !rolActual.equalsIgnoreCase("ADMIN") && !rolActual.equalsIgnoreCase("RRHH")) {
            throw new SecurityException("Acceso denegado: Su cuenta no tiene permiso para modificar empleados.");
        }
        try {
            if (emp.getIdEmpleado() == null || emp.getIdEmpleado() == 0) {
                long id = empleadoDAO.insertar(emp);
                emp.setIdEmpleado(id);
            } else {
                empleadoDAO.actualizar(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}