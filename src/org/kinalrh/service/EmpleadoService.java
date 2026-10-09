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
        if (rolActual != null && !autorizacionService.tienePermiso(rolActual, "EMPLEADO_VER")) {
            if (!rolActual.equalsIgnoreCase("ADMIN") && !rolActual.equalsIgnoreCase("RRHH") && !rolActual.equalsIgnoreCase("ENCARGADO")) {
                 System.out.println("Alerta: Sin permisos estrictos para ver empleados");
            }
        }
        try {
            return empleadoDAO.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Empleado buscarPorId(long idEmpleado) {
        try {
            Optional<Empleado> opt = empleadoDAO.buscarPorId(idEmpleado);
            return opt.orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void guardarEmpleado(Empleado emp) {
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