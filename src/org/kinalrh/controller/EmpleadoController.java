package org.kinalrh.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.kinalrh.model.Empleado;
import org.kinalrh.service.EmpleadoService;

public class EmpleadoController implements Initializable {

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colEstado;
    @FXML private TableColumn<Empleado, String> colArea;
    @FXML private TableColumn<Empleado, String> colPuesto;

    @FXML private Button btnNuevo;
    @FXML private Button btnRefrescar;

    private EmpleadoService empleadoService;
    private int idSeleccionadoGuardado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empleadoService = new EmpleadoService();
        configurarTabla();
        cargarDatos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colArea.setCellValueFactory(new PropertyValueFactory<>("area"));
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));
    }

    private void cargarDatos() {
        // Guardar id visible seleccionado (si hay)
        Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            idSeleccionadoGuardado = seleccionado.getIdEmpleado();
        }

        List<Empleado> empleados = empleadoService.listarEmpleados("ADMIN");
        tablaEmpleados.getItems().setAll(empleados);

        // Restaurar seleccion
        if (idSeleccionadoGuardado != -1) {
            for (Empleado e : tablaEmpleados.getItems()) {
                if (e.getIdEmpleado() == idSeleccionadoGuardado) {
                    tablaEmpleados.getSelectionModel().select(e);
                    break;
                }
            }
        }
    }

    @FXML
    public void eventoRefrescar(ActionEvent event) {
        cargarDatos();
    }

    @FXML
    public void eventoNuevo(ActionEvent event) {
        abrirFichaEmpleado(0);
    }

    @FXML
    public void eventoSeleccionTabla(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                abrirFichaEmpleado(seleccionado.getIdEmpleado());
            }
        }
    }

    private void abrirFichaEmpleado(int idEmpleado) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/kinalrh/view/FormularioEmpleado.fxml"));
            javafx.scene.Parent root = loader.load();
            
            EmpleadoFormController controller = loader.getController();
            Empleado emp = idEmpleado == 0 ? null : new EmpleadoService().buscarPorId(idEmpleado);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            controller.setStage(stage);
            controller.setEmpleado(emp);
            
            stage.setTitle(idEmpleado == 0 ? "Nuevo Empleado" : "Editar Empleado");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            cargarDatos(); // Refrescar despus de cerrar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}