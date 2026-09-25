package org.kinalrh.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;
import org.kinalrh.service.AutorizacionService;
import org.kinalrh.service.EmpleadoService;
import org.kinalrh.system.Main;

/**
 * Controlador del Dashboard principal.
 *
 * T1.17 – Muestra el panel y los botones de navegación según el permiso
 *          disponible del usuario autenticado.
 * T1.20 – Consulta a empleado ficticio protegida por AutorizacionService
 *          antes de ejecutar cualquier acceso a datos.
 */
public class DashboardController implements Initializable, BaseDashboardController {

    // ── Header ──────────────────────────────────────────────────────────────────
    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;

    // ── Navegación lateral (se muestra/oculta según permiso) ───────────────────
    @FXML private VBox  panelNavegacion;
    @FXML private Button btnEmpleados;
    @FXML private Button btnReportes;
    @FXML private Button btnAdmin;
    // btnCerrarSesion no necesita @FXML porque su onAction ya apunta al método directamente

    // ── Área de contenido: tabla de empleados ───────────────────────────────────
    @FXML private VBox  panelContenido;
    @FXML private TableView<Empleado>        tablaEmpleados;
    @FXML private TableColumn<Empleado, String> colCodigo;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colApellido;
    @FXML private TableColumn<Empleado, String> colPuesto;
    @FXML private TableColumn<Empleado, String> colDepartamento;

    @FXML private Label lblMensajePanel;

    // ── Servicios ───────────────────────────────────────────────────────────────
    private EmpleadoService   empleadoService;
    private AutorizacionService autorizacionService;
    private Usuario usuarioActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empleadoService     = new EmpleadoService();
        autorizacionService = new AutorizacionService();

        // Configurar columnas de la tabla
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));
        colDepartamento.setCellValueFactory(new PropertyValueFactory<>("departamento"));
    }

    // ── T1.17: Recibe al usuario y configura el panel según su rol ─────────────
    @Override
    public void iniciarUsuario(Usuario usuario) {
        this.usuarioActual = usuario;

        lblBienvenida.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());
        lblRol.setText("Rol: " + usuario.getRol().toUpperCase());

        configurarNavegacion(usuario.getRol());
    }

    /**
     * Muestra u oculta botones del menú lateral según el rol/permiso.
     * T1.17: Navegación según permiso disponible.
     */
    private void configurarNavegacion(String rol) {
        boolean puedeVerEmpleados = autorizacionService.tienePermiso(rol, "EMPLEADO_VER");
        boolean esAdmin           = autorizacionService.tienePermiso(rol, "ADMIN_TOTAL");

        // Botón Empleados: visible solo si tiene EMPLEADO_VER
        btnEmpleados.setVisible(puedeVerEmpleados);
        btnEmpleados.setManaged(puedeVerEmpleados);

        // Botón Admin: visible solo para administradores
        btnAdmin.setVisible(esAdmin);
        btnAdmin.setManaged(esAdmin);

        // Si puede ver empleados, cargar la lista directamente al iniciar
        if (puedeVerEmpleados) {
            cargarEmpleados();
        } else {
            lblMensajePanel.setText("Su rol no tiene módulos asignados en este panel.");
            lblMensajePanel.setVisible(true);
        }
    }

    // ── T1.20: Cargar empleados protegido por AutorizacionService ──────────────
    @FXML
    public void eventoVerEmpleados() {
        cargarEmpleados();
    }

    private void cargarEmpleados() {
        try {
            // AutorizacionService verifica EMPLEADO_VER antes de tocar la BD
            List<Empleado> lista = empleadoService.listarEmpleados(usuarioActual.getRol());
            tablaEmpleados.getItems().setAll(lista);
            tablaEmpleados.setVisible(true);
            lblMensajePanel.setVisible(false);
        } catch (SecurityException se) {
            // Acceso denegado — mensaje claro pero sin revelar datos del sistema
            lblMensajePanel.setText("No tiene permiso para ver esta información.");
            lblMensajePanel.setVisible(true);
            tablaEmpleados.setVisible(false);
        }
    }

    // ── Eventos de navegación ──────────────────────────────────────────────────
    @FXML
    public void eventoReportes() {
        lblMensajePanel.setText("Módulo de Reportes — próximamente disponible.");
        lblMensajePanel.setVisible(true);
        tablaEmpleados.setVisible(false);
    }

    @FXML
    public void eventoAdmin() {
        lblMensajePanel.setText("Módulo de Administración — próximamente disponible.");
        lblMensajePanel.setVisible(true);
        tablaEmpleados.setVisible(false);
    }

    @FXML
    public void eventoCerrarSesion() {
        try {
            Main.cambiarVista("/org/kinalrh/view/Login.fxml");
            Main.getEscenarioPrincipal().setTitle("Kinal RH – Iniciar Sesión");
            Main.getEscenarioPrincipal().setResizable(false);
            Main.getEscenarioPrincipal().centerOnScreen();
        } catch (Exception e) {
            System.err.println("[DashboardController] Error al cerrar sesión: " + e.getMessage());
        }
    }
}
