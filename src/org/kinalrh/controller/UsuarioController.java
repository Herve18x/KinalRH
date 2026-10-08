package org.kinalrh.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import org.kinalrh.util.SecurityUtil;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.kinalrh.model.Usuario;
import org.kinalrh.service.UsuarioService;
import org.kinalrh.util.ComponenteUtil;

public class UsuarioController implements Initializable {

    // --- Controles de Usuarios.fxml (Lista) ---
    @FXML private Label lblMensajePermiso;
    @FXML private Button btnNuevo;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombreVisible;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, Boolean> colEstado;

    // --- Controles de FormularioUsuario.fxml ---
    @FXML private Label lblTituloFormulario;
    @FXML private Label lblError;
    @FXML private TextField txtUsername;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnDesbloquearPass;
    @FXML private ComboBox<String> cmbRol;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private UsuarioService usuarioService;
    
    // Usuario logueado en la aplicaciÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³n (simulado o inyectado, para validar permisos)
    private String rolActual = "ADMIN"; // Asumiremos ADMIN para pruebas, en prod se inyecta
    
    // Usuario siendo editado en el formulario
    private Usuario usuarioEdicion;
    private Stage stageFormulario;
    private UsuarioController listController; // Referencia al controlador de la lista si somos el form

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usuarioService = new UsuarioService();

        // Identificar si estamos inicializando la vista Lista o el Formulario
        if (tablaUsuarios != null) {
            configurarLista();
        } else if (txtUsername != null) {
            configurarFormulario();
        }
    }

    public void setRolActual(String rolActual) {
        this.rolActual = rolActual;
        if (tablaUsuarios != null) {
            cargarDatosLista();
        }
    }

    // ==========================================================
    // LÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œGICA DE LA VISTA DE LISTA (Usuarios.fxml)
    // ==========================================================
    private void configurarLista() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        // El nombre visible puede componerse, en este caso mostramos el nombre
        colNombreVisible.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));

        cargarDatosLista();
    }

    private void cargarDatosLista() {
        try {
            List<Usuario> lista = usuarioService.listarUsuarios(rolActual);
            tablaUsuarios.getItems().setAll(lista);
            lblMensajePermiso.setVisible(false);
            tablaUsuarios.setVisible(true);
            btnNuevo.setDisable(false);
        } catch (SecurityException e) {
            lblMensajePermiso.setText(e.getMessage());
            lblMensajePermiso.setVisible(true);
            tablaUsuarios.setVisible(false);
            btnNuevo.setDisable(true);
        }
    }

    @FXML
    public void eventoNuevo(ActionEvent event) {
        abrirFormulario(null);
    }

    @FXML
    public void eventoSeleccionarFicha(MouseEvent event) {
        if (event.getClickCount() == 2 && tablaUsuarios.getSelectionModel().getSelectedItem() != null) {
            Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
            abrirFormulario(seleccionado);
        }
    }

    private void abrirFormulario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/kinalrh/view/FormularioUsuario.fxml"));
            Parent root = loader.load();
            
            UsuarioController controllerForm = loader.getController();
            controllerForm.rolActual = this.rolActual;
            controllerForm.listController = this; // Pasar referencia para refrescar la tabla
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            try { stage.getIcons().add(new Image(getClass().getResourceAsStream("/org/kinalrh/image/icon.png"))); } catch(Exception e) {}
            stage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");
            stage.setScene(new Scene(root));
            
            controllerForm.setStageFormulario(stage);
            controllerForm.cargarDatosFormulario(usuario);
            
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refrescarTabla() {
        cargarDatosLista();
    }

    // ==========================================================
    // LÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œGICA DEL FORMULARIO (FormularioUsuario.fxml)
    // ==========================================================
    private void configurarFormulario() {
        if (txtPassword != null && txtPasswordVisible != null) {
            txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
        }
        ComponenteUtil.configurarSelectorRol(cmbRol);
        lblError.setVisible(false);
    }

    public void setStageFormulario(Stage stage) {
        this.stageFormulario = stage;
    }

    public void cargarDatosFormulario(Usuario usuario) {
        this.usuarioEdicion = usuario;
        lblError.setVisible(false);
        
        if (usuario != null) {
            lblTituloFormulario.setText("EdiciÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³n de Usuario (ID: " + usuario.getId() + ")");
            txtUsername.setText(usuario.getUsername());
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            cmbRol.setValue(usuario.getRol());
            chkActivo.setSelected(usuario.isActivo());
            
            // CRÃƒÆ’Ã†â€™Ãƒâ€šÃ‚ÂTICO T2.02: Nunca rellenar un campo de contraseÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â±a con el hash
            txtPassword.setText(""); 
            txtPassword.setPromptText("Dejar vacÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­o para no cambiar");
        } else {
            lblTituloFormulario.setText("Nuevo Usuario");
            chkActivo.setSelected(true); // Activo por defecto
        }
    }

    @FXML
    public void eventoDesbloquearPass(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Seguridad");
        dialog.setHeaderText("Autorizacion Requerida");
        dialog.setContentText("Ingrese contrasena del administrador:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String passAdmin = result.get();
            // Verifica contra la clave del admin (mock seguro por ahora)
            if (SecurityUtil.hashSHA256(passAdmin).equals("8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918")) {
                
                
                // Alternar vista de contrasena
                if (txtPassword.isVisible()) {
                    txtPassword.setVisible(false);
                    txtPassword.setManaged(false);
                    txtPasswordVisible.setVisible(true);
                    txtPasswordVisible.setManaged(true);
                    btnDesbloquearPass.setText("Ocultar Pass");
                } else {
                    txtPassword.setVisible(true);
                    txtPassword.setManaged(true);
                    txtPasswordVisible.setVisible(false);
                    txtPasswordVisible.setManaged(false);
                    btnDesbloquearPass.setText("Ver Pass");
                }
                lblError.setVisible(false);
            } else {
                lblError.setText("Error: Contrasena de admin incorrecta.");
                lblError.setVisible(true);
            }
        }
    }

    @FXML
    public void eventoGuardar(ActionEvent event) {
        // ValidaciÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³n visual clara (T2.13)
        if (txtUsername.getText() == null || txtUsername.getText().trim().isEmpty() ||
            txtNombre.getText() == null || txtNombre.getText().trim().isEmpty() ||
            cmbRol.getValue() == null) {
            
            lblError.setText("Error: Los campos Usuario, Nombre y Rol son obligatorios.");
            lblError.setVisible(true);
            return;
        }

        // Si es nuevo, la contraseÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â±a es obligatoria
        if (usuarioEdicion == null && (txtPassword.getText() == null || txtPassword.getText().trim().isEmpty())) {
            lblError.setText("Error: La contraseÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â±a es obligatoria para usuarios nuevos.");
            lblError.setVisible(true);
            return;
        }

        lblError.setVisible(false);

        try {
            if (usuarioEdicion == null) {
                // Modo crear
                Usuario nuevo = new Usuario(
                    null, 
                    txtUsername.getText().trim(),
                    txtNombre.getText().trim(),
                    txtApellido.getText() == null ? "" : txtApellido.getText().trim(),
                    txtPassword.getText().trim(), // El Service lo hasearÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡
                    cmbRol.getValue(),
                    chkActivo.isSelected()
                );
                usuarioService.guardarUsuario(nuevo, rolActual);
            } else {
                // Modo editar
                usuarioEdicion.setUsername(txtUsername.getText().trim());
                usuarioEdicion.setNombre(txtNombre.getText().trim());
                usuarioEdicion.setApellido(txtApellido.getText() == null ? "" : txtApellido.getText().trim());
                usuarioEdicion.setRol(cmbRol.getValue());
                usuarioEdicion.setActivo(chkActivo.isSelected());
                
                // Si escribiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ algo en contraseÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â±a, actualizarla. Si no, dejarla como estÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡.
                if (txtPassword.getText() != null && !txtPassword.getText().trim().isEmpty()) {
                    usuarioEdicion.setPasswordHash(txtPassword.getText().trim());
                } else {
                    usuarioEdicion.setPasswordHash(null); // SeÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â±al para el service de no actualizar
                }
                
                usuarioService.actualizarUsuario(usuarioEdicion, rolActual);
            }
            
            if (listController != null) {
                listController.refrescarTabla();
            }
            if (stageFormulario != null) {
                stageFormulario.close();
            }
            
        } catch (SecurityException e) {
            lblError.setText("Acceso Denegado: " + e.getMessage());
            lblError.setVisible(true);
        } catch (Exception e) {
            lblError.setText("Error al guardar: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    @FXML
    public void eventoCancelar(ActionEvent event) {
        if (stageFormulario != null) {
            stageFormulario.close();
        }
    }
}


