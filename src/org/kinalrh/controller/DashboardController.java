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
        // En una app real, esto se evalúa con autorizacionService.tienePermiso(...)
        // Por ahora lo hacemos genérico para que tu compañero tenga la base
        String r = rol.toUpperCase();
        
        boolean esAdmin = r.equals("ADMIN");
        boolean esRh = r.equals("RRHH") || esAdmin;
        boolean esGerente = r.equals("GERENTE") || r.equals("SUPERVISOR") || esAdmin;
        boolean esVisor = r.equals("VISOR") || esAdmin || esRh;

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
            panelPrincipal.setCenter(vista);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista: " + fxml);
        }
    }

    // --- EVENTOS DEL MENÚ (NAVEGACIÓN) ---
    @FXML public void abrirColaboradores() { cargarVistaCentral("Colaboradores.fxml"); }
    @FXML public void abrirCatalogos() { cargarVistaCentral("Catalogos.fxml"); } // Vista pendiente
    @FXML public void abrirImportacion() { cargarVistaCentral("Importacion.fxml"); }
    @FXML public void abrirReportes() { cargarVistaCentral("Reportes.fxml"); } // Vista pendiente
    @FXML public void abrirBandeja() { cargarVistaCentral("Bandeja.fxml"); }
    @FXML public void abrirMiEquipo() { cargarVistaCentral("Colaboradores.fxml"); } // Reutiliza colaboradores pero filtrado (tu compa hará el filtro)
    @FXML public void abrirDirectorio() { cargarVistaCentral("Directorio.fxml"); }
    @FXML public void abrirUsuarios() { cargarVistaCentral("Usuarios.fxml"); } // Esta ya la programamos
    @FXML public void abrirRoles() { cargarVistaCentral("Roles.fxml"); } // Vista pendiente
    @FXML public void abrirAuditoria() { cargarVistaCentral("Auditoria.fxml"); } // Vista pendiente

    @FXML
    public void eventoCerrarSesion() {
        try {
            Main.cambiarVista("/org/kinalrh/view/Login.fxml");
            Main.getEscenarioPrincipal().setTitle("Kinal RH – Iniciar Sesión");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
