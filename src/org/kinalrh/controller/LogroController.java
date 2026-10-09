package org.kinalrh.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.kinalrh.dao.impl.LogroDAOImpl;
import org.kinalrh.model.Logro;

public class LogroController implements Initializable {

    @FXML private TableView<Logro> tablaLogros;
    @FXML private TableColumn<Logro, String> colNombre;
    @FXML private TableColumn<Logro, String> colInstitucion;
    @FXML private TableColumn<Logro, String> colOrigen;
    @FXML private TableColumn<Logro, String> colFecha;

    @FXML private VBox formPane;
    @FXML private TextField txtNombre;
    @FXML private TextField txtInstitucion;

    private LogroDAOImpl logroDAO;
    private Logro logroActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logroDAO = new LogroDAOImpl();
        
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colInstitucion.setCellValueFactory(new PropertyValueFactory<>("institucion"));
        colOrigen.setCellValueFactory(new PropertyValueFactory<>("origen"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaObtencion"));
        
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            tablaLogros.getItems().setAll(logroDAO.listarTodos());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void eventoNuevo(ActionEvent event) {
        logroActual = new Logro();
        txtNombre.clear();
        txtInstitucion.clear();
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    @FXML
    public void eventoEditar(ActionEvent event) {
        logroActual = tablaLogros.getSelectionModel().getSelectedItem();
        if (logroActual != null) {
            txtNombre.setText(logroActual.getNombre());
            txtInstitucion.setText(logroActual.getInstitucion());
            formPane.setVisible(true);
            formPane.setManaged(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Por favor seleccione un logro para editar.");
            alert.showAndWait();
        }
    }

    @FXML
    public void eventoGuardar(ActionEvent event) {
        if (txtNombre.getText().trim().isEmpty()) return;
        
        logroActual.setNombre(txtNombre.getText().trim());
        logroActual.setInstitucion(txtInstitucion.getText().trim());
        logroActual.setIdTipoLogro(1L); // Default por ahora
        logroActual.setOrigen("OTRO");
        
        try {
            if (logroActual.getIdLogro() == null) {
                logroDAO.insertar(logroActual);
            } else {
                logroDAO.actualizar(logroActual);
            }
            cargarDatos();
            eventoCancelar(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void eventoCancelar(ActionEvent event) {
        formPane.setVisible(false);
        formPane.setManaged(false);
        logroActual = null;
    }
}