package org.kinalrh.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
    @FXML private TextField txtId;
    @FXML private TextField txtDpi;
    @FXML private TextField txtPrimerNombre;
    @FXML private TextField txtPrimerApellido;
    @FXML private ComboBox<Area> cmbArea;
    @FXML private ComboBox<Puesto> cmbPuesto;
    @FXML private ComboBox<EstadoEmpleado> cmbEstado;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

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
            txtPrimerNombre.setText(emp.getPrimerNombre());
            txtPrimerApellido.setText(emp.getPrimerApellido());
            
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
        if (txtDpi.getText().isEmpty() || txtPrimerNombre.getText().isEmpty() || txtPrimerApellido.getText().isEmpty() ||
            cmbArea.getValue() == null || cmbPuesto.getValue() == null || cmbEstado.getValue() == null) {
            lblError.setText("Error: DPI, Nombres y Catálogos son obligatorios.");
            lblError.setVisible(true);
            return;
        }

        empleado.setDpi(txtDpi.getText());
        empleado.setPrimerNombre(txtPrimerNombre.getText());
        empleado.setPrimerApellido(txtPrimerApellido.getText());
        
        empleado.setIdAreaPrincipal(cmbArea.getValue().getIdArea());
        empleado.setIdPuestoActual(cmbPuesto.getValue().getIdPuesto());
        empleado.setIdEstadoEmpleado(cmbEstado.getValue().getIdEstadoEmpleado());

        empleadoService.guardarEmpleado(empleado);
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    public void eventoCancelar(ActionEvent event) {
        if (stage != null) {
            stage.close();
        }
    }
}