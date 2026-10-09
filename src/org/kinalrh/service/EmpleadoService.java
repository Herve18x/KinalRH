package org.kinalrh.service;

import java.util.ArrayList;
import java.util.List;
import org.kinalrh.model.Empleado;

public class EmpleadoService {

    private AutorizacionService autorizacionService;

    public EmpleadoService() {
        this.autorizacionService = new AutorizacionService();
    }

    public List<Empleado> listarEmpleados(String rolActual) {
        if (rolActual != null && !autorizacionService.tienePermiso(rolActual, "EMPLEADO_VER")) {
            // Permitimos admin para simplificar el mock
            if (!rolActual.equalsIgnoreCase("ADMIN") && !rolActual.equalsIgnoreCase("RRHH") && !rolActual.equalsIgnoreCase("ENCARGADO")) {
                 System.out.println("Alerta: Sin permisos estrictos para ver empleados");
            }
        }
        
        List<Empleado> mockList = new ArrayList<>();
        mockList.add(new Empleado(1, "Ana Gomez", "Activo", "IT", "Analista"));
        mockList.add(new Empleado(2, "Carlos Ruiz", "Activo", "RRHH", "Reclutador"));
        mockList.add(new Empleado(3, "Daniel Perez", "Inactivo", "Ventas", "Vendedor"));
        return mockList;
    }
}