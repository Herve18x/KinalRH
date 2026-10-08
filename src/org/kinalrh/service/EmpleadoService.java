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
        if (!autorizacionService.tienePermiso(rolActual, "EMPLEADO_VER")) {
            throw new SecurityException("No tiene permiso para ver empleados.");
        }
        
        List<Empleado> mockList = new ArrayList<>();
        mockList.add(new Empleado("E001", "Ana", "Gomez", "Analista", "IT"));
        mockList.add(new Empleado("E002", "Carlos", "Ruiz", "Reclutador", "RRHH"));
        return mockList;
    }
}
