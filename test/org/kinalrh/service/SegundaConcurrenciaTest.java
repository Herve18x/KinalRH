package org.kinalrh.service;

import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.concurrent.*;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.EmpleadoDAO;
import org.kinalrh.dao.impl.*;
import org.kinalrh.model.*;
import static org.kinalrh.PruebaBD.verificar;

public class SegundaConcurrenciaTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd=new PruebaBD(Path.of(args[0]),false);
                ExecutorService hilos=Executors.newFixedThreadPool(2)) {
            ImportacionXlsxServiceTest.preparar(bd);
            UsuarioDAOImpl usuarios=new UsuarioDAOImpl();
            long admin=bd.numero("SELECT id_rol FROM rol WHERE nombre='admin'"), visor=bd.numero("SELECT id_rol FROM rol WHERE nombre='visor'");
            for(int i=0;i<10;i++) {
                bd.sql("DELETE FROM usuario_rol WHERE id_usuario=2");
                CountDownLatch salida=new CountDownLatch(1);
                Future<Boolean> revocar=hilos.submit(() -> { salida.await(); try { usuarios.asignarRol(1,visor); return true; }
                    catch(IllegalStateException ex) { verificar(ex.getMessage().contains("última cuenta"),"Rechazo por invariante, no por error SQL");return false; } });
                Future<?> asignar=hilos.submit(() -> { salida.await(); usuarios.asignarRol(2,admin);return null; });
                salida.countDown(); boolean revocado=revocar.get(15,TimeUnit.SECONDS); asignar.get(15,TimeUnit.SECONDS);
                verificar(usuarios.contarAdministradoresActivos()==(revocado?1:2),"Revocar/asignar conserva admin");
                usuarios.asignarRol(1,admin);
                Usuario a=usuarios.buscarPorId(2).orElseThrow(), b=usuarios.buscarPorId(2).orElseThrow();
                a.setNombreCompleto("Edición A");a.setCorreo("a@example.test");a.setRol("visor");
                b.setNombreCompleto("Edición B");b.setCorreo("b@example.test");b.setRol("encargado");
                CountDownLatch editar=new CountDownLatch(1);
                Future<?> fa=hilos.submit(() -> {editar.await();usuarios.actualizar(a);return null;});
                Future<?> fb=hilos.submit(() -> {editar.await();usuarios.actualizar(b);return null;});
                editar.countDown();fa.get(15,TimeUnit.SECONDS);fb.get(15,TimeUnit.SECONDS);
                Usuario fin=usuarios.buscarPorId(2).orElseThrow();
                verificar((fin.getNombreCompleto().equals("Edición A") && fin.getCorreo().equals("a@example.test") && fin.getRol().equalsIgnoreCase("visor"))
                        || (fin.getNombreCompleto().equals("Edición B") && fin.getCorreo().equals("b@example.test") && fin.getRol().equalsIgnoreCase("encargado")),"Edición atómica, sin mezcla entre fila y rol");
            }
            EmpleadoDAOImpl empleados=new EmpleadoDAOImpl(bd.datos);
            Empleado nuevo=new Empleado();nuevo.setDpi("CONCURRENTE-2");nuevo.setPrimerNombre("Segundo");nuevo.setPrimerApellido("Ficticio");nuevo.setIdEstadoEmpleado(1L);
            long segundo=empleados.insertar(nuevo);
            for(int i=0;i<10;i++) {
                bd.sql("UPDATE empleado SET id_jefe_inmediato=NULL");
                CyclicBarrier validados=new CyclicBarrier(2);
                CountDownLatch primeroConfirmado=new CountDownLatch(1);
                EmpleadoDAO sincronizado=(EmpleadoDAO)Proxy.newProxyInstance(EmpleadoDAO.class.getClassLoader(),new Class<?>[]{EmpleadoDAO.class},(p,m,a) -> {
                    boolean escritura=m.getName().equals("actualizar");
                    boolean primero=escritura && ((Empleado)a[0]).getIdEmpleado()==1L;
                    if(escritura) {validados.await(15,TimeUnit.SECONDS);if(!primero)verificar(primeroConfirmado.await(15,TimeUnit.SECONDS),"Primera escritura completa");}
                    try{return m.invoke(empleados,a);}catch(InvocationTargetException ex){throw ex.getCause();}
                    finally {if(primero)primeroConfirmado.countDown();}
                });
                EmpleadoService servicio=new EmpleadoService(sincronizado);
                Future<Boolean> uno=hilos.submit(() -> jerarquia(servicio,1,segundo));
                Future<Boolean> dos=hilos.submit(() -> jerarquia(servicio,segundo,1L));
                boolean a=uno.get(20,TimeUnit.SECONDS),b=dos.get(20,TimeUnit.SECONDS);
                verificar(a!=b,"Dos validaciones simultáneas no deben confirmar un ciclo A-B-A");
                verificar(bd.numero("SELECT COUNT(*) FROM empleado a JOIN empleado b ON a.id_jefe_inmediato=b.id_empleado AND b.id_jefe_inmediato=a.id_empleado")==0,"BD sin ciclo concurrente");
            }
            System.out.println("SegundaConcurrenciaTest: PASS diez carreras revocar/asignar, edición del mismo usuario y jerarquía recíproca.");
        }
    }
    private static boolean jerarquia(EmpleadoService s,long empleado,Long jefe) {
        try{s.asignarJefeInmediato(empleado,jefe,"ADMIN");return true;}
        catch(IllegalArgumentException ex){verificar(ex.getMessage().contains("jerárquico"),"Debe rechazar por ciclo");return false;}
    }
}
