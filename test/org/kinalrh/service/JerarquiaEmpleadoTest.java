package org.kinalrh.service;

import java.lang.reflect.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.model.Empleado;
import static org.kinalrh.PruebaBD.verificar;

public class JerarquiaEmpleadoTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), false)) {
            ImportacionXlsxServiceTest.preparar(bd);
            EmpleadoDAO dao = new EmpleadoDAOImpl(bd.datos);
            long segundo = dao.insertar(empleado("FICTICIO-002")), tercero = dao.insertar(empleado("FICTICIO-003"));
            bd.sql("UPDATE empleado SET id_jefe_inmediato=" + segundo + " WHERE id_empleado=1");
            bd.sql("UPDATE empleado SET id_jefe_inmediato=" + tercero + " WHERE id_empleado=" + segundo);
            EmpleadoService servicio = new EmpleadoService(dao);
            List<String> fallos = new ArrayList<>();
            try { servicio.asignarJefeInmediato(tercero,1L,"ADMIN"); fallos.add("Ciclo de tres empleados permitido"); }
            catch (IllegalArgumentException esperado) { }
            bd.sql("UPDATE empleado SET id_jefe_inmediato=NULL WHERE id_empleado=" + tercero);
            Empleado inexistente = empleado("FICTICIO-999"); inexistente.setIdEmpleado(99999L);
            try { servicio.guardarEmpleado(inexistente,"ADMIN"); fallos.add("Edición inexistente indicada como éxito"); }
            catch (IllegalArgumentException esperado) { }
            EmpleadoDAO fallo = (EmpleadoDAO) Proxy.newProxyInstance(EmpleadoDAO.class.getClassLoader(), new Class<?>[]{EmpleadoDAO.class}, (p,m,a) -> {
                if (m.getName().equals("buscarPorId")) throw new SQLException("Fallo controlado");
                try { return m.invoke(dao,a); } catch (InvocationTargetException ex) { throw ex.getCause(); }
            });
            try { new EmpleadoService(fallo).validarJefeInmediato(1L,tercero); fallos.add("Error SQL en jerarquía ignorado"); }
            catch (IllegalStateException esperado) { }
            fallos.forEach(f -> System.out.println("FAIL: " + f));
            verificar(fallos.isEmpty(), "Fallos de jerarquía/edición: " + fallos.size());
            servicio.asignarJefeInmediato(1,tercero,"ADMIN");
            verificar(dao.buscarPorId(1).orElseThrow().getIdJefeInmediato() == tercero, "Asignación válida");
            System.out.println("JerarquiaEmpleadoTest: ciclos transitivos, error SQL, edición inexistente y asignación válida correctos.");
        }
    }
    private static Empleado empleado(String dpi) {
        Empleado e = new Empleado(); e.setDpi(dpi); e.setPrimerNombre("Nombre"); e.setPrimerApellido("Prueba"); e.setIdEstadoEmpleado(1L); return e;
    }
}
