package org.kinalrh.service;

import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.kinalrh.PruebaBD;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.model.*;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;

public class ImportacionXlsxServiceTest {
    public static void main(String[] args) throws Exception {
        Path raiz=Path.of(args[0]);
        try(PruebaBD bd=new PruebaBD(raiz,false)) {
            preparar(bd);
            AtomicInteger conexiones=new AtomicInteger(),commits=new AtomicInteger(),rollbacks=new AtomicInteger(),restauradas=new AtomicInteger();
            DataSource datos=(DataSource)Proxy.newProxyInstance(DataSource.class.getClassLoader(),new Class[]{DataSource.class},(p,m,a)-> {
                if(!m.getName().equals("getConnection")) return invocar(m,bd.datos,a);
                conexiones.incrementAndGet();
                Connection c=bd.datos.getConnection();
                return Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class[]{Connection.class},(pc,mc,ac)-> {
                    if(mc.getName().equals("commit")) commits.incrementAndGet();
                    if(mc.getName().equals("rollback")) rollbacks.incrementAndGet();
                    if(mc.getName().equals("setAutoCommit") && Boolean.TRUE.equals(ac[0])) restauradas.incrementAndGet();
                    return invocar(mc,c,ac);
                });
            });
            ImportacionXlsxService servicio=new ImportacionXlsxService(datos);
            Path valido=raiz.resolve("test/fixtures/lote_valido.xlsx");
            var lectura=servicio.leer(valido);
            verificar(lectura.encabezados().equals(ValidadorImportacion.ENCABEZADOS),"Siete encabezados");
            verificar(lectura.filas().size()==2 && lectura.filas().get(0).getNumeroFila()==2,"Números de fila");
            var lote=servicio.previsualizar(valido);
            verificar(lote.getFilas().get(0).getAccion()==FilaImportacion.Accion.ACTUALIZAR,"DPI existente");
            verificar(lote.getFilas().get(1).getAccion()==FilaImportacion.Accion.NUEVO,"DPI nuevo");
            verificar(bd.numero("SELECT COUNT(*) FROM empleado")==1,"Previsualización no escribe");
            bd.sql("UPDATE estado_empleado SET activo=FALSE WHERE codigo='ACTIVO'");
            verificar(servicio.previsualizar(valido).tieneErrores(),"Estado conocido pero inactivo");
            bd.sql("UPDATE estado_empleado SET activo=TRUE WHERE codigo='ACTIVO'");
            bd.sql("UPDATE area SET activo=FALSE WHERE codigo='AREA_B'");
            bd.sql("UPDATE puesto SET activo=FALSE WHERE codigo='PUESTO_B'");
            var referenciasInactivas=servicio.previsualizar(valido);
            verificar(referenciasInactivas.getFilas().get(1).getErrores().size()==2,"Área y puesto inactivos se acumulan");
            bd.sql("UPDATE area SET activo=TRUE WHERE codigo='AREA_B'");
            bd.sql("UPDATE puesto SET activo=TRUE WHERE codigo='PUESTO_B'");
            lote.cancelar();
            denegado(()->servicio.confirmar(lote));
            verificar(bd.numero("SELECT COUNT(*) FROM empleado")==1,"Cancelar no escribe");
            var invalido=servicio.previsualizar(raiz.resolve("test/fixtures/lote_invalido.xlsx"));
            verificar(invalido.tieneErrores(),"Lote inválido");
            verificar(invalido.getFilas().get(0).getErrores().size()>=8,"Acumula errores");
            verificar(invalido.getFilas().get(1).getErrores().stream().anyMatch(e->e.columna().equals("dpi")),"Duplicado en ambas filas");
            verificar(invalido.getFilas().get(2).getErrores().stream().anyMatch(e->e.columna().equals("dpi")),"DPI largo");
            try{ servicio.confirmar(invalido); throw new AssertionError("Inválido confirmado"); }catch(IllegalArgumentException esperado) { }
            for(Path archivo:List.of(Path.of("inexistente.xlsx"),Path.of("corrupto.xlsx"))) {
                if(archivo.getFileName().toString().equals("corrupto.xlsx")) Files.writeString(archivo,"Contenido corrupto");
                try{ servicio.leer(archivo); throw new AssertionError("Archivo inválido leído"); }catch(IllegalArgumentException esperado) { }
            }
            Files.deleteIfExists(Path.of("corrupto.xlsx"));
            var rollback=servicio.previsualizar(valido);
            bd.sql("CREATE TRIGGER fallo_importacion BEFORE INSERT ON empleado FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo controlado'");
            conexiones.set(0);
            try{ servicio.confirmar(rollback); throw new AssertionError("Fallo no detectado"); }catch(IllegalStateException esperado) { }
            verificar(conexiones.get()==1 && commits.get()==0 && rollbacks.get()==1 && restauradas.get()==1,"Una conexión, rollback y autoCommit restaurado");
            verificar(bd.numero("SELECT COUNT(*) FROM empleado")==1,"Rollback total del lote");
            verificar(bd.numero("SELECT COUNT(*) FROM empleado WHERE primer_apellido='Original'")==1,"Actualización anterior revertida");
            bd.sql("DROP TRIGGER fallo_importacion");
            var exito=servicio.previsualizar(valido);
            conexiones.set(0);
            var resultado=servicio.confirmar(exito);
            verificar(resultado.nuevos()==1 && resultado.actualizados()==1,"Conteos del commit");
            verificar(conexiones.get()==1 && commits.get()==1 && restauradas.get()==2,"Una conexión y restauración tras commit");
            Empleado anterior=new EmpleadoDAOImpl(bd.datos).buscarPorId(resultado.ids().get(0)).orElseThrow();
            verificar("Conservado".equals(anterior.getSegundoNombre()) && "prueba@example.test".equals(anterior.getCorreoPersonal()),"Campos ajenos conservados");
            verificar(anterior.getIdAreaPrincipal()==1 && anterior.getIdPuestoActual()==1 && anterior.getFechaIngresoKinal().equals(LocalDate.of(2020,1,1)),"Opcionales vacíos conservados");
            denegado(()->servicio.confirmar(exito));
            Usuario visor=new Usuario(); visor.setIdUsuario(2L); visor.setRol("visor"); SessionManager.getInstance().login(visor);
            conexiones.set(0);
            denegado(()->servicio.previsualizar(valido));
            verificar(conexiones.get()==0,"Sin permiso no consulta previsualización");
            denegado(()->servicio.confirmar(exito));
            SessionManager.getInstance().logout();
            denegado(()->servicio.previsualizar(valido));
            System.out.println("ImportacionXlsxServiceTest: lectura, validación, clasificación, cancelación, commit, rollback, autorización y conexión única correctos.");
        }
    }
    private static Object invocar(Method metodo,Object objeto,Object[] args) throws Throwable {
        try{return metodo.invoke(objeto,args);}catch(InvocationTargetException e){throw e.getCause();}
    }
    public static void preparar(PruebaBD bd) throws Exception {
        bd.sql("INSERT INTO area (codigo,nombre) VALUES ('AREA_A','Área A'),('AREA_B','Área B')");
        bd.sql("INSERT INTO puesto (codigo,nombre) VALUES ('PUESTO_A','Puesto A'),('PUESTO_B','Puesto B')");
        bd.sql("INSERT INTO usuario(nombre_usuario,password_hash,nombre_completo) VALUES ('revision_admin','sin acceso','Administrador ficticio'),('revision_visor','sin acceso','Visor ficticio')");
        bd.sql("INSERT INTO usuario_rol SELECT 1,id_rol FROM rol WHERE nombre='admin'");
        Empleado e=new Empleado(); e.setDpi("FICTICIO-001"); e.setPrimerNombre("Elena"); e.setPrimerApellido("Original"); e.setIdEstadoEmpleado(1L);
        e.setSegundoNombre("Conservado"); e.setCorreoPersonal("prueba@example.test"); e.setIdAreaPrincipal(1L); e.setIdPuestoActual(1L); e.setFechaIngresoKinal(LocalDate.of(2020,1,1));
        new EmpleadoDAOImpl(bd.datos).insertar(e);
        Usuario admin=new Usuario(); admin.setIdUsuario(1L); admin.setRol("ADMIN"); SessionManager.getInstance().login(admin);
    }
    public static void denegado(Runnable accion) {
        try{accion.run();throw new AssertionError("Operación indebidamente permitida");}catch(SecurityException esperado) { }
    }
}
