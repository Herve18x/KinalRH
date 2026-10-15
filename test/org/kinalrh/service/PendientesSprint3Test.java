package org.kinalrh.service;

import java.nio.file.Path;
import java.sql.*;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.*;
import org.kinalrh.model.*;
import static org.kinalrh.PruebaBD.verificar;

public class PendientesSprint3Test {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), false)) {
            bd.sql("INSERT INTO area (codigo,nombre) VALUES ('A','Área A'),('B','Área B')");
            EmpleadoDAOImpl dao = new EmpleadoDAOImpl(bd.datos);
            Empleado e = new Empleado();
            e.setDpi("FICTICIO-001"); e.setPrimerNombre("Elena"); e.setPrimerApellido("Prueba");
            e.setIdEstadoEmpleado(1L); e.setIdAreaPrincipal(1L);
            long id = dao.insertar(e);
            bd.sql("INSERT INTO empleado_area (id_empleado,id_area) VALUES (" + id + ",2)");
            verificar(dao.buscar("Elena", null, null, null,25,0).size()==1,"Nombre parcial");
            verificar(dao.buscar(null,"FICTICIO-001",null,null,25,0).size()==1,"DPI exacto");
            verificar(dao.buscar(null,"FICTICIO",null,null,25,0).isEmpty(),"DPI no parcial");
            verificar(dao.buscar(null,null,2L,null,25,0).isEmpty(),"Solo área principal");
            verificar(dao.buscar(null,null,1L,null,25,0).size()==1,"Área aislada");
            verificar(dao.buscar(null,null,null,1L,25,0).size()==1,"Estado aislado");
            verificar(dao.buscar("El", "FICTICIO-001",1L,1L,25,0).size()==1,"Combinación sin duplicados");
            verificar(dao.buscar(" ","",null,null,25,0).size()==1,"Vacíos no filtran");
            verificar(dao.buscar("' OR 1=1 --",null,null,null,25,0).isEmpty(),"Parámetros seguros");
            verificar(dao.buscar("%",null,null,null,25,0).isEmpty(),"LIKE literal");
            verificar(dao.buscar(null,null,null,null,1,1).isEmpty(),"Paginación");
            LogroDAOImpl logros = new LogroDAOImpl();
            Logro l = logro("Logro uno");
            long primero = logros.guardarConEmpleado(l,id,false,null);
            logros.guardarConEmpleado(logro("Logro dos"),id,true,null);
            verificar(logros.listarPorEmpleado(id).size()==2,"Dos logros distintos");
            try { logros.asignarLogroAEmpleado(primero,id); throw new AssertionError("Duplicado permitido"); }
            catch (SQLException esperado) { verificar(esperado.getErrorCode()==1062,"PK vínculo"); }
            long cantidad = bd.numero("SELECT COUNT(*) FROM logro");
            try { logros.guardarConEmpleado(logro("Debe revertirse"),99999,false,null); throw new AssertionError("FK permitida"); }
            catch (SQLException esperado) { }
            verificar(bd.numero("SELECT COUNT(*) FROM logro")==cantidad,"Rollback logro + vínculo");
            AuditoriaService audit = new AuditoriaService();
            for (String campo : new String[]{"dpi","password","password_hash","contraseña","correo","alta", "desconocido"}) {
                audit.registrarEvento(null,"PRUEBA","empleado",id,campo,"dato privado","dato privado");
            }
            audit.registrarEvento(null,"PRUEBA","empleado",id,"id_area_principal","1","2");
            verificar(bd.numero("SELECT COUNT(*) FROM auditoria WHERE valor_nuevo='dato privado' OR valor_anterior='dato privado'")==0,"Datos excluidos");
            verificar(bd.numero("SELECT COUNT(*) FROM auditoria WHERE campo='id_area_principal' AND valor_nuevo='2'")==1,"Trazabilidad mínima");
            System.out.println("PendientesSprint3Test: filtros, transacciones de logros y exclusiones de auditoría correctos.");
        }
    }
    private static Logro logro(String nombre) { Logro l = new Logro(); l.setNombre(nombre); l.setIdTipoLogro(1L); return l; }
}
