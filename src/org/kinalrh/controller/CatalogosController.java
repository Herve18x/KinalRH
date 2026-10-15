package org.kinalrh.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.kinalrh.model.NivelAcademico;
import org.kinalrh.model.TipoLogro;
import org.kinalrh.service.CatalogoService;
import org.kinalrh.service.SesionService;

/**
 * Controlador para la vista Catalogos.fxml.
 * T3.09 Lectura/gestión básica de nivel académico y tipo de logro.
 * T3.12 Verificar que solo se guarda un nivel válido y activo.
 */
public class CatalogosController implements Initializable {

    private final CatalogoService catalogoService = new CatalogoService();

    // --- TAB NIVELES ACADÉMICOS ---
    @FXML private TableView<NivelAcademico> tblNiveles;
    @FXML private TableColumn<NivelAcademico, Long> colNivelId;
    @FXML private TableColumn<NivelAcademico, String> colNivelCodigo;
    @FXML private TableColumn<NivelAcademico, String> colNivelNombre;
    @FXML private TableColumn<NivelAcademico, Integer> colNivelOrden;
    @FXML private TableColumn<NivelAcademico, String> colNivelActivo;

    @FXML private TextField txtNivelId;
    @FXML private TextField txtNivelCodigo;
    @FXML private TextField txtNivelNombre;
    @FXML private TextField txtNivelOrden;
    @FXML private CheckBox chkNivelActivo;
    @FXML private Label lblMensajeNivel;
    @FXML private Button btnNuevoNivel;
    @FXML private Button btnGuardarNivel;
    @FXML private Button btnEstadoNivel;

    // --- TAB TIPOS DE LOGRO ---
    @FXML private TableView<TipoLogro> tblTiposLogro;
    @FXML private TableColumn<TipoLogro, Long> colTipoId;
    @FXML private TableColumn<TipoLogro, String> colTipoCodigo;
    @FXML private TableColumn<TipoLogro, String> colTipoNombre;
    @FXML private TableColumn<TipoLogro, String> colTipoDescripcion;
    @FXML private TableColumn<TipoLogro, String> colTipoActivo;

    @FXML private TextField txtTipoId;
    @FXML private TextField txtTipoCodigo;
    @FXML private TextField txtTipoNombre;
    @FXML private TextField txtTipoDescripcion;
    @FXML private CheckBox chkTipoActivo;
    @FXML private Label lblMensajeTipo;
    @FXML private Button btnNuevoTipo;
    @FXML private Button btnGuardarTipo;
    @FXML private Button btnEstadoTipo;

    private final ObservableList<NivelAcademico> listaNiveles = FXCollections.observableArrayList();
    private final ObservableList<TipoLogro> listaTiposLogro = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablas();
        cargarDatosNiveles();
        cargarDatosTiposLogro();

        tblNiveles.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                seleccionarNivel(newSel);
            }
        });

        tblTiposLogro.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                seleccionarTipoLogro(newSel);
            }
        });
    }

    private String getRolActual() {
        if (SesionService.getInstance().getUsuarioAutenticado() != null) {
            return SesionService.getInstance().getUsuarioAutenticado().getRol();
        }
        return "ADMIN";
    }

    private void configurarTablas() {
        // Niveles
        colNivelId.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getIdNivelAcademico()).asObject());
        colNivelCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colNivelNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colNivelOrden.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getOrdenNivel()).asObject());
        colNivelActivo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActivo() ? "ACTIVO" : "INACTIVO"));
        tblNiveles.setItems(listaNiveles);

        // Tipos de logro
        colTipoId.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getIdTipoLogro()).asObject());
        colTipoCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colTipoNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colTipoDescripcion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescripcion() != null ? data.getValue().getDescripcion() : ""));
        colTipoActivo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActivo() ? "ACTIVO" : "INACTIVO"));
        tblTiposLogro.setItems(listaTiposLogro);
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ==========================================
    // --- GESTIÓN DE NIVELES ACADÉMICOS ---
    // ==========================================
    public void cargarDatosNiveles() {
        listaNiveles.clear();
        listaNiveles.addAll(catalogoService.listarNivelesAcademicos(getRolActual()));
    }

    @FXML
    public void eventoSeleccionarNivel() {
        NivelAcademico sel = tblNiveles.getSelectionModel().getSelectedItem();
        if (sel != null) {
            seleccionarNivel(sel);
        }
    }

    private void seleccionarNivel(NivelAcademico nivel) {
        if (nivel == null) return;
        txtNivelId.setText(String.valueOf(nivel.getIdNivelAcademico()));
        txtNivelCodigo.setText(nivel.getCodigo());
        txtNivelNombre.setText(nivel.getNombre());
        txtNivelOrden.setText(String.valueOf(nivel.getOrdenNivel()));
        chkNivelActivo.setSelected(nivel.isActivo());
        btnGuardarNivel.setText("💾 Actualizar");
        btnEstadoNivel.setText(nivel.isActivo() ? "🚫 Desactivar" : "✅ Activar");
        if (lblMensajeNivel != null) lblMensajeNivel.setText("");
    }

    @FXML
    public void eventoNuevoNivel() {
        txtNivelId.clear();
        txtNivelCodigo.clear();
        txtNivelNombre.clear();
        txtNivelOrden.setText("1");
        chkNivelActivo.setSelected(true);
        tblNiveles.getSelectionModel().clearSelection();
        btnGuardarNivel.setText("💾 Guardar");
        btnEstadoNivel.setText("🚫 Desactivar");
        if (lblMensajeNivel != null) lblMensajeNivel.setText("");
        txtNivelCodigo.requestFocus();
    }

    @FXML
    public void eventoGuardarNivel() {
        try {
            String codigo = txtNivelCodigo.getText() != null ? txtNivelCodigo.getText().trim() : "";
            String nombre = txtNivelNombre.getText() != null ? txtNivelNombre.getText().trim() : "";
            
            if (codigo.isEmpty() || nombre.isEmpty()) {
                mostrarAlerta(AlertType.WARNING, "Campos Requeridos", "Debe ingresar el código y el nombre del nivel académico.");
                return;
            }

            int orden = 1;
            try {
                orden = Integer.parseInt(txtNivelOrden.getText().trim());
            } catch (Exception ignored) {}

            boolean activo = chkNivelActivo.isSelected();
            Long id = null;
            if (txtNivelId.getText() != null && !txtNivelId.getText().trim().isEmpty() && !txtNivelId.getText().trim().equalsIgnoreCase("Auto")) {
                id = Long.parseLong(txtNivelId.getText().trim());
            }

            NivelAcademico nivel = new NivelAcademico(id, codigo, nombre, orden, activo);
            long idGuardado = catalogoService.guardarNivelAcademico(nivel, getRolActual());

            mostrarAlerta(AlertType.INFORMATION, "Nivel Académico Guardado", 
                    id == null ? "Nivel académico registrado exitosamente con ID " + idGuardado + "."
                               : "Nivel académico '" + nombre + "' actualizado correctamente.");
            
            cargarDatosNiveles();
            eventoNuevoNivel();
        } catch (Exception ex) {
            mostrarAlerta(AlertType.ERROR, "Error al guardar", ex.getMessage());
        }
    }

    @FXML
    public void eventoCambiarEstadoNivel() {
        NivelAcademico sel = tblNiveles.getSelectionModel().getSelectedItem();
        Long id = null;
        boolean estadoActual = true;
        String nombre = "";

        if (sel != null) {
            id = sel.getIdNivelAcademico();
            estadoActual = sel.isActivo();
            nombre = sel.getNombre();
        } else if (txtNivelId.getText() != null && !txtNivelId.getText().trim().isEmpty() && !txtNivelId.getText().trim().equalsIgnoreCase("Auto")) {
            try {
                id = Long.parseLong(txtNivelId.getText().trim());
                estadoActual = chkNivelActivo.isSelected();
                nombre = txtNivelNombre.getText();
            } catch (Exception ignored) {}
        }

        if (id == null) {
            mostrarAlerta(AlertType.WARNING, "Selección requerida", "Seleccione un nivel académico de la tabla para cambiar su estado.");
            return;
        }

        try {
            boolean nuevoEstado = !estadoActual;
            catalogoService.cambiarEstadoNivelAcademico(id, nuevoEstado, getRolActual());
            mostrarAlerta(AlertType.INFORMATION, "Estado Actualizado", "El nivel académico '" + nombre + "' ahora está " + (nuevoEstado ? "ACTIVO" : "INACTIVO") + ".");
            cargarDatosNiveles();
            eventoNuevoNivel();
        } catch (Exception ex) {
            mostrarAlerta(AlertType.ERROR, "Error al cambiar estado", ex.getMessage());
        }
    }

    // ==========================================
    // --- GESTIÓN DE TIPOS DE LOGRO ---
    // ==========================================
    public void cargarDatosTiposLogro() {
        listaTiposLogro.clear();
        listaTiposLogro.addAll(catalogoService.listarTiposLogro(getRolActual()));
    }

    @FXML
    public void eventoSeleccionarTipo() {
        TipoLogro sel = tblTiposLogro.getSelectionModel().getSelectedItem();
        if (sel != null) {
            seleccionarTipoLogro(sel);
        }
    }

    private void seleccionarTipoLogro(TipoLogro tipo) {
        if (tipo == null) return;
        txtTipoId.setText(String.valueOf(tipo.getIdTipoLogro()));
        txtTipoCodigo.setText(tipo.getCodigo());
        txtTipoNombre.setText(tipo.getNombre());
        txtTipoDescripcion.setText(tipo.getDescripcion() != null ? tipo.getDescripcion() : "");
        chkTipoActivo.setSelected(tipo.isActivo());
        btnGuardarTipo.setText("💾 Actualizar");
        btnEstadoTipo.setText(tipo.isActivo() ? "🚫 Desactivar" : "✅ Activar");
        if (lblMensajeTipo != null) lblMensajeTipo.setText("");
    }

    @FXML
    public void eventoNuevoTipo() {
        txtTipoId.clear();
        txtTipoCodigo.clear();
        txtTipoNombre.clear();
        txtTipoDescripcion.clear();
        chkTipoActivo.setSelected(true);
        tblTiposLogro.getSelectionModel().clearSelection();
        btnGuardarTipo.setText("💾 Guardar");
        btnEstadoTipo.setText("🚫 Desactivar");
        if (lblMensajeTipo != null) lblMensajeTipo.setText("");
        txtTipoCodigo.requestFocus();
    }

    @FXML
    public void eventoGuardarTipo() {
        try {
            String codigo = txtTipoCodigo.getText() != null ? txtTipoCodigo.getText().trim() : "";
            String nombre = txtTipoNombre.getText() != null ? txtTipoNombre.getText().trim() : "";
            String descripcion = txtTipoDescripcion.getText() != null ? txtTipoDescripcion.getText().trim() : "";
            
            if (codigo.isEmpty() || nombre.isEmpty()) {
                mostrarAlerta(AlertType.WARNING, "Campos Requeridos", "Debe ingresar el código y el nombre del tipo de logro.");
                return;
            }

            boolean activo = chkTipoActivo.isSelected();
            Long id = null;
            if (txtTipoId.getText() != null && !txtTipoId.getText().trim().isEmpty() && !txtTipoId.getText().trim().equalsIgnoreCase("Auto")) {
                id = Long.parseLong(txtTipoId.getText().trim());
            }

            TipoLogro tipo = new TipoLogro(id, codigo, nombre, descripcion, activo);
            long idGuardado = catalogoService.guardarTipoLogro(tipo, getRolActual());

            mostrarAlerta(AlertType.INFORMATION, "Tipo de Logro Guardado", 
                    id == null ? "Tipo de logro registrado exitosamente con ID " + idGuardado + "."
                               : "Tipo de logro '" + nombre + "' actualizado correctamente.");
            
            cargarDatosTiposLogro();
            eventoNuevoTipo();
        } catch (Exception ex) {
            mostrarAlerta(AlertType.ERROR, "Error al guardar", ex.getMessage());
        }
    }

    @FXML
    public void eventoCambiarEstadoTipo() {
        TipoLogro sel = tblTiposLogro.getSelectionModel().getSelectedItem();
        Long id = null;
        boolean estadoActual = true;
        String nombre = "";

        if (sel != null) {
            id = sel.getIdTipoLogro();
            estadoActual = sel.isActivo();
            nombre = sel.getNombre();
        } else if (txtTipoId.getText() != null && !txtTipoId.getText().trim().isEmpty() && !txtTipoId.getText().trim().equalsIgnoreCase("Auto")) {
            try {
                id = Long.parseLong(txtTipoId.getText().trim());
                estadoActual = chkTipoActivo.isSelected();
                nombre = txtTipoNombre.getText();
            } catch (Exception ignored) {}
        }

        if (id == null) {
            mostrarAlerta(AlertType.WARNING, "Selección requerida", "Seleccione un tipo de logro de la tabla para cambiar su estado.");
            return;
        }

        try {
            boolean nuevoEstado = !estadoActual;
            catalogoService.cambiarEstadoTipoLogro(id, nuevoEstado, getRolActual());
            mostrarAlerta(AlertType.INFORMATION, "Estado Actualizado", "El tipo de logro '" + nombre + "' ahora está " + (nuevoEstado ? "ACTIVO" : "INACTIVO") + ".");
            cargarDatosTiposLogro();
            eventoNuevoTipo();
        } catch (Exception ex) {
            mostrarAlerta(AlertType.ERROR, "Error al cambiar estado", ex.getMessage());
        }
    }
}
