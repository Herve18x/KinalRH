package org.kinalrh.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.kinalrh.model.FilaImportacion;
import org.kinalrh.service.AutorizacionService;
import org.kinalrh.service.ImportacionXlsxService;

/**
 * T4.04 - Pantalla de previsualización y errores por fila
 * HU: US-28
 * Responsable: Daniel
 */
public class ImportacionController implements Initializable {

    @FXML private Label estadoLectura;
    @FXML private Label registros;
    @FXML private Label lblArchivo;
    @FXML private Button ejecutar;
    @FXML private Button btnValidar;
    @FXML private Button btnCancelar;

    @FXML private TableView<FilaImportacion> tablaPrevisualizacion;
    @FXML private TableColumn<FilaImportacion, Integer> colFila;
    @FXML private TableColumn<FilaImportacion, String> colDpi;
    @FXML private TableColumn<FilaImportacion, String> colNombre;
    @FXML private TableColumn<FilaImportacion, String> colAccion;
    @FXML private TableColumn<FilaImportacion, String> colValidacion;

    private final ImportacionXlsxService servicio = new ImportacionXlsxService();
    private final AutorizacionService autorizacion = new AutorizacionService();
    private final ObservableList<FilaImportacion> listaFilas = FXCollections.observableArrayList();
    private Path archivo;
    private ImportacionXlsxService.Lote lote;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colFila.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumeroFila()).asObject());
        colDpi.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().valor(0)));
        colNombre.setCellValueFactory(data -> {
            String n = (data.getValue().valor(1) + " " + data.getValue().valor(2)).trim();
            return new SimpleStringProperty(n.isEmpty() ? "-" : n);
        });
        colAccion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccion().name()));
        colValidacion.setCellValueFactory(data -> {
            if (data.getValue().getErrores().isEmpty()) {
                return new SimpleStringProperty("✓ Correcto");
            } else {
                return new SimpleStringProperty(data.getValue().getMensajes());
            }
        });

        // Estilos para la columna de acción
        colAccion.setCellFactory(col -> new TableCell<FilaImportacion, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("NUEVO".equalsIgnoreCase(item)) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a; -fx-alignment: CENTER;");
                    } else if ("ACTUALIZAR".equalsIgnoreCase(item)) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        tablaPrevisualizacion.setItems(listaFilas);
        tablaPrevisualizacion.setPlaceholder(new Label("Seleccione un archivo XLSX y pulse 'Validar Datos' para previsualizar las filas."));
        ejecutar.setDisable(true);
    }

    @FXML
    public void seleccionarArchivo() {
        try {
            autorizacion.exigirPermiso("IMPORTACION_EJECUTAR");
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar plantilla de empleados XLSX");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Libros de Excel (*.xlsx)", "*.xlsx"));
            File elegido = chooser.showOpenDialog(estadoLectura.getScene().getWindow());
            if (elegido == null) return;

            cancelar();
            archivo = elegido.toPath();
            if (lblArchivo != null) lblArchivo.setText("Archivo: " + elegido.getName());
            estadoLectura.setText("Archivo seleccionado. Pulse '2. Validar Datos' para generar la previsualización.");
        } catch (SecurityException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", ex.getMessage());
        }
    }

    @FXML
    public void cargarFixtureValido() {
        Path p = Paths.get("test", "fixtures", "lote_valido.xlsx");
        if (new File(p.toString()).exists()) {
            cancelar();
            archivo = p;
            if (lblArchivo != null) lblArchivo.setText("Archivo: lote_valido.xlsx (Fixture)");
            estadoLectura.setText("Fixture cargado. Pulse '2. Validar Datos'.");
            validar();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "No encontrado", "No se encontró test/fixtures/lote_valido.xlsx");
        }
    }

    @FXML
    public void cargarFixtureInvalido() {
        Path p = Paths.get("test", "fixtures", "lote_invalido.xlsx");
        if (new File(p.toString()).exists()) {
            cancelar();
            archivo = p;
            if (lblArchivo != null) lblArchivo.setText("Archivo: lote_invalido.xlsx (Fixture con errores)");
            estadoLectura.setText("Fixture cargado. Pulse '2. Validar Datos'.");
            validar();
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "No encontrado", "No se encontró test/fixtures/lote_invalido.xlsx");
        }
    }

    @FXML
    public void validar() {
        if (archivo == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Archivo no seleccionado", "Por favor seleccione un archivo Excel primero.");
            return;
        }

        if (lote != null) lote.cancelar();
        lote = null;
        ejecutar.setDisable(true);

        try {
            autorizacion.exigirPermiso("IMPORTACION_EJECUTAR");
            lote = servicio.previsualizar(archivo);
            listaFilas.setAll(lote.getFilas());

            long nuevos = lote.getFilas().stream().filter(f -> f.getAccion() == FilaImportacion.Accion.NUEVO).count();
            long cambios = lote.getFilas().stream().filter(f -> f.getAccion() == FilaImportacion.Accion.ACTUALIZAR).count();
            long errores = lote.getFilas().stream().filter(f -> f.getAccion() == FilaImportacion.Accion.ERROR).count();

            registros.setText(String.format("Total: %d · Nuevos: %d · Actualizaciones: %d · Errores: %d",
                    lote.getFilas().size(), nuevos, cambios, errores));

            if (lote.tieneErrores()) {
                estadoLectura.setText("Se detectaron errores bloqueantes. Revise la columna de validación.");
                ejecutar.setDisable(true);
            } else {
                estadoLectura.setText("Validación completada sin errores. Puede confirmar la importación.");
                ejecutar.setDisable(false);
            }
        } catch (SecurityException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", ex.getMessage());
            estadoLectura.setText("Error al validar el archivo: " + ex.getMessage());
        }
    }

    @FXML
    public void confirmar() {
        if (lote == null || lote.tieneErrores()) {
            mostrarAlerta(Alert.AlertType.WARNING, "No se puede importar", "El lote contiene errores o no ha sido validado.");
            return;
        }

        try {
            autorizacion.exigirPermiso("IMPORTACION_EJECUTAR");
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                registros.getText() + "\n\n¿Desea proceder a registrar estos cambios en la base de datos?", 
                ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Confirmar Importación Masiva");

            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }

            ImportacionXlsxService.Resultado resultado = servicio.confirmar(lote);
            ejecutar.setDisable(true);
            estadoLectura.setText(String.format("Importación exitosa: %d nuevos registrados y %d actualizados.",
                    resultado.nuevos(), resultado.actualizados()));

            mostrarAlerta(Alert.AlertType.INFORMATION, "Importación Finalizada",
                    String.format("Se importaron exitosamente %d registros nuevos y se actualizaron %d existentes.",
                            resultado.nuevos(), resultado.actualizados()));

            cancelar();
        } catch (SecurityException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error en Importación", ex.getMessage());
        }
    }

    @FXML
    public void cancelar() {
        if (lote != null) {
            lote.cancelar();
            lote = null;
        }
        listaFilas.clear();
        archivo = null;
        ejecutar.setDisable(true);
        if (lblArchivo != null) lblArchivo.setText("Archivo: Ninguno seleccionado");
        registros.setText("Registros detectados: 0");
        estadoLectura.setText("Operación cancelada. La base de datos no sufrió modificaciones.");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
