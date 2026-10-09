package org.kinalrh.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.kinalrh.model.Usuario;
import org.kinalrh.service.AutorizacionService;
import org.kinalrh.system.Main;

public class DashboardController implements Initializable, BaseDashboardController {

    @FXML private BorderPane panelPrincipal;
    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;

    // Secciones del menú lateral
    @FXML private VBox seccionRH;
    @FXML private VBox seccionGerencia;
    @FXML private VBox seccionVisor;
    @FXML private VBox seccionAdmin;

    private AutorizacionService autorizacionService;
    private Usuario usuarioActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        autorizacionService = new AutorizacionService();
    }

    @Override
    public void iniciarUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        lblBienvenida.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());
        lblRol.setText("Rol: " + usuario.getRol().toUpperCase());

        configurarNavegacionSegunRol(usuario.getRol());
    }

    private void configurarNavegacionSegunRol(String rol) {
        String r = rol.toUpperCase();
        
        boolean esAdmin = r.equals("ADMIN");
        boolean esRh = r.equals("ENCARGADO") || esAdmin;
        boolean esGerente = r.equals("GERENTEAREA") || r.equals("GERENTEGENERAL") || r.equals("JEFE") || esAdmin;
        boolean esVisor = r.equals("VISOR") || esAdmin || esRh || esGerente;

        seccionAdmin.setVisible(esAdmin);
        seccionAdmin.setManaged(esAdmin);

        seccionRH.setVisible(esRh);
        seccionRH.setManaged(esRh);

        seccionGerencia.setVisible(esGerente);
        seccionGerencia.setManaged(esGerente);

        seccionVisor.setVisible(esVisor);
        seccionVisor.setManaged(esVisor);
    }

    // --- CARGADOR DINÁMICO DE VISTAS ---
    private void cargarVistaCentral(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/kinalrh/view/" + fxml));
            Node vista = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof UsuarioController && usuarioActual != null) {
                ((UsuarioController) ctrl).setRolActual(usuarioActual.getRol());
            }
            panelPrincipal.setCenter(vista);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista: " + fxml);
        }
    }

    // --- EVENTOS DEL MENÚ (NAVEGACIÓN) ---
    @FXML public void abrirColaboradores() { cargarVistaCentral("Empleados.fxml"); }
    @FXML public void abrirCatalogos() { cargarVistaCentral("Catalogos.fxml"); } 
    @FXML public void abrirImportacion() { cargarVistaCentral("Importacion.fxml"); }
    @FXML public void abrirReportes() { cargarVistaCentral("Reportes.fxml"); } 
    @FXML public void abrirBandeja() { cargarVistaCentral("Bandeja.fxml"); }
    @FXML public void abrirMiEquipo() { cargarVistaCentral("Empleados.fxml"); } 
    @FXML public void abrirDirectorio() { cargarVistaCentral("Directorio.fxml"); }
    @FXML public void abrirLogros() { cargarVistaCentral("Logros.fxml"); } 
    @FXML public void abrirUsuarios() { cargarVistaCentral("Usuarios.fxml"); } 
    @FXML public void abrirRoles() { cargarVistaCentral("Roles.fxml"); } 
    @FXML public void abrirAuditoria() { cargarVistaCentral("Auditoria.fxml"); } 

    @FXML
    public void eventoCerrarSesion() {
        try {
            org.kinalrh.service.SesionService.getInstance().cerrarSesion();
            org.kinalrh.system.Main.cambiarVista("/org/kinalrh/view/Login.fxml");
            org.kinalrh.system.Main.getEscenarioPrincipal().setTitle("Kinal RH - Iniciar Sesión");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}