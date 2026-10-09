package org.kinalrh.service;

import java.util.ArrayList;
import java.util.List;
import org.kinalrh.model.Empleado;

public class EmpleadoService {

    private AutorizacionService autorizacionService;
    
    // MOCK en memoria temporal para que funcione sin Base de Datos
    private static List<Empleado> mockList = new ArrayList<>();
    private static int nextId = 4;
    
    static {
        mockList.add(new Empleado(1, "Ana Gomez", "Activo", "IT", "Analista"));
        mockList.add(new Empleado(2, "Carlos Ruiz", "Activo", "RRHH", "Reclutador"));
        mockList.add(new Empleado(3, "Daniel Perez", "Inactivo", "Ventas", "Vendedor"));
    }

    public EmpleadoService() {
        this.autorizacionService = new AutorizacionService();
    }

    public List<Empleado> listarEmpleados(String rolActual) {
        if (rolActual != null && !autorizacionService.tienePermiso(rolActual, "EMPLEADO_VER")) {
            if (!rolActual.equalsIgnoreCase("ADMIN") && !rolActual.equalsIgnoreCase("RRHH") && !rolActual.equalsIgnoreCase("ENCARGADO")) {
                 System.out.println("Alerta: Sin permisos estrictos para ver empleados");
            }
        }
        return new ArrayList<>(mockList);
    }
    
    public Empleado buscarPorId(int idEmpleado) {
        for(Empleado e : mockList) {
            if(e.getIdEmpleado() == idEmpleado) return e;
        }
        return null;
    }
    
    public void guardarEmpleado(Empleado emp) {
        if(emp.getIdEmpleado() == 0) {
            emp.setIdEmpleado(nextId++);
            mockList.add(emp);
        } else {
            Empleado existente = buscarPorId(emp.getIdEmpleado());
            if(existente != null) {
                existente.setNombre(emp.getNombre());
                existente.setArea(emp.getArea());
                existente.setPuesto(emp.getPuesto());
                existente.setEstado(emp.getEstado());
            }
        }
    }
}