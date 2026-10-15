package org.kinalrh.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.kinalrh.model.Auditoria;
import org.kinalrh.service.AuditoriaService;

/**
 * Controlador para la vista del Log de Auditoría (Auditoria.fxml).
 * T1.23, T3.18 Trazabilidad y auditoría integral del sistema.
 */
public class AuditoriaController implements Initializable {

    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cmbEntidad;
    @FXML private ComboBox<Integer> cmbLimite;
    @FXML private Button btnActualizar;
    @FXML private Label lblContador;

    @FXML private TableView<Auditoria> tablaAuditoria;
    @FXML private TableColumn<Auditoria, Long> colId;
    @FXML private TableColumn<Auditoria, String> colFecha;
    @FXML private TableColumn<Auditoria, String> colUsuario;
    @FXML private TableColumn<Auditoria, String> colAccion;
    @FXML private TableColumn<Auditoria, String> colEntidad;
    @FXML private TableColumn<Auditoria, String> colRegistroId;
    @FXML private TableColumn<Auditoria, String> colCampo;
    @FXML private TableColumn<Auditoria, String> colAnterior;
    @FXML private TableColumn<Auditoria, String> colNuevo;
    @FXML private TableColumn<Auditoria, String> colSesion;

    private final AuditoriaService auditoriaService = new AuditoriaService();
    private final ObservableList<Auditoria> masterData = FXCollections.observableArrayList();
    private FilteredList<Auditoria> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarFiltros();
        cargarDatos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().getIdAuditoria()).asObject());
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaHoraFormateada()));
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreUsuario()));
        colAccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccion()));
        colEntidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEntidad()));
        colRegistroId.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRegistroId() != null ? String.valueOf(d.getValue().getRegistroId()) : "-"));
        colCampo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCampo()));
        colAnterior.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValorAnterior()));
        colNuevo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValorNuevo()));
        colSesion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUserAgent()));

        filteredData = new FilteredList<>(masterData, p -> true);
        tablaAuditoria.setItems(filteredData);
    }

    private void configurarFiltros() {
        if (cmbEntidad != null) {
            cmbEntidad.getItems().setAll("TODAS", "empleado", "usuario", "nivel_academico", "tipo_logro", "SESION", "empleado_logro", "OPERACION");
            cmbEntidad.getSelectionModel().selectFirst();
            cmbEntidad.valueProperty().addListener((obs, oldV, newV) -> cargarDatos());
        }

        if (cmbLimite != null) {
            cmbLimite.getItems().setAll(50, 100, 250, 500);
            cmbLimite.getSelectionModel().select(1); // 100 por defecto
            cmbLimite.valueProperty().addListener((obs, oldV, newV) -> cargarDatos());
        }

        if (txtBusqueda != null) {
            txtBusqueda.textProperty().addListener((obs, oldV, newV) -> aplicarFiltroTexto());
        }
    }

    public void cargarDatos() {
        String entidadSel = (cmbEntidad != null && cmbEntidad.getValue() != null) ? cmbEntidad.getValue() : "TODAS";
        int limite = (cmbLimite != null && cmbLimite.getValue() != null) ? cmbLimite.getValue() : 100;

        List<Auditoria> lista = auditoriaService.listarRegistrosAuditoria(null, entidadSel, limite);
        masterData.setAll(lista);
        aplicarFiltroTexto();
    }

    private void aplicarFiltroTexto() {
        String filtro = (txtBusqueda != null && txtBusqueda.getText() != null) ? txtBusqueda.getText().trim().toLowerCase() : "";
        filteredData.setPredicate(ev -> {
            if (filtro.isEmpty()) return true;
            return (ev.getAccion() != null && ev.getAccion().toLowerCase().contains(filtro))
                    || (ev.getNombreUsuario() != null && ev.getNombreUsuario().toLowerCase().contains(filtro))
                    || (ev.getEntidad() != null && ev.getEntidad().toLowerCase().contains(filtro))
                    || (ev.getCampo() != null && ev.getCampo().toLowerCase().contains(filtro))
                    || (ev.getValorNuevo() != null && ev.getValorNuevo().toLowerCase().contains(filtro))
                    || (ev.getUserAgent() != null && ev.getUserAgent().toLowerCase().contains(filtro));
        });

        if (lblContador != null) {
            lblContador.setText("Mostrando " + filteredData.size() + " registros de auditoría.");
        }
    }

    @FXML
    public void eventoActualizar() {
        cargarDatos();
    }

    @FXML
    public void eventoLimpiarFiltros() {
        if (txtBusqueda != null) txtBusqueda.clear();
        if (cmbEntidad != null) cmbEntidad.getSelectionModel().selectFirst();
        cargarDatos();
    }

    @FXML
    public void eventoDobleClicFila(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Auditoria sel = tablaAuditoria.getSelectionModel().getSelectedItem();
            if (sel != null) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Detalle del Evento de Auditoría #" + sel.getIdAuditoria());
                alert.setHeaderText("Acción: " + sel.getAccion() + " | Entidad: " + sel.getEntidad());
                String sb = "• ID Evento: " + sel.getIdAuditoria() + "\n" +
                        "• Fecha y Hora: " + sel.getFechaHoraFormateada() + "\n" +
                        "• Autor (Usuario): " + sel.getNombreUsuario() + " (ID: " + sel.getIdUsuario() + ")\n" +
                        "• Entidad Afectada: " + sel.getEntidad() + "\n" +
                        "• ID Registro: " + (sel.getRegistroId() != null ? sel.getRegistroId() : "N/A") + "\n" +
                        "• Campo Modificado: " + sel.getCampo() + "\n" +
                        "• Valor Anterior: " + sel.getValorAnterior() + "\n" +
                        "• Valor Nuevo: " + sel.getValorNuevo() + "\n" +
                        "• IP de Origen: " + sel.getIp() + "\n" +
                        "• Contexto / Sesión: " + sel.getUserAgent();
                alert.setContentText(sb);
                alert.showAndWait();
            }
        }
    }
}
