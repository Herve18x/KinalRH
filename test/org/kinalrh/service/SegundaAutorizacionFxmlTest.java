package org.kinalrh.service;

import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.stage.Window;
import org.kinalrh.PruebaBD;
import org.kinalrh.controller.EmpleadoFormController;
import org.kinalrh.dao.impl.UsuarioDAOImpl;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;

/** Cada guarda nueva se invoca con un rol SQL mínimo, luego de revocar el permiso. */
public class SegundaAutorizacionFxmlTest {
    private static PruebaBD bd;
    private static final List<String> fallos = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        try (PruebaBD base = new PruebaBD(Path.of(args[0]), true)) {
            bd = base;
            ImportacionXlsxServiceTest.preparar(bd);
            bd.sql("INSERT INTO usuario_rol SELECT 2,id_rol FROM rol WHERE nombre='visor'");
            bd.sql("INSERT INTO solicitud_cambio(id_empleado,tipo_cambio,motivo,solicitado_por) VALUES (1,'CAMBIO_PUESTO','Solicitud ficticia',1)");
            CountDownLatch inicio = new CountDownLatch(1); Platform.startup(inicio::countDown);
            verificar(inicio.await(15,TimeUnit.SECONDS), "Inicio JavaFX");
            FutureTask<Void> tarea = new FutureTask<>(() -> {
                for (String[] caso : List.of(
                        new String[]{"Directorio.fxml","tileDirectorio","EMPLEADO_VER","cargarEmpleados"},
                        new String[]{"Empleados.fxml","cmbFiltroJefe","EMPLEADO_VER","cargarCatalogosFiltros"},
                        new String[]{"FormularioEmpleado.fxml","cmbJefe","EMPLEADO_CREAR","cargarCatalogos"},
                        new String[]{"FormularioUsuario.fxml","cmbEmpleado","USUARIO_GESTIONAR","configurarFormulario"},
                        new String[]{"Logros.fxml","cmbEmpleado","LOGRO_GESTIONAR","cargarEmpleados"},
                        new String[]{"Logros.fxml","cmbEmpleado","EMPLEADO_VER","cargarEmpleados"},
                        new String[]{"Roles.fxml","tablaRoles","ROL_GESTIONAR","cargarRoles"},
                        new String[]{"Bandeja.fxml","tablaSolicitudes","CAMBIO_CREAR","cargarSolicitudes"},
                        new String[]{"Bandeja.fxml","tablaSolicitudes","CAMBIO_APROBAR_RH","cargarSolicitudes"},
                        new String[]{"Bandeja.fxml","tablaSolicitudes","CAMBIO_APROBAR_GERENCIA","cargarSolicitudes"})) {
                    try { probarGuarda(caso); }
                    catch (Throwable ex) { fallos.add(Arrays.toString(caso)+": "+causa(ex)); }
                }
                for (String[] permisos : List.of(new String[]{"EMPLEADO_CREAR"},new String[]{"EMPLEADO_VER"},
                        new String[]{"EMPLEADO_CREAR","EMPLEADO_VER"},new String[0])) {
                    try { formulario(permisos); }
                    catch (Throwable ex) { fallos.add("A "+Arrays.toString(permisos)+": "+causa(ex)); }
                }
                try { bandeja(); roles(); }
                catch (Throwable ex) { fallos.add("Acciones: "+causa(ex)); }
                return null;
            });
            Platform.runLater(tarea);
            try { tarea.get(90,TimeUnit.SECONDS); } finally { Platform.exit(); }
            fallos.forEach(System.out::println);
            verificar(fallos.isEmpty(), "Guardas/formulario: "+fallos);
            System.out.println("SegundaAutorizacionFxmlTest: PASS permisos positivos/negativos, cuatro perfiles de alta, bandeja y roles.");
        }
    }
    private static void permisos(String... codigos) throws Exception {
        bd.sql("DELETE rp FROM rol_permiso rp JOIN rol r ON r.id_rol=rp.id_rol WHERE r.nombre='visor'");
        for (String codigo : codigos) bd.sql("INSERT INTO rol_permiso SELECT r.id_rol,p.id_permiso FROM rol r JOIN permiso p ON p.codigo='"+codigo+"' WHERE r.nombre='visor'");
        SessionManager.getInstance().login(new UsuarioDAOImpl().buscarPorId(2).orElseThrow());
    }
    private static FXMLLoader cargar(String archivo) throws Exception {
        FXMLLoader l = new FXMLLoader(SegundaAutorizacionFxmlTest.class.getResource("/org/kinalrh/view/"+archivo));
        Parent p = l.load(); new Scene(p); p.applyCss(); p.layout(); return l;
    }
    private static void probarGuarda(String[] caso) throws Exception {
        permisos(caso[2]); FXMLLoader l = cargar(caso[0]);
        Object control = l.getNamespace().get(caso[1]);
        int n = control instanceof ComboBox<?> c ? c.getItems().size() : control instanceof TableView<?> t ? t.getItems().size()
                : control instanceof TilePane p ? p.getChildren().size() : -1;
        verificar(n>0,"CON permiso debe cargar datos reales: "+caso[0]);
        invocar(l.getController(),caso[3]);
        permisos(); denegar(() -> invocar(l.getController(),caso[3]));
        System.out.println("PASS guarda +/- "+caso[0]+" "+caso[3]+" "+caso[2]);
    }
    private static void formulario(String[] codigos) throws Exception {
        permisos(codigos);
        if (codigos.length==0) { denegar(() -> cargar("FormularioEmpleado.fxml")); return; }
        FXMLLoader l = cargar("FormularioEmpleado.fxml");
        EmpleadoFormController c = l.getController(); c.setEmpleado(null);
        ((TextField)l.getNamespace().get("txtDpi")).setText("123456789"+String.format("%04d",bd.numero("SELECT COUNT(*) FROM empleado")));
        ((TextField)l.getNamespace().get("txtPrimerNombre")).setText("Alta");
        ((TextField)l.getNamespace().get("txtPrimerApellido")).setText("Ficticia");
        long antes=bd.numero("SELECT COUNT(*) FROM empleado"); c.eventoGuardar(null);
        boolean crear=Arrays.asList(codigos).contains("EMPLEADO_CREAR");
        verificar(bd.numero("SELECT COUNT(*) FROM empleado")==antes+(crear?1:0),"Alta solo con CREAR "+Arrays.toString(codigos));
        if (!crear) verificar(((Label)l.getNamespace().get("lblError")).isVisible(),"Rechazo de guardado visible");
        System.out.println("PASS A abrir/guardar "+Arrays.toString(codigos));
    }
    private static void roles() throws Exception {
        permisos("ROL_GESTIONAR"); FXMLLoader l=cargar("Roles.fxml");
        TableView<?> tabla=(TableView<?>)l.getNamespace().get("tablaRoles");
        Object rol=tabla.getItems().get(0);
        Method m=l.getController().getClass().getDeclaredMethod("cargarPermisosDeRol",org.kinalrh.model.Rol.class); m.setAccessible(true);
        m.invoke(l.getController(),rol); permisos(); denegar(() -> m.invoke(l.getController(),rol));
    }
    private static void bandeja() throws Exception {
        permisos("CAMBIO_CREAR"); FXMLLoader l=cargar("Bandeja.fxml"); Object c=l.getController();
        verificar(!((List<?>)invocar(c,"obtenerEmpleadosActivos")).isEmpty(),"Colaboradores para solicitud");
        cerrarDialogo(); invocar(c,"eventoNuevaSolicitud");
        Method guardar=c.getClass().getDeclaredMethod("guardarNuevaSolicitud",Long.class,String.class,String.class,String.class,String.class,String.class);
        guardar.setAccessible(true);
        long antes=bd.numero("SELECT COUNT(*) FROM solicitud_cambio");
        cerrarDialogo(); guardar.invoke(c,1L,"CAMBIO_PUESTO","puesto","Anterior","Nuevo","Solicitud de auditoría");
        verificar(bd.numero("SELECT COUNT(*) FROM solicitud_cambio")==antes+1,"CON CAMBIO_CREAR persiste solicitud");
        permisos(); denegar(() -> invocar(c,"obtenerEmpleadosActivos")); denegar(() -> invocar(c,"eventoNuevaSolicitud"));
        denegar(() -> guardar.invoke(c,1L,"CAMBIO_PUESTO","puesto","Anterior","Nuevo","Rechazada"));
        denegar(() -> invocar(c,"eventoAprobar")); denegar(() -> invocar(c,"eventoRechazar"));
        for (String p : List.of("CAMBIO_APROBAR_RH","CAMBIO_APROBAR_GERENCIA")) {
            permisos(p); FXMLLoader b=cargar("Bandeja.fxml");
            TableView<?> tabla=(TableView<?>)b.getNamespace().get("tablaSolicitudes");
            for (int i=0;i<tabla.getItems().size();i++) {
                if ("PENDIENTE".equals(((org.kinalrh.model.SolicitudCambio)tabla.getItems().get(i)).getEstado())) {
                    tabla.getSelectionModel().select(i); break;
                }
            }
            ((TextArea)b.getNamespace().get("txtComentario")).setText("Resolución ficticia");
            cerrarDialogo(); invocar(b.getController(),p.endsWith("RH")?"eventoAprobar":"eventoRechazar");
        }
        verificar(bd.numero("SELECT COUNT(*) FROM solicitud_cambio WHERE estado<>'PENDIENTE'")==2,"Ambas alternativas resuelven");
    }
    private static void cerrarDialogo() { Platform.runLater(() -> new ArrayList<>(Window.getWindows()).forEach(Window::hide)); }
    private static Object invocar(Object o,String nombre) throws Exception { Method m=o.getClass().getDeclaredMethod(nombre);m.setAccessible(true);return m.invoke(o); }
    @FunctionalInterface private interface Accion { void ejecutar() throws Exception; }
    private static void denegar(Accion a) throws Exception {
        try { a.ejecutar(); throw new AssertionError("SIN permiso debe denegar"); }
        catch (Exception ex) { if (!(causa(ex) instanceof SecurityException)) throw ex; }
    }
    private static Throwable causa(Throwable t) { while(t.getCause()!=null)t=t.getCause();return t; }
}
