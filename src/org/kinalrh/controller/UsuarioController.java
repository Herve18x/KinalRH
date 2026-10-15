package org.kinalrh.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.model.ColaboradorSinRol;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Usuario;
import org.kinalrh.service.AuditoriaService;
import org.kinalrh.service.UsuarioService;

public class UsuarioController implements Initializable {

    // --- Controles de Usuarios.fxml (Lista) ---
    @FXML private Label lblMensajePermiso;
    @FXML private Button btnNuevo;
    @FXML private Button btnRefrescar;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colNombreVisible;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, Boolean> colEstado;

    // --- Banner de Alerta Formal para Colaboradores Sin Rol / Acceso ---
    @FXML private VBox bannerAlertaColaboradores;
    @FXML private Label lblTituloAlerta;
    @FXML private Label lblDetalleAlerta;
    @FXML private ComboBox<ColaboradorSinRol> cmbColaboradoresPendientes;
    @FXML private Button btnAsignarAcceso;

    // --- Controles de FormularioUsuario.fxml ---
    @FXML private Label lblTituloFormulario;
    @FXML private Label lblError;
    @FXML private ComboBox<Empleado> cmbEmpleado;
    @FXML private TextField txtUsername;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtPassword;
    @FXML private ComboBox<String> cmbRol;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private UsuarioService usuarioService;
    private AuditoriaService auditoriaService;
    
    // Rol del usuario logueado en la sesión
    private String rolActual = "ADMIN";
    
    // Usuario siendo editado en el formulario
    private Usuario usuarioEdicion;
    private Long idEmpleadoPreseleccionado;
    private Stage stageFormulario;
    private UsuarioController listController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usuarioService = new UsuarioService();
        auditoriaService = new AuditoriaService();

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
    // LOGICA DE LA VISTA DE LISTA (Usuarios.fxml)
    // ==========================================================
    private void configurarLista() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombreVisible.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Personalización de celda de Rol con distintivo formal
        colRol.setCellFactory(col -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String rol, boolean empty) {
                super.updateItem(rol, empty);
                if (empty || rol == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else if ("SIN_ROL".equalsIgnoreCase(rol) || rol.trim().isEmpty()) {
                    Label badge = new Label("⚠️ Colaborador sin asignar rol");
                    badge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                } else {
                    Label badge = new Label(rol.toUpperCase());
                    badge.setStyle("-fx-background-color: #E0F2FE; -fx-text-fill: #0369A1; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Personalización de celda de Estado
        colEstado.setCellFactory(col -> new TableCell<Usuario, Boolean>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else if (activo) {
                    Label badge = new Label("ACTIVO");
                    badge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                } else {
                    Label badge = new Label("INACTIVO");
                    badge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        cargarDatosLista();
    }

    private void cargarDatosLista() {
        try {
            List<Usuario> lista = usuarioService.listarUsuarios(rolActual);
            tablaUsuarios.getItems().setAll(lista);
            lblMensajePermiso.setVisible(false);
            tablaUsuarios.setVisible(true);
            btnNuevo.setDisable(false);

            // Consultar colaboradores que no tienen rol ni usuario asignado
            verificarColaboradoresSinRol();

        } catch (SecurityException e) {
            lblMensajePermiso.setText(e.getMessage());
            lblMensajePermiso.setVisible(true);
            tablaUsuarios.setVisible(false);
            btnNuevo.setDisable(true);
            if (bannerAlertaColaboradores != null) {
                bannerAlertaColaboradores.setVisible(false);
                bannerAlertaColaboradores.setManaged(false);
            }
        }
    }

    private void verificarColaboradoresSinRol() {
        if (bannerAlertaColaboradores == null) return;
        try {
            List<ColaboradorSinRol> pendientes = usuarioService.listarColaboradoresSinRol(rolActual);
            if (pendientes != null && !pendientes.isEmpty()) {
                bannerAlertaColaboradores.setVisible(true);
                bannerAlertaColaboradores.setManaged(true);
                cmbColaboradoresPendientes.getItems().setAll(pendientes);
                cmbColaboradoresPendientes.getSelectionModel().selectFirst();
                
                int count = pendientes.size();
                lblTituloAlerta.setText("Aviso Formal: " + count + (count == 1 
                        ? " colaborador sin asignar rol o credenciales de acceso" 
                        : " colaboradores sin asignar rol o credenciales de acceso"));
                lblDetalleAlerta.setText("Se detectaron colaboradores registrados en la institución que aún no cuentan con rol ni acceso al sistema.");
            } else {
                bannerAlertaColaboradores.setVisible(false);
                bannerAlertaColaboradores.setManaged(false);
            }
        } catch (Exception e) {
            System.err.println("Error al verificar colaboradores sin rol: " + e.getMessage());
            bannerAlertaColaboradores.setVisible(false);
            bannerAlertaColaboradores.setManaged(false);
        }
    }

    @FXML
    public void eventoAsignarAccesoColaborador(ActionEvent event) {
        if (cmbColaboradoresPendientes == null) return;
        ColaboradorSinRol seleccionado = cmbColaboradoresPendientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Atención");
            alerta.setHeaderText("Selección requerida");
            alerta.setContentText("Por favor seleccione un colaborador pendiente de la lista.");
            alerta.showAndWait();
            return;
        }
        abrirFormularioParaColaborador(seleccionado);
    }

    @FXML
    public void eventoRefrescar(ActionEvent event) {
        cargarDatosLista();
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
            controllerForm.listController = this;
            
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

    private void abrirFormularioParaColaborador(ColaboradorSinRol colab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/kinalrh/view/FormularioUsuario.fxml"));
            Parent root = loader.load();
            
            UsuarioController controllerForm = loader.getController();
            controllerForm.rolActual = this.rolActual;
            controllerForm.listController = this;
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            try { stage.getIcons().add(new Image(getClass().getResourceAsStream("/org/kinalrh/image/icon.png"))); } catch(Exception e) {}
            stage.setTitle("Asignar Credenciales y Rol a Colaborador");
            stage.setScene(new Scene(root));
            
            controllerForm.setStageFormulario(stage);
            controllerForm.prepararParaColaborador(colab);
            
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refrescarTabla() {
        cargarDatosLista();
    }

    // ==========================================================
    // LOGICA DEL FORMULARIO (FormularioUsuario.fxml)
    // ==========================================================
    private void configurarFormulario() {
        lblError.setVisible(false);

        // Cargar opciones de roles disponibles en el sistema
        cmbRol.getItems().setAll("ADMIN", "ENCARGADO", "GERENTEAREA", "GERENTEGENERAL", "JEFE", "VISOR");
        cmbRol.setValue("ENCARGADO");

        // Cargar colaboradores en el ComboBox
        try {
            List<Empleado> empleados = new EmpleadoDAOImpl().listarTodos();
            cmbEmpleado.getItems().setAll(empleados);
            cmbEmpleado.setConverter(new StringConverter<Empleado>() {
                @Override
                public String toString(Empleado emp) {
                    if (emp == null) return "";
                    String dpiStr = (emp.getDpi() != null && !emp.getDpi().trim().isEmpty()) ? " [DPI: " + emp.getDpi() + "]" : "";
                    return (emp.getNombreCompleto() != null ? emp.getNombreCompleto() : "Empleado #" + emp.getIdEmpleado()) + dpiStr;
                }
                @Override
                public Empleado fromString(String string) {
                    return null;
                }
            });

            // Autocompletar datos cuando se selecciona un colaborador
            cmbEmpleado.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty())) {
                    txtNombre.setText(newVal.getPrimerNombre() != null ? newVal.getPrimerNombre() : newVal.getNombreCompleto());
                    txtApellido.setText(newVal.getPrimerApellido() != null ? newVal.getPrimerApellido() : "");
                    if (txtUsername.getText() == null || txtUsername.getText().trim().isEmpty()) {
                        String userSugg = (newVal.getPrimerNombre() != null ? newVal.getPrimerNombre().toLowerCase() : "user") + "." +
                                          (newVal.getPrimerApellido() != null ? newVal.getPrimerApellido().toLowerCase() : "kinal");
                        txtUsername.setText(userSugg.replaceAll("[^a-z0-9.]", ""));
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error al cargar colaboradores en formulario: " + e.getMessage());
        }
    }

    public void setStageFormulario(Stage stage) {
        this.stageFormulario = stage;
    }

    public void cargarDatosFormulario(Usuario usuario) {
        this.usuarioEdicion = usuario;
        lblError.setVisible(false);
        
        if (usuario != null) {
            lblTituloFormulario.setText("Edición de Usuario (ID: " + usuario.getId() + ")");
            txtUsername.setText(usuario.getUsername());
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            chkActivo.setSelected(usuario.isActivo());
            
            // Seleccionar rol
            String r = (usuario.getRol() != null && !usuario.getRol().equals("SIN_ROL")) 
                    ? usuario.getRol().toUpperCase() : "ENCARGADO";
            cmbRol.setValue(r);

            // Seleccionar colaborador vinculado
            if (usuario.getIdEmpleado() != null) {
                for (Empleado e : cmbEmpleado.getItems()) {
                    if (e.getIdEmpleado() != null && e.getIdEmpleado().equals(usuario.getIdEmpleado())) {
                        cmbEmpleado.setValue(e);
                        break;
                    }
                }
            }
            
            // Nunca rellenar un campo de contraseña con el hash
            txtPassword.setText(usuario.getPasswordHash()); 
            txtPassword.setPromptText("Dejar vacío para no cambiar");
        } else {
            lblTituloFormulario.setText("Nuevo Usuario");
            chkActivo.setSelected(true);
            cmbRol.setValue("ENCARGADO");
        }
    }

    public void prepararParaColaborador(ColaboradorSinRol colab) {
        lblError.setVisible(false);
        this.idEmpleadoPreseleccionado = colab.getIdEmpleado();

        // Si el colaborador ya tiene un idUsuario registrado sin rol
        if (colab.getIdUsuario() != null) {
            Optional<Usuario> optUser = usuarioService.buscarPorId(colab.getIdUsuario(), rolActual);
            if (optUser.isPresent()) {
                cargarDatosFormulario(optUser.get());
                lblTituloFormulario.setText("Asignar Rol a Colaborador: " + colab.getNombreCompleto());
                cmbRol.setValue("ENCARGADO");
                return;
            }
        }

        // Si es un colaborador nuevo sin cuenta de acceso
        lblTituloFormulario.setText("Asignar Rol y Acceso: " + colab.getNombreCompleto());
        chkActivo.setSelected(true);
        cmbRol.setValue("ENCARGADO");

        if (colab.getNombreCompleto() != null) {
            txtNombre.setText(colab.getNombreCompleto());
            String sugerido = colab.getNombreCompleto().toLowerCase().replaceAll("\\s+", ".").replaceAll("[^a-z0-9.]", "");
            txtUsername.setText(sugerido);
        }

        // Seleccionar colaborador en el ComboBox
        for (Empleado emp : cmbEmpleado.getItems()) {
            if (emp.getIdEmpleado() != null && emp.getIdEmpleado().equals(colab.getIdEmpleado())) {
                cmbEmpleado.setValue(emp);
                break;
            }
        }
    }

    @FXML
    public void eventoGuardar(ActionEvent event) {
        // Validación visual
        if (txtUsername.getText() == null || txtUsername.getText().trim().isEmpty() ||
            txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            
            lblError.setText("Error: Los campos Usuario y Nombre son obligatorios.");
            lblError.setVisible(true);
            return;
        }

        // Si es nuevo, la contraseña es obligatoria
        if (usuarioEdicion == null && (txtPassword.getText() == null || txtPassword.getText().trim().isEmpty())) {
            lblError.setText("Error: La contraseña es obligatoria para usuarios nuevos.");
            lblError.setVisible(true);
            return;
        }

        String rolSeleccionado = cmbRol.getValue() != null ? cmbRol.getValue().trim() : "ENCARGADO";
        Empleado empleadoSeleccionado = cmbEmpleado.getValue();
        Long idEmp = (empleadoSeleccionado != null) ? empleadoSeleccionado.getIdEmpleado() : idEmpleadoPreseleccionado;

        lblError.setVisible(false);

        try {
            if (usuarioEdicion == null) {
                // Modo crear usuario con rol asignado y empleado vinculado
                Usuario nuevo = new Usuario();
                nuevo.setUsername(txtUsername.getText().trim());
                nuevo.setPasswordHash(txtPassword.getText().trim());
                nuevo.setNombre(txtNombre.getText().trim());
                if (txtApellido.getText() != null) nuevo.setApellido(txtApellido.getText().trim());
                nuevo.setRol(rolSeleccionado);
                nuevo.setActivo(chkActivo.isSelected());
                nuevo.setIdEmpleado(idEmp);

                if (empleadoSeleccionado != null && empleadoSeleccionado.getCorreoPersonal() != null) {
                    nuevo.setCorreo(empleadoSeleccionado.getCorreoPersonal());
                }

                usuarioService.guardarUsuario(nuevo, rolActual);

                // Registrar evento en auditoría
                auditoriaService.registrarEvento("CREAR_USUARIO_ROL", "usuario", nuevo.getIdUsuario(), "rol", null, rolSeleccionado);

            } else {
                // Modo editar usuario
                usuarioEdicion.setUsername(txtUsername.getText().trim());
                usuarioEdicion.setNombre(txtNombre.getText().trim());
                usuarioEdicion.setApellido(txtApellido.getText() == null ? "" : txtApellido.getText().trim());
                usuarioEdicion.setRol(rolSeleccionado);
                usuarioEdicion.setActivo(chkActivo.isSelected());
                if (idEmp != null) {
                    usuarioEdicion.setIdEmpleado(idEmp);
                }
                
                // Si escribió algo en contraseña, actualizarla. Si no, dejarla como está.
                if (txtPassword.getText() != null && !txtPassword.getText().trim().isEmpty()) {
                    usuarioEdicion.setPasswordHash(txtPassword.getText().trim());
                } else {
                    usuarioEdicion.setPasswordHash(null);
                }
                
                usuarioService.actualizarUsuario(usuarioEdicion, rolActual);

                // Registrar evento en auditoría
                auditoriaService.registrarEvento("ACTUALIZAR_USUARIO_ROL", "usuario", usuarioEdicion.getIdUsuario(), "rol", null, rolSeleccionado);
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
