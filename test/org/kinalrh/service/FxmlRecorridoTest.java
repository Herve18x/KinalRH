package org.kinalrh.service;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.TilePane;
import org.kinalrh.PruebaBD;
import org.kinalrh.model.Usuario;
import org.kinalrh.util.SessionManager;
import static org.kinalrh.PruebaBD.verificar;

/** Carga sin ventanas ni interacción, siempre con datos descartables. */
public class FxmlRecorridoTest {
    public static void main(String[] args) throws Exception {
        try (PruebaBD bd = new PruebaBD(Path.of(args[0]), true)) {
            ImportacionXlsxServiceTest.preparar(bd);
            bd.sql("INSERT INTO solicitud_cambio(id_empleado,tipo_cambio,motivo,solicitado_por) VALUES (1,'CAMBIO_PUESTO','Solicitud ficticia',1)");
            CountDownLatch inicio = new CountDownLatch(1);
            Platform.startup(inicio::countDown);
            verificar(inicio.await(15, TimeUnit.SECONDS), "Inicio de JavaFX");
            FutureTask<Void> tarea = new FutureTask<>(() -> {
                List<String> fallos = new ArrayList<>();
                try (var archivos = Files.list(Path.of(args[0]).resolve("src/org/kinalrh/view"))) {
                    for (Path archivo : archivos.filter(p -> p.toString().endsWith(".fxml")).sorted().toList()) {
                        String nombre = archivo.getFileName().toString();
                        try {
                            FXMLLoader cargador=loader(nombre);
                            Parent vista = cargador.load();
                            validarEnlaces(archivo,cargador,vista);
                            new Scene(vista); vista.applyCss(); vista.layout();
                            System.out.println("PASS carga: " + nombre);
                        } catch (Exception ex) { fallos.add("Carga " + nombre + ": " + causa(ex)); }
                    }
                }
                Usuario visor = new Usuario(); visor.setIdUsuario(2L); visor.setRol("visor");
                SessionManager.getInstance().login(visor);
                Map<String,String> controles = new LinkedHashMap<>();
                controles.put("Directorio.fxml", "tileDirectorio");
                controles.put("Empleados.fxml", "cmbFiltroJefe");
                controles.put("FormularioEmpleado.fxml", "cmbJefe");
                controles.put("FormularioUsuario.fxml", "cmbEmpleado");
                controles.put("Logros.fxml", "cmbEmpleado");
                controles.put("Roles.fxml", "tablaRoles");
                controles.put("Bandeja.fxml", "tablaSolicitudes");
                for (var entrada : controles.entrySet()) {
                    try {
                        FXMLLoader loader = loader(entrada.getKey()); loader.load();
                        Object control = loader.getNamespace().get(entrada.getValue());
                        int filas = control instanceof ComboBox<?> c ? c.getItems().size()
                                : control instanceof TableView<?> t ? t.getItems().size()
                                : control instanceof TilePane p ? p.getChildren().size() : 0;
                        if (filas > 0) fallos.add("Filtración al visor: " + entrada.getKey());
                    } catch (Exception ex) {
                        if (!(causa(ex) instanceof SecurityException)) fallos.add("Carga denegada falla con " + causa(ex));
                    }
                }
                fallos.forEach(f -> System.out.println("FAIL: " + f));
                verificar(fallos.isEmpty(), "Fallos de vistas: " + fallos.size());
                System.out.println("FxmlRecorridoTest: todas las vistas cargan; visor sin permisos no recibe datos.");
                return null;
            });
            Platform.runLater(tarea);
            try { tarea.get(45, TimeUnit.SECONDS); } finally { Platform.exit(); }
        }
    }
    private static FXMLLoader loader(String nombre) {
        return new FXMLLoader(FxmlRecorridoTest.class.getResource("/org/kinalrh/view/" + nombre));
    }
    private static Parent cargar(String nombre) throws Exception { return loader(nombre).load(); }
    private static void validarEnlaces(Path archivo,FXMLLoader cargador,Parent vista) throws Exception {
        var fabrica=javax.xml.parsers.DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        var documento=fabrica.newDocumentBuilder().parse(archivo.toFile());
        String controlador=documento.getDocumentElement().getAttributeNS("http://javafx.com/fxml/1","controller");
        verificar(controlador.isBlank() ? cargador.getController()==null
                : cargador.getController()!=null && cargador.getController().getClass().getName().equals(controlador),"fx:controller "+archivo);
        int ids=0,acciones=0;
        var nodos=documento.getElementsByTagName("*");
        Set<String> unicos=new HashSet<>();
        for(int i=0;i<nodos.getLength();i++) {
            var e=(org.w3c.dom.Element)nodos.item(i);
            String id=e.getAttributeNS("http://javafx.com/fxml/1","id");
            if(!id.isEmpty()) {
                ids++; verificar(unicos.add(id),"fx:id duplicado "+id);
                Object objeto=cargador.getNamespace().get(id);verificar(objeto!=null,"fx:id sin objeto "+id);
                if(cargador.getController()!=null) try {
                    var campo=cargador.getController().getClass().getDeclaredField(id);
                    campo.setAccessible(true);verificar(campo.get(cargador.getController())==objeto,"Inyección fx:id "+id);
                } catch(NoSuchFieldException sinCampo) { /* Un fx:id también puede usarse solo en el documento. */ }
            }
            String accion=e.getAttribute("onAction");
            if(!accion.isEmpty()) {
                acciones++; verificar(accion.startsWith("#"),"Acción de controlador "+accion);
                verificar(Arrays.stream(cargador.getController().getClass().getDeclaredMethods())
                        .anyMatch(m->m.getName().equals(accion.substring(1))),"onAction sin método "+accion);
            }
        }
        for(String css:vista.getStylesheets())try(var in=new java.net.URI(css).toURL().openStream()){verificar(in.read()!=-1,"CSS legible");}
        System.out.println("PASS enlaces: "+archivo.getFileName()+" ids="+ids+" acciones="+acciones+" CSS="+vista.getStylesheets().size());
    }
    private static Throwable causa(Throwable ex) {
        while (ex.getCause() != null) ex = ex.getCause();
        return ex;
    }
}
