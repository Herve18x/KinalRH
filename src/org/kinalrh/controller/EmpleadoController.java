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
        // TBD: LÃ³gica para nuevo empleado
        System.out.println("Abriendo ficha para nuevo empleado...");
    }

    @FXML
    public void eventoSeleccionTabla(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Empleado seleccionado = tablaEmpleados.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                // T2.18: "nunca por Ã­ndice de la tabla, abrir ficha por id_empleado"
                abrirFichaEmpleado(seleccionado.getIdEmpleado());
            }
        }
    }

    private void abrirFichaEmpleado(int idEmpleado) {
        // TBD: LÃ³gica para abrir ficha
        System.out.println("Abriendo ficha para el empleado con ID: " + idEmpleado);
    }
}