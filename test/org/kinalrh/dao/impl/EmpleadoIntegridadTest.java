package org.kinalrh.dao.impl;

import java.nio.file.Path;
import java.util.function.Consumer;
import org.kinalrh.PruebaBD;
import org.kinalrh.model.Empleado;
import static org.kinalrh.PruebaBD.verificar;

public class EmpleadoIntegridadTest {
    public static void main(String[] args) throws Exception {
        try(PruebaBD bd=new PruebaBD(Path.of(args[0]),false)) {
            EmpleadoDAOImpl dao=new EmpleadoDAOImpl(bd.datos);
            Empleado original=empleado();long id=dao.insertar(original); original.setIdEmpleado(id);
            rechazar(()->dao.insertar(empleado()));
            comprobar(bd,id);
            for(Consumer<Empleado> cambiar:java.util.List.<Consumer<Empleado>>of(
                    e->e.setIdEstadoEmpleado(99999L),e->e.setIdAreaPrincipal(99999L),
                    e->e.setIdPuestoActual(99999L),e->e.setPrimerNombre(" "))) {
                Empleado e=empleado();e.setDpi("FICTICIO-FALLO");cambiar.accept(e);
                rechazar(()->dao.insertar(e));comprobar(bd,id);
                Empleado edicion=empleado();edicion.setIdEmpleado(id);cambiar.accept(edicion);
                rechazar(()->dao.actualizar(edicion));comprobar(bd,id);
            }
            original.setIdEstadoEmpleado(bd.numero("SELECT id_estado_empleado FROM estado_empleado WHERE codigo='RETIRADO'"));
            verificar(dao.actualizar(original),"Cambio a estado válido");
            verificar(dao.buscarPorId(id).orElseThrow().getIdEstadoEmpleado().equals(original.getIdEstadoEmpleado()),"Estado persistido");
            verificar(bd.numero("SELECT COUNT(*) FROM empleado_area ea LEFT JOIN empleado e ON e.id_empleado=ea.id_empleado LEFT JOIN area a ON a.id_area=ea.id_area WHERE e.id_empleado IS NULL OR a.id_area IS NULL")==0,"Sin huérfanos");
            System.out.println("EmpleadoIntegridadTest: duplicados, FK, obligatorios, conservación y estado válido correctos.");
        }
    }
    private static void comprobar(PruebaBD bd,long id) throws Exception {
        verificar(bd.numero("SELECT COUNT(*) FROM empleado")==1,"Sin altas parciales");
        verificar(bd.numero("SELECT COUNT(*) FROM empleado WHERE id_empleado="+id+" AND primer_nombre='Original' AND id_area_principal IS NULL AND id_puesto_actual IS NULL AND id_estado_empleado=1")==1,"Datos anteriores conservados");
    }
    private static Empleado empleado() { Empleado e=new Empleado();e.setDpi("FICTICIO-BASE");e.setPrimerNombre("Original");e.setPrimerApellido("Prueba");e.setIdEstadoEmpleado(1L);return e; }
    private interface Accion { void ejecutar() throws Exception; }
    private static void rechazar(Accion accion) throws Exception {
        try{accion.ejecutar();throw new AssertionError("Dato inválido aceptado");}
        catch(IllegalArgumentException ex){
            verificar(!ex.getMessage().contains("FICTICIO") && !ex.getMessage().contains("INSERT") && !ex.getMessage().contains("UPDATE") && ex.getCause()==null,"Error entendible sin datos ni SQL");
        }
    }
}
