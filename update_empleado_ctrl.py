import os
filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\EmpleadoController.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
imports = '''import javafx.collections.FXCollections;
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
import org.kinalrh.dao.impl.EmpleadoDAOImpl;'''
content = content.replace('import org.kinalrh.model.Empleado;', imports + '\nimport org.kinalrh.model.Empleado;')

# Add fields
fields = '''    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<Area> cmbFiltroArea;
    @FXML private ComboBox<Puesto> cmbFiltroPuesto;
    @FXML private ComboBox<NivelAcademico> cmbFiltroNivel;
    @FXML private ComboBox<EstadoEmpleado> cmbFiltroEstado;
    @FXML private ComboBox<Empleado> cmbFiltroJefe;
    
    private ObservableList<Empleado> masterData = FXCollections.observableArrayList();
    private FilteredList<Empleado> filteredData;'''
content = content.replace('private EmpleadoService empleadoService;', fields + '\n    private EmpleadoService empleadoService;')

# Update initialize
init_old = '''    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empleadoService = new EmpleadoService();
        configurarTabla();
        cargarDatos();
    }'''
init_new = '''    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empleadoService = new EmpleadoService();
        configurarTabla();
        cargarCatalogosFiltros();
        configurarFiltros();
        cargarDatos();
    }
    
    private void cargarCatalogosFiltros() {
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
    
    private void aplicarFiltros() {
        filteredData.setPredicate(emp -> {
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
    }'''
content = content.replace(init_old, init_new)

# Update cargarDatos
cargar_old = '''    public void cargarDatos() {
        try {
            java.util.List<Empleado> lista = empleadoService.listarEmpleados();
            tablaEmpleados.getItems().setAll(lista);
            
            if (idSeleccionadoGuardado != null) {
                for (Empleado e : tablaEmpleados.getItems()) {
                    if (e.getIdEmpleado().equals(idSeleccionadoGuardado)) {
                        tablaEmpleados.getSelectionModel().select(e);
                        tablaEmpleados.scrollTo(e);
                        break;
                    }
                }
                idSeleccionadoGuardado = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }'''
cargar_new = '''    public void cargarDatos() {
        try {
            java.util.List<Empleado> lista = empleadoService.listarEmpleados();
            masterData.setAll(lista);
            
            if (idSeleccionadoGuardado != null) {
                for (Empleado e : tablaEmpleados.getItems()) {
                    if (e.getIdEmpleado().equals(idSeleccionadoGuardado)) {
                        tablaEmpleados.getSelectionModel().select(e);
                        tablaEmpleados.scrollTo(e);
                        break;
                    }
                }
                idSeleccionadoGuardado = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }'''
content = content.replace(cargar_old, cargar_new)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("EmpleadoController updated!")