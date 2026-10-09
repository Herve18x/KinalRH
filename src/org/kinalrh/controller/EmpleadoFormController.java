package org.kinalrh.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Area;
import org.kinalrh.model.Puesto;
import org.kinalrh.model.EstadoEmpleado;
import org.kinalrh.dao.impl.AreaDAOImpl;
import org.kinalrh.dao.impl.PuestoDAOImpl;
import org.kinalrh.dao.impl.EstadoEmpleadoDAOImpl;
import org.kinalrh.service.EmpleadoService;

public class EmpleadoFormController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private Label lblError;
    
    // IdentificaciÃ³n
    @FXML private TextField txtId;
    @FXML private TextField txtDpi;
    @FXML private TextField txtNit;
    @FXML private TextField txtCodigo;
    
    // Nombres y Apellidos
    @FXML private TextField txtPrimerNombre;
    @FXML private TextField txtSegundoNombre;
    @FXML private TextField txtTercerNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private TextField txtSegundoApellido;
    @FXML private TextField txtApellidoCasada;
    
    // Personales
    @FXML private DatePicker dpFechaNac;
    @FXML private ComboBox<String> cmbEstadoCivil;
    @FXML private TextField txtTelefonoMovil;
    @FXML private TextField txtTelefonoFijo;
    @FXML private TextField txtCorreo;
    
    // DirecciÃ³n
    @FXML private TextField txtDireccion;
    @FXML private TextField txtZona;
    @FXML private TextField txtMunicipio;
    @FXML private TextField txtDepartamento;
    
    // Institucionales
    @FXML private DatePicker dpFechaIngreso;
    @FXML private ComboBox<Area> cmbArea;
    @FXML private ComboBox<Puesto> cmbPuesto;
    @FXML private ComboBox<EstadoEmpleado> cmbEstado;
    
    private Empleado empleado;
    private Stage stage;
    private EmpleadoService empleadoService;
    
    private AreaDAOImpl areaDAO = new AreaDAOImpl();
    private PuestoDAOImpl puestoDAO = new PuestoDAOImpl();
    private EstadoEmpleadoDAOImpl estadoDAO = new EstadoEmpleadoDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empleadoService = new EmpleadoService();
        cargarCatalogos();
    }
    
    private void cargarCatalogos() {
        cmbArea.getItems().setAll(areaDAO.listarActivas());
        cmbPuesto.getItems().setAll(puestoDAO.listarActivos());
        cmbEstado.getItems().setAll(estadoDAO.listarTodos());
        cmbEstadoCivil.getItems().addAll("Soltero(a)", "Casado(a)", "Divorciado(a)", "Viudo(a)", "Unido(a)");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setEmpleado(Empleado emp) {
        this.empleado = emp;
        if (emp != null && emp.getIdEmpleado() != null && emp.getIdEmpleado() > 0) {
            lblTitulo.setText("Editar Empleado");
            txtId.setText(String.valueOf(emp.getIdEmpleado()));
            txtDpi.setText(emp.getDpi());
            txtNit.setText(emp.getNit());
            txtCodigo.setText(emp.getCodigoEmpleado());
            
            txtPrimerNombre.setText(emp.getPrimerNombre());
            txtSegundoNombre.setText(emp.getSegundoNombre());
            txtTercerNombre.setText(emp.getTercerNombre());
            txtPrimerApellido.setText(emp.getPrimerApellido());
            txtSegundoApellido.setText(emp.getSegundoApellido());
            txtApellidoCasada.setText(emp.getApellidoCasada());
            
            dpFechaNac.setValue(emp.getFechaNacimiento());
            cmbEstadoCivil.setValue(emp.getEstadoCivil());
            txtTelefonoMovil.setText(emp.getTelefonoMovil());
            txtTelefonoFijo.setText(emp.getTelefonoFijo());
            txtCorreo.setText(emp.getCorreoPersonal());
            
            txtDireccion.setText(emp.getDireccion());
            txtZona.setText(emp.getZona());
            txtMunicipio.setText(emp.getMunicipio());
            txtDepartamento.setText(emp.getDepartamento());
            
            dpFechaIngreso.setValue(emp.getFechaIngresoKinal());
            
            seleccionarArea(emp.getIdAreaPrincipal());
            seleccionarPuesto(emp.getIdPuestoActual());
            seleccionarEstado(emp.getIdEstadoEmpleado());
        } else {
            lblTitulo.setText("Nuevo Empleado");
            this.empleado = new Empleado();
        }
    }
    
    private void seleccionarArea(Long id) {
        if(id == null) return;
        for(Area a : cmbArea.getItems()) {
            if(a.getIdArea().equals(id)) {
                cmbArea.getSelectionModel().select(a);
                break;
            }
        }
    }
    
    private void seleccionarPuesto(Long id) {
        if(id == null) return;
        for(Puesto p : cmbPuesto.getItems()) {
            if(p.getIdPuesto().equals(id)) {
                cmbPuesto.getSelectionModel().select(p);
                break;
            }
        }
    }
    
    private void seleccionarEstado(Long id) {
        if(id == null) return;
        for(EstadoEmpleado e : cmbEstado.getItems()) {
            if(e.getIdEstadoEmpleado().equals(id)) {
                cmbEstado.getSelectionModel().select(e);
                break;
            }
        }
    }

    @FXML
    public void eventoGuardar(ActionEvent event) {
        if (!validarEntradas()) {
            return;
        }

        empleado.setDpi(txtDpi.getText().trim());
        empleado.setNit(txtNit.getText().trim());
        empleado.setCodigoEmpleado(txtCodigo.getText().trim());
        
        empleado.setPrimerNombre(txtPrimerNombre.getText().trim());
        empleado.setSegundoNombre(txtSegundoNombre.getText().trim());
        empleado.setTercerNombre(txtTercerNombre.getText().trim());
        empleado.setPrimerApellido(txtPrimerApellido.getText().trim());
        empleado.setSegundoApellido(txtSegundoApellido.getText().trim());
        empleado.setApellidoCasada(txtApellidoCasada.getText().trim());
        
        empleado.setFechaNacimiento(dpFechaNac.getValue());
        empleado.setEstadoCivil(cmbEstadoCivil.getValue());
        empleado.setTelefonoMovil(txtTelefonoMovil.getText().trim());
        empleado.setTelefonoFijo(txtTelefonoFijo.getText().trim());
        empleado.setCorreoPersonal(txtCorreo.getText().trim());
        
        empleado.setDireccion(txtDireccion.getText().trim());
        empleado.setZona(txtZona.getText().trim());
        empleado.setMunicipio(txtMunicipio.getText().trim());
        empleado.setDepartamento(txtDepartamento.getText().trim());
        
        empleado.setFechaIngresoKinal(dpFechaIngreso.getValue());
        
        empleado.setIdAreaPrincipal(cmbArea.getValue() != null ? cmbArea.getValue().getIdArea() : null);
        empleado.setIdPuestoActual(cmbPuesto.getValue() != null ? cmbPuesto.getValue().getIdPuesto() : null);
        empleado.setIdEstadoEmpleado(cmbEstado.getValue().getIdEstadoEmpleado());

        empleadoService.guardarEmpleado(empleado);
        if (stage != null) {
            stage.close();
        }
    }

    private boolean validarEntradas() {
        String dpi = txtDpi.getText() != null ? txtDpi.getText().trim() : "";
        String pNombre = txtPrimerNombre.getText() != null ? txtPrimerNombre.getText().trim() : "";
        String pApellido = txtPrimerApellido.getText() != null ? txtPrimerApellido.getText().trim() : "";
        String correo = txtCorreo.getText() != null ? txtCorreo.getText().trim() : "";

        // 1. Campos obligatorios
        if (dpi.isEmpty() || pNombre.isEmpty() || pApellido.isEmpty() || cmbEstado.getValue() == null) {
            lblError.setText("Error: Los campos DPI, Primer Nombre, Primer Apellido y Estado son obligatorios.");
            lblError.setVisible(true);
            return false;
        }

        // 2. ValidaciÃ³n de formato de DPI (13 dÃ­gitos)
        if (!Pattern.matches("^\\d{13}$", dpi)) {
            lblError.setText("Error: El DPI debe contener exactamente 13 dÃ­gitos numÃ©ricos.");
            lblError.setVisible(true);
            return false;
        }

        // 3. ValidaciÃ³n de formato de correo (bÃ¡sico)
        if (!correo.isEmpty() && !Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", correo)) {
            lblError.setText("Error: El formato del correo personal no es vÃ¡lido.");
            lblError.setVisible(true);
            return false;
        }

        lblError.setVisible(false);
        return true;
    }

    @FXML
    public void eventoCancelar(ActionEvent event) {
        if (stage != null) {
            stage.close();
        }
    }
}