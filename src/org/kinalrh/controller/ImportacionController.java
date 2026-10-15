package org.kinalrh.controller;

import java.io.File;
import java.nio.file.Path;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.kinalrh.service.AutorizacionService;
import org.kinalrh.service.ImportacionXlsxService;

public class ImportacionController {
    @FXML private Label estadoLectura;
    @FXML private Label registros;
    @FXML private Button ejecutar;
    private final ImportacionXlsxService servicio = new ImportacionXlsxService();
    private Path archivo;
    private ImportacionXlsxService.Lote lote;

    @FXML private void initialize() { ejecutar.setDisable(true); }
    @FXML private void seleccionarArchivo() {
        try {
            new AutorizacionService().exigirPermiso("IMPORTACION_EJECUTAR");
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar plantilla de empleados");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel XLSX", "*.xlsx"));
            File elegido = chooser.showOpenDialog(estadoLectura.getScene().getWindow());
            if (elegido == null) return;
            cancelar();
            archivo = elegido.toPath();
            estadoLectura.setText("Archivo seleccionado. Pulse Validar Datos.");
        } catch (SecurityException ex) { mostrar(ex.getMessage()); }
    }
    @FXML private void validar() {
        if(lote!=null) lote.cancelar();
        lote=null;
        ejecutar.setDisable(true);
        try {
            lote=servicio.previsualizar(archivo);
            long nuevos=lote.getFilas().stream().filter(f->f.getAccion()==org.kinalrh.model.FilaImportacion.Accion.NUEVO).count();
            long cambios=lote.getFilas().stream().filter(f->f.getAccion()==org.kinalrh.model.FilaImportacion.Accion.ACTUALIZAR).count();
            registros.setText("Nuevos: " + nuevos + " · Actualizaciones: " + cambios);
            estadoLectura.setText(lote.tieneErrores()?"Hay errores. Corrija el archivo y vuelva a validar.":"Validación terminada. Revise el lote antes de confirmar.");
            ejecutar.setDisable(lote.tieneErrores());
            if(lote.tieneErrores()) mostrar(String.join("\n",lote.getFilas().stream().flatMap(f->f.getErrores().stream()).map(Object::toString).distinct().toList()));
        } catch (RuntimeException ex) { mostrar(ex.getMessage()); }
    }
    @FXML private void confirmar() {
        try {
            new AutorizacionService().exigirPermiso("IMPORTACION_EJECUTAR");
            Alert aviso=new Alert(Alert.AlertType.CONFIRMATION,registros.getText()+". ¿Guardar el lote completo?",ButtonType.OK,ButtonType.CANCEL);
            if(aviso.showAndWait().orElse(ButtonType.CANCEL)!=ButtonType.OK) return;
            ImportacionXlsxService.Resultado resultado=servicio.confirmar(lote);
            ejecutar.setDisable(true);
            estadoLectura.setText("Importación completa: " + resultado.nuevos()+" nuevos y "+resultado.actualizados()+" actualizados.");
        } catch(RuntimeException ex) { mostrar(ex.getMessage()); }
    }
    @FXML private void cancelar() {
        if(lote!=null) lote.cancelar();
        lote=null; archivo=null;
        ejecutar.setDisable(true);
        registros.setText("Registros detectados: 0");
        estadoLectura.setText("Operación cancelada. No se guardaron cambios pendientes.");
    }
    public ImportacionXlsxService.Lote getLote() { return lote; }
    private void mostrar(String mensaje) { new Alert(Alert.AlertType.WARNING,mensaje,ButtonType.OK).showAndWait(); }
}
