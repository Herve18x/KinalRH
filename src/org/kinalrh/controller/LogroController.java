package org.kinalrh.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.model.Empleado;
import org.kinalrh.model.Logro;
import org.kinalrh.model.LogroEmpleadoDTO;
import org.kinalrh.service.LogroService;
import org.kinalrh.service.SesionService;

public class LogroController implements Initializable {

    @FXML private TableView<LogroEmpleadoDTO> tablaLogrosDTO;
    @FXML private TableColumn<LogroEmpleadoDTO, String> colEmpleado;
    @FXML private TableColumn<LogroEmpleadoDTO, String> colNombre;
    @FXML private TableColumn<LogroEmpleadoDTO, String> colInstitucion;
    @FXML private TableColumn<LogroEmpleadoDTO, String> colFecha;

    @FXML private VBox formPane;
    @FXML private ComboBox<Empleado> cmbEmpleado;
    @FXML private TextField txtNombre;
    @FXML private TextField txtInstitucion;
    @FXML private DatePicker dpFecha;

    private LogroService logroService;
    private EmpleadoDAOImpl empleadoDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logroService = new LogroService();
        empleadoDAO = new EmpleadoDAOImpl();
        
        colEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreLogro"));
        colInstitucion.setCellValueFactory(new PropertyValueFactory<>("institucion"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaObtencion"));
        
        cargarDatos();
        cargarEmpleados();
    }

    private String obtenerRolActual() {
        return SesionService.getInstance().getUsuarioAutenticado() != null
                ? SesionService.getInstance().getUsuarioAutenticado().getRol()
                : "ADMIN";
    }

    private void cargarDatos() {
        try {
            tablaLogrosDTO.getItems().setAll(logroService.listarLogrosConEmpleados(obtenerRolActual()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void cargarEmpleados() {
        try {
            cmbEmpleado.getItems().setAll(empleadoDAO.listarTodos());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void eventoNuevo(ActionEvent event) {
        cmbEmpleado.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtInstitucion.clear();
        dpFecha.setValue(null);
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    @FXML
    public void eventoEditar(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("La edición de logros directos estará disponible en la próxima versión.");
        alert.showAndWait();
    }

    @FXML
    public void eventoGuardar(ActionEvent event) {
        if (txtNombre.getText().trim().isEmpty() || cmbEmpleado.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Por favor seleccione un colaborador y escriba el nombre del logro.");
            alert.showAndWait();
            return;
        }
        
        Logro nuevoLogro = new Logro();
        nuevoLogro.setNombre(txtNombre.getText().trim());
        nuevoLogro.setInstitucion(txtInstitucion.getText().trim());
        nuevoLogro.setFechaObtencion(dpFecha.getValue());
        nuevoLogro.setIdTipoLogro(1L); 
        nuevoLogro.setOrigen("OTRO");
        
        try {
            logroService.guardarLogroDeColaborador(
                    cmbEmpleado.getValue().getIdEmpleado(),
                    nuevoLogro,
                    obtenerRolActual()
            );
            cargarDatos();
            eventoCancelar(null);
        } catch (IllegalArgumentException | SecurityException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void eventoCancelar(ActionEvent event) {
        formPane.setVisible(false);
        formPane.setManaged(false);
    }
}
