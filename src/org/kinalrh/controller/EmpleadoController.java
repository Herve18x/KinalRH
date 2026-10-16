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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.kinalrh.model.Area;
import org.kinalrh.model.Puesto;
import org.kinalrh.model.NivelAcademico;
import org.kinalrh.model.EstadoEmpleado;
import org.kinalrh.dao.impl.AreaDAOImpl;
import org.kinalrh.dao.impl.PuestoDAOImpl;
import org.kinalrh.dao.impl.NivelAcademicoDAOImpl;
import org.kinalrh.dao.impl.EstadoEmpleadoDAOImpl;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;
import org.kinalrh.model.Empleado;
import org.kinalrh.service.EmpleadoService;

public class EmpleadoController implements Initializable {

    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, Long> colId;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colEstado;
    @FXML private TableColumn<Empleado, String> colArea;
    @FXML private TableColumn<Empleado, String> colPuesto;

    @FXML private javafx.scene.control.Label lblTitulo;
    @FXML private Button btnNuevo;
    @FXML private Button btnRefrescar;

    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<Area> cmbFiltroArea;
    @FXML private ComboBox<Puesto> cmbFiltroPuesto;
    @FXML private ComboBox<NivelAcademico> cmbFiltroNivel;
    @FXML private ComboBox<EstadoEmpleado> cmbFiltroEstado;
    @FXML private ComboBox<Empleado> cmbFiltroJefe;
    
    private ObservableList<Empleado> masterData = FXCollections.observableArrayList();
    private FilteredList<Empleado> filteredData;
    private EmpleadoService empleadoService;
    private Long idSeleccionadoGuardado = null;
    private boolean modoMiEquipo = false;
    private Long idEmpleadoJefe = null;
    private String rolUsuarioActual = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empleadoService = new EmpleadoService();
        configurarTabla();
        cargarCatalogosFiltros();
        configurarFiltros();
        cargarDatos();
    }
    
    private void cargarCatalogosFiltros() {
        new org.kinalrh.service.AutorizacionService().exigirPermiso("EMPLEADO_VER");
        try {
            cmbFiltroArea.getItems().setAll(new AreaDAOImpl().listarActivas());
            cmbFiltroPuesto.getItems().setAll(new PuestoDAOImpl().listarActivos());
            cmbFiltroNivel.getItems().setAll(new NivelAcademicoDAOImpl().listarTodos());
            cmbFiltroEstado.getItems().setAll(new EstadoEmpleadoDAOImpl().listarTodos());
            cmbFiltroJefe.getItems().setAll(new EmpleadoDAOImpl().listarTodos());
        } catch(Exception e) { e.printStackTrace(); }
    }
    
    private void configurarFiltros() {
        filteredData = new FilteredList<>(masterData, p -> true);
        
        txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cmbFiltroArea.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cmbFiltroPuesto.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cmbFiltroNivel.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cmbFiltroJefe.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        
        SortedList<Empleado> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaEmpleados.comparatorProperty());
        tablaEmpleados.setItems(sortedData);
    }
    
    public void configurarModoMiEquipo(Long idEmpleadoJefe, String rolActual) {
        this.modoMiEquipo = true;
        this.idEmpleadoJefe = idEmpleadoJefe;
        this.rolUsuarioActual = rolActual != null ? rolActual.toUpperCase() : "";
        
        if (lblTitulo != null) {
            lblTitulo.setText("Mi Equipo de Trabajo (Supervisión y Colaboradores a Cargo)");
        }
        if (btnNuevo != null) {
            btnNuevo.setVisible(false);
            btnNuevo.setManaged(false);
        }
        
        if (idEmpleadoJefe != null && cmbFiltroJefe != null) {
            for (Empleado j : cmbFiltroJefe.getItems()) {
                if (j.getIdEmpleado() != null && j.getIdEmpleado().equals(idEmpleadoJefe)) {
                    cmbFiltroJefe.getSelectionModel().select(j);
                    break;
                }
            }
        }
        
        if (tablaEmpleados != null) {
            tablaEmpleados.setPlaceholder(new javafx.scene.control.Label("No tiene colaboradores bajo su supervisión directa actualmente."));
        }
        
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        filteredData.setPredicate(emp -> {
            // En modo "Mi Equipo": filtrar por los subordinados del usuario actual si no es ADMIN
            if (modoMiEquipo && idEmpleadoJefe != null && !"ADMIN".equals(rolUsuarioActual)) {
                if (emp.getIdJefeInmediato() == null || !emp.getIdJefeInmediato().equals(idEmpleadoJefe)) {
                    return false;
                }
            }

            // 1. Busqueda (Nombre, DPI, NIT, Codigo)
            String busqueda = txtBusqueda.getText() == null ? "" : txtBusqueda.getText().toLowerCase();
            boolean matchBusqueda = busqueda.isEmpty() 
                || (emp.getNombre() != null && emp.getNombre().toLowerCase().contains(busqueda))
                || (emp.getDpi() != null && emp.getDpi().toLowerCase().contains(busqueda))
                || (emp.getNit() != null && emp.getNit().toLowerCase().contains(busqueda))
                || (emp.getCodigoEmpleado() != null && emp.getCodigoEmpleado().toLowerCase().contains(busqueda));
            
            // 2. Filtro Area
            boolean matchArea = cmbFiltroArea.getValue() == null || (emp.getIdAreaPrincipal() != null && emp.getIdAreaPrincipal().equals(cmbFiltroArea.getValue().getIdArea()));
            
            // 3. Filtro Puesto
            boolean matchPuesto = cmbFiltroPuesto.getValue() == null || (emp.getIdPuestoActual() != null && emp.getIdPuestoActual().equals(cmbFiltroPuesto.getValue().getIdPuesto()));
            
            // 4. Filtro Nivel
            boolean matchNivel = cmbFiltroNivel.getValue() == null || (emp.getIdNivelAcademico() != null && emp.getIdNivelAcademico().equals(cmbFiltroNivel.getValue().getIdNivelAcademico()));
            
            // 5. Filtro Estado
            boolean matchEstado = cmbFiltroEstado.getValue() == null || (emp.getIdEstadoEmpleado() != null && emp.getIdEstadoEmpleado().equals(cmbFiltroEstado.getValue().getIdEstadoEmpleado()));
            
            // 6. Filtro Jefe
            boolean matchJefe = cmbFiltroJefe.getValue() == null || (emp.getIdJefeInmediato() != null && emp.getIdJefeInmediato().equals(cmbFiltroJefe.getValue().getIdEmpleado()));
            
            return matchBusqueda && matchArea && matchPuesto && matchNivel && matchEstado && matchJefe;
        });
    }
    
    @FXML
    public void eventoLimpiarFiltros() {
        txtBusqueda.clear();
        cmbFiltroArea.getSelectionModel().clearSelection();
        cmbFiltroPuesto.getSelectionModel().clearSelection();
        cmbFiltroNivel.getSelectionModel().clearSelection();
        cmbFiltroEstado.getSelectionModel().clearSelection();
        cmbFiltroJefe.getSelectionModel().clearSelection();
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

        List<Empleado> empleados = empleadoService.listarEmpleados(org.kinalrh.service.SesionService.getInstance().getUsuarioAutenticado() != null ? org.kinalrh.service.SesionService.getInstance().getUsuarioAutenticado().getRol() : "");
        masterData.setAll(empleados);

        // Restaurar seleccion
        if (idSeleccionadoGuardado != null) {
            for (Empleado e : tablaEmpleados.getItems()) {
                if (e.getIdEmpleado().equals(idSeleccionadoGuardado)) {
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
        abrirFichaEmpleado(0L);
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

    private void abrirFichaEmpleado(Long idEmpleado) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/kinalrh/view/FormularioEmpleado.fxml"));
            javafx.scene.Parent root = loader.load();
            
            EmpleadoFormController controller = loader.getController();
            Empleado emp = idEmpleado == 0L ? null : new EmpleadoService().buscarPorId(idEmpleado, org.kinalrh.service.SesionService.getInstance().getUsuarioAutenticado() != null ? org.kinalrh.service.SesionService.getInstance().getUsuarioAutenticado().getRol() : "");
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            controller.setStage(stage);
            controller.setEmpleado(emp);
            
            stage.setTitle(idEmpleado == 0L ? "Nuevo Empleado" : "Editar Empleado");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            cargarDatos(); // Refrescar despus de cerrar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
