package org.kinalrh.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.kinalrh.dao.impl.AreaDAOImpl;
import org.kinalrh.dao.impl.PuestoDAOImpl;
import org.kinalrh.model.Area;
import org.kinalrh.model.FilaReporte;
import org.kinalrh.model.Puesto;
import org.kinalrh.service.ReporteService;

public class ReporteController implements Initializable {

    @FXML private ComboBox<Area> cmbArea;
    @FXML private ComboBox<Puesto> cmbPuesto;
    @FXML private Button btnLimpiar;
    @FXML private Button btnExportar;
    @FXML private Label lblContador;
    @FXML private Label lblEstadoFiltro;

    @FXML private TableView<FilaReporte> tablaReporte;
    @FXML private TableColumn<FilaReporte, Long> colId;
    @FXML private TableColumn<FilaReporte, String> colNombre;
    @FXML private TableColumn<FilaReporte, String> colEstado;
    @FXML private TableColumn<FilaReporte, String> colArea;
    @FXML private TableColumn<FilaReporte, String> colPuesto;

    private final ReporteService reporteService = new ReporteService();
    private final ObservableList<FilaReporte> listaFilas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().id()).asObject());
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nombre()));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().estado()));
        colArea.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().area()));
        colPuesto.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().puesto()));

        tablaReporte.setItems(listaFilas);
        tablaReporte.setPlaceholder(new Label("Sin resultados."));

        cargarFiltros();
        cargarDatos();

        cmbArea.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatos());
        cmbPuesto.valueProperty().addListener((obs, oldVal, newVal) -> cargarDatos());
    }

    private void cargarFiltros() {
        try {
            cmbArea.getItems().setAll(new AreaDAOImpl().listarActivas());
            cmbPuesto.getItems().setAll(new PuestoDAOImpl().listarActivos());
        } catch (Exception e) {
            System.err.println("Error al cargar combos de reporte: " + e.getMessage());
        }
    }

    public void cargarDatos() {
        listaFilas.clear();
        Long idArea = cmbArea.getValue() != null ? cmbArea.getValue().getIdArea() : null;
        Long idPuesto = cmbPuesto.getValue() != null ? cmbPuesto.getValue().getIdPuesto() : null;

        try {
            List<FilaReporte> resultados = reporteService.consultar(idArea, idPuesto);
            listaFilas.addAll(resultados);

            lblContador.setText("Colaboradores activos encontrados: " + listaFilas.size());
            if (idArea == null && idPuesto == null) {
                lblEstadoFiltro.setText("Mostrando todos los colaboradores activos.");
            } else {
                lblEstadoFiltro.setText("Filtro aplicado (" + (idArea != null ? cmbArea.getValue().getNombre() : "") + 
                                        (idArea != null && idPuesto != null ? " / " : "") + 
                                        (idPuesto != null ? cmbPuesto.getValue().getNombre() : "") + ")");
            }
        } catch (SecurityException ex) {
            lblContador.setText("Acceso denegado.");
            lblEstadoFiltro.setText("No cuenta con permisos para consultar este reporte.");
            tablaReporte.setPlaceholder(new Label("Acceso denegado: Se requiere permiso EMPLEADO_VER."));
        } catch (Exception ex) {
            lblContador.setText("Error en consulta.");
            lblEstadoFiltro.setText(ex.getMessage());
        }
    }

    @FXML
    public void eventoLimpiar() {
        cmbArea.getSelectionModel().clearSelection();
        cmbPuesto.getSelectionModel().clearSelection();
        cargarDatos();
    }

    @FXML
    public void eventoExportar() {
        if (listaFilas.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", "No hay registros disponibles para exportar con los filtros seleccionados.");
            return;
        }

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Guardar Reporte XLSX");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"));
            chooser.setInitialFileName("Reporte_Empleados_Activos.xlsx");

            File destino = chooser.showSaveDialog(tablaReporte.getScene().getWindow());
            if (destino == null) return;

            Long idArea = cmbArea.getValue() != null ? cmbArea.getValue().getIdArea() : null;
            Long idPuesto = cmbPuesto.getValue() != null ? cmbPuesto.getValue().getIdPuesto() : null;

            boolean ok = reporteService.exportar(destino.toPath(), idArea, idPuesto);
            if (ok) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Exportación Exitosa", 
                    "El reporte ha sido generado correctamente con formato oficial y trazabilidad en:\n" + destino.getAbsolutePath());
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo generar el archivo de reporte.");
            }
        } catch (SecurityException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Permiso Denegado", "No cuenta con el permiso REPORTES_XLSX para exportar.");
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Exportación", ex.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
