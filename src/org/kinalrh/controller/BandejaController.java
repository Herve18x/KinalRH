package org.kinalrh.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.kinalrh.model.SolicitudCambio;
import org.kinalrh.service.AuditoriaService;
import org.kinalrh.service.SesionService;
import org.kinalrh.util.Conexion;

public class BandejaController implements Initializable {

    @FXML private TableView<SolicitudCambio> tablaSolicitudes;
    @FXML private TableColumn<SolicitudCambio, Long> colId;
    @FXML private TableColumn<SolicitudCambio, String> colFecha;
    @FXML private TableColumn<SolicitudCambio, String> colColaborador;
    @FXML private TableColumn<SolicitudCambio, String> colMotivo;
    @FXML private TableColumn<SolicitudCambio, String> colCampoAnterior;
    @FXML private TableColumn<SolicitudCambio, String> colCampoNuevo;
    @FXML private TableColumn<SolicitudCambio, String> colEstado;

    @FXML private TextArea txtComentario;
    @FXML private Button btnAprobar;
    @FXML private Button btnRechazar;

    private final ObservableList<SolicitudCambio> listaSolicitudes = FXCollections.observableArrayList();
    private final AuditoriaService auditoriaService = new AuditoriaService();

    public static class EmpleadoComboItem {
        private final Long id;
        private final String nombre;

        public EmpleadoComboItem(Long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() {
            return nombre + " (ID: " + id + ")";
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idSolicitud"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colColaborador.setCellValueFactory(new PropertyValueFactory<>("colaborador"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colCampoAnterior.setCellValueFactory(new PropertyValueFactory<>("campoAnterior"));
        colCampoNuevo.setCellValueFactory(new PropertyValueFactory<>("campoNuevo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Resaltar visualmente el estado
        colEstado.setCellFactory(col -> new TableCell<SolicitudCambio, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    if ("PENDIENTE".equalsIgnoreCase(estado)) {
                        setStyle("-fx-text-fill: #EB7207; -fx-font-weight: bold;");
                    } else if ("APROBADA".equalsIgnoreCase(estado)) {
                        setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                    } else if ("RECHAZADA".equalsIgnoreCase(estado)) {
                        setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #23376D; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tablaSolicitudes.setPlaceholder(new Label("No hay solicitudes registradas en la bandeja."));
        tablaSolicitudes.setItems(listaSolicitudes);

        // Controlar botones al seleccionar fila
        tablaSolicitudes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                boolean esPendiente = "PENDIENTE".equalsIgnoreCase(newSel.getEstado());
                btnAprobar.setDisable(!esPendiente);
                btnRechazar.setDisable(!esPendiente);
                if (!esPendiente) {
                    txtComentario.setPromptText("Solicitud ya resuelta como " + newSel.getEstado() + ".");
                } else {
                    txtComentario.setPromptText("Ingrese un comentario antes de aprobar o rechazar...");
                }
            } else {
                btnAprobar.setDisable(false);
                btnRechazar.setDisable(false);
            }
        });

        cargarSolicitudes();
    }

    public void cargarSolicitudes() {
        new org.kinalrh.service.AutorizacionService().exigirAlgunPermiso("CAMBIO_CREAR", "CAMBIO_APROBAR_RH", "CAMBIO_APROBAR_GERENCIA");
        listaSolicitudes.clear();
        String sql = "SELECT s.id_solicitud_cambio, s.fecha_solicitud, " +
                     "COALESCE(e.nombre_completo, CONCAT(COALESCE(e.primer_nombre, ''), ' ', COALESCE(e.primer_apellido, ''))) AS nombre_empleado, " +
                     "s.motivo, s.estado, " +
                     "d.campo, d.valor_anterior, d.valor_nuevo " +
                     "FROM solicitud_cambio s " +
                     "JOIN empleado e ON s.id_empleado = e.id_empleado " +
                     "LEFT JOIN detalle_cambio d ON s.id_solicitud_cambio = d.id_solicitud_cambio " +
                     "ORDER BY s.fecha_solicitud DESC";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id_solicitud_cambio");
                java.sql.Timestamp f = rs.getTimestamp("fecha_solicitud");
                String fechaStr = f != null ? sdf.format(f) : "";
                String empleado = rs.getString("nombre_empleado");
                String motivo = rs.getString("motivo");
                String est = rs.getString("estado");
                String campo = rs.getString("campo");
                String valAnt = rs.getString("valor_anterior");
                String valNue = rs.getString("valor_nuevo");

                String ant = (campo != null ? campo + ": " : "") + (valAnt != null ? valAnt : "-");
                String nue = (campo != null ? campo + ": " : "") + (valNue != null ? valNue : "-");

                listaSolicitudes.add(new SolicitudCambio(id, fechaStr, empleado, motivo, ant, nue, est));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar solicitudes: " + e.getMessage());
        }
    }

    @FXML
    public void eventoNuevaSolicitud() {
        new org.kinalrh.service.AutorizacionService().exigirPermiso("CAMBIO_CREAR");
        List<EmpleadoComboItem> empleados = obtenerEmpleadosActivos();
        if (empleados.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin colaboradores", "No se encontraron colaboradores registrados en la base de datos.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("➕ Registrar Nueva Solicitud de Cambio");
        dialog.setHeaderText("Formulario de Solicitud de Cambio Administrativo\n(El registro ingresará en estado PENDIENTE para revisión)");

        ButtonType btnRegistrar = new ButtonType("Enviar Solicitud", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnRegistrar, ButtonType.CANCEL);

        ComboBox<EmpleadoComboItem> cbEmpleado = new ComboBox<>(FXCollections.observableArrayList(empleados));
        cbEmpleado.getSelectionModel().selectFirst();
        cbEmpleado.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "CAMBIO_PUESTO", "AJUSTE_SALARIAL", "CAMBIO_DEPARTAMENTO", "PROMOCION"
        ));
        cbTipo.setValue("CAMBIO_PUESTO");
        cbTipo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbCampo = new ComboBox<>(FXCollections.observableArrayList(
                "Puesto", "Salario Base", "Departamento / Área", "Jornada Laboral"
        ));
        cbCampo.setValue("Puesto");
        cbCampo.setMaxWidth(Double.MAX_VALUE);

        TextField txtValorAnterior = new TextField();
        txtValorAnterior.setPromptText("Ej. Desarrollador Junior / Q 6,500.00 / TI");

        TextField txtValorNuevo = new TextField();
        txtValorNuevo.setPromptText("Ej. Supervisor Técnico / Q 8,500.00 / Gerencia");

        TextArea txtMotivo = new TextArea();
        txtMotivo.setPromptText("Escriba la justificación formal del cambio...");
        txtMotivo.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 20, 10, 20));

        grid.add(new Label("Colaborador:"), 0, 0);
        grid.add(cbEmpleado, 1, 0);
        grid.add(new Label("Tipo de Solicitud:"), 0, 1);
        grid.add(cbTipo, 1, 1);
        grid.add(new Label("Campo a Modificar:"), 0, 2);
        grid.add(cbCampo, 1, 2);
        grid.add(new Label("Dato Anterior:"), 0, 3);
        grid.add(txtValorAnterior, 1, 3);
        grid.add(new Label("Dato Solicitado (Nuevo):"), 0, 4);
        grid.add(txtValorNuevo, 1, 4);
        grid.add(new Label("Motivo / Justificación:"), 0, 5);
        grid.add(txtMotivo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultado = dialog.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnRegistrar) {
            EmpleadoComboItem empSel = cbEmpleado.getValue();
            String tipo = cbTipo.getValue();
            String campo = cbCampo.getValue();
            String ant = txtValorAnterior.getText().trim();
            String nue = txtValorNuevo.getText().trim();
            String mot = txtMotivo.getText().trim();

            if (empSel == null || nue.isEmpty() || mot.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos requeridos", "Debe seleccionar un colaborador e ingresar el dato nuevo y la justificación.");
                return;
            }

            guardarNuevaSolicitud(empSel.getId(), tipo, campo, ant, nue, mot);
        }
    }

    private List<EmpleadoComboItem> obtenerEmpleadosActivos() {
        new org.kinalrh.service.AutorizacionService().exigirPermiso("CAMBIO_CREAR");
        List<EmpleadoComboItem> lista = new ArrayList<>();
        String sql = "SELECT id_empleado, COALESCE(nombre_completo, CONCAT(COALESCE(primer_nombre, ''), ' ', COALESCE(primer_apellido, ''))) AS nombre " +
                     "FROM empleado ORDER BY nombre ASC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new EmpleadoComboItem(rs.getLong("id_empleado"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener empleados: " + e.getMessage());
        }
        return lista;
    }

    private void guardarNuevaSolicitud(Long idEmpleado, String tipo, String campo, String ant, String nue, String motivo) {
        Long idUser = new org.kinalrh.service.AutorizacionService().exigirPermiso("CAMBIO_CREAR").getIdUsuario();

        String sqlSolicitud = "INSERT INTO solicitud_cambio (id_empleado, tipo_cambio, motivo, estado, solicitado_por, fecha_solicitud) " +
                              "VALUES (?, ?, ?, 'PENDIENTE', ?, NOW())";
        String sqlDetalle = "INSERT INTO detalle_cambio (id_solicitud_cambio, entidad, campo, valor_anterior, valor_nuevo) " +
                            "VALUES (?, 'empleado', ?, ?, ?)";

        try (Connection conn = Conexion.getInstancia().conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtSol = conn.prepareStatement(sqlSolicitud, Statement.RETURN_GENERATED_KEYS)) {
                stmtSol.setLong(1, idEmpleado);
                stmtSol.setString(2, tipo);
                stmtSol.setString(3, motivo);
                stmtSol.setLong(4, idUser);
                stmtSol.executeUpdate();

                ResultSet rsKeys = stmtSol.getGeneratedKeys();
                if (rsKeys.next()) {
                    long idSol = rsKeys.getLong(1);
                    try (PreparedStatement stmtDet = conn.prepareStatement(sqlDetalle)) {
                        stmtDet.setLong(1, idSol);
                        stmtDet.setString(2, campo);
                        stmtDet.setString(3, ant.isEmpty() ? "-" : ant);
                        stmtDet.setString(4, nue);
                        stmtDet.executeUpdate();
                    }

                    conn.commit();

                    // Registrar auditoría
                    auditoriaService.registrarEvento(idUser, "SOLICITUD_CREADA", "solicitud_cambio", idSol, campo, ant, nue);

                    mostrarAlerta(Alert.AlertType.INFORMATION, "Solicitud Registrada",
                            "La solicitud #" + idSol + " fue ingresada exitosamente con estado PENDIENTE.\nYa está visible en la bandeja.");
                    cargarSolicitudes();
                } else {
                    conn.rollback();
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo generar el identificador de la solicitud.");
                }
            } catch (SQLException ex) {
                conn.rollback();
                mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", "Ocurrió un error al procesar la solicitud: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudo conectar a la base de datos: " + e.getMessage());
        }
    }

    @FXML
    public void eventoAprobar() {
        procesarResolucion("APROBADA");
    }

    @FXML
    public void eventoRechazar() {
        procesarResolucion("RECHAZADA");
    }

    private void procesarResolucion(String nuevoEstado) {
        new org.kinalrh.service.AutorizacionService().exigirAlgunPermiso("CAMBIO_APROBAR_RH", "CAMBIO_APROBAR_GERENCIA");
        SolicitudCambio seleccionada = tablaSolicitudes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Seleccione una solicitud de la lista.");
            return;
        }

        if (!"PENDIENTE".equalsIgnoreCase(seleccionada.getEstado())) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Solicitud ya resuelta", "Esta solicitud ya se encuentra " + seleccionada.getEstado() + ".");
            return;
        }

        String comentario = txtComentario.getText().trim();
        if (comentario.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Comentario requerido", "Ingrese un comentario antes de procesar la resolución.");
            return;
        }

        String sql = "UPDATE solicitud_cambio SET estado = ?, observaciones = ?, fecha_resolucion = NOW() WHERE id_solicitud_cambio = ?";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setString(2, comentario);
            stmt.setLong(3, seleccionada.getIdSolicitud());
            stmt.executeUpdate();

            // Auditoría
            Long idUser = SesionService.getInstance().getUsuarioAutenticado() != null ? SesionService.getInstance().getUsuarioAutenticado().getIdUsuario() : 1L;
            auditoriaService.registrarEvento(idUser, "SOLICITUD_" + nuevoEstado, "solicitud_cambio", seleccionada.getIdSolicitud(), null, null, null);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "La solicitud #" + seleccionada.getIdSolicitud() + " ha sido marcada como " + nuevoEstado + ".");
            txtComentario.clear();
            cargarSolicitudes();

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la solicitud: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
