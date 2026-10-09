package org.kinalrh.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kinalrh.model.Empleado;
import org.kinalrh.service.EmpleadoService;

public class EmpleadoFormController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private Label lblError;
    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtArea;
    @FXML private TextField txtPuesto;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private Empleado empleadoEdicion;
    private Stage stagePrincipal;
    private EmpleadoService empleadoService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empleadoService = new EmpleadoService();
        cmbEstado.getItems().addAll("Activo", "Inactivo");
        cmbEstado.getSelectionModel().selectFirst();
    }

    public void setStage(Stage stage) {
        this.stagePrincipal = stage;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleadoEdicion = empleado;
        if (empleado != null) {
            lblTitulo.setText("Editar Ficha Empleado");
            txtId.setText(String.valueOf(empleado.getIdEmpleado()));
            txtNombre.setText(empleado.getNombre());
            txtArea.setText(empleado.getArea());
            txtPuesto.setText(empleado.getPuesto());
            cmbEstado.getSelectionModel().select(empleado.getEstado());
        } else {
            lblTitulo.setText("Nuevo Empleado");
        }
    }

    @FXML
    public void eventoGuardar(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            lblError.setText("El nombre es requerido.");
            lblError.setVisible(true);
            return;
        }

        if (empleadoEdicion == null) {
            empleadoEdicion = new Empleado(0, nombre, cmbEstado.getValue(), txtArea.getText().trim(), txtPuesto.getText().trim());
        } else {
            empleadoEdicion.setNombre(nombre);
            empleadoEdicion.setEstado(cmbEstado.getValue());
            empleadoEdicion.setArea(txtArea.getText().trim());
            empleadoEdicion.setPuesto(txtPuesto.getText().trim());
        }

        empleadoService.guardarEmpleado(empleadoEdicion);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Éxito");
        alerta.setHeaderText(null);
        alerta.setContentText("Empleado guardado correctamente.");
        alerta.showAndWait();

        cerrarVentana();
    }

    @FXML
    public void eventoCancelar(ActionEvent event) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        if (stagePrincipal != null) {
            stagePrincipal.close();
        }
    }
}