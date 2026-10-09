import os

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\EmpleadoFormController.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Imports
content = content.replace('import org.kinalrh.model.Puesto;', '''import org.kinalrh.model.Puesto;
import org.kinalrh.model.NivelAcademico;
import org.kinalrh.dao.impl.NivelAcademicoDAOImpl;
import org.kinalrh.dao.impl.EmpleadoDAOImpl;''')

# Fields
content = content.replace('@FXML private ComboBox<EstadoEmpleado> cmbEstado;', '''@FXML private ComboBox<EstadoEmpleado> cmbEstado;
    @FXML private ComboBox<NivelAcademico> cmbNivelAcademico;
    @FXML private ComboBox<Empleado> cmbJefe;''')

# DAOs
content = content.replace('private EstadoEmpleadoDAOImpl estadoDAO = new EstadoEmpleadoDAOImpl();', '''private EstadoEmpleadoDAOImpl estadoDAO = new EstadoEmpleadoDAOImpl();
    private NivelAcademicoDAOImpl nivelAcademicoDAO = new NivelAcademicoDAOImpl();
    private EmpleadoDAOImpl empleadoDAO = new EmpleadoDAOImpl();''')

# Initializing lists
content = content.replace('cmbEstado.getItems().setAll(estadoDAO.listarTodos());', '''cmbEstado.getItems().setAll(estadoDAO.listarTodos());
        try {
            cmbNivelAcademico.getItems().setAll(nivelAcademicoDAO.listarTodos());
            cmbJefe.getItems().setAll(empleadoDAO.listarTodos());
        } catch (Exception ex) { ex.printStackTrace(); }''')

# Helpers
content = content.replace('private void seleccionarEstado(Long id)', '''private void seleccionarNivelAcademico(Long id) {
        if(id == null) return;
        for(NivelAcademico n : cmbNivelAcademico.getItems()) {
            if(n.getIdNivelAcademico().equals(id)) {
                cmbNivelAcademico.getSelectionModel().select(n);
                break;
            }
        }
    }
    private void seleccionarJefe(Long id) {
        if(id == null) return;
        for(Empleado e : cmbJefe.getItems()) {
            if(e.getIdEmpleado().equals(id)) {
                cmbJefe.getSelectionModel().select(e);
                break;
            }
        }
    }
    private void seleccionarEstado(Long id)''')

# Setting values in form
content = content.replace('seleccionarEstado(emp.getIdEstadoEmpleado());', '''seleccionarEstado(emp.getIdEstadoEmpleado());
            seleccionarNivelAcademico(emp.getIdNivelAcademico());
            seleccionarJefe(emp.getIdJefeInmediato());''')

# Saving values
content = content.replace('empleado.setIdEstadoEmpleado(cmbEstado.getValue().getIdEstadoEmpleado());', '''empleado.setIdEstadoEmpleado(cmbEstado.getValue().getIdEstadoEmpleado());
        empleado.setIdNivelAcademico(cmbNivelAcademico.getValue() != null ? cmbNivelAcademico.getValue().getIdNivelAcademico() : null);
        empleado.setIdJefeInmediato(cmbJefe.getValue() != null ? cmbJefe.getValue().getIdEmpleado() : null);''')

# Fix hardcoded ADMIN to role (T2.14)
content = content.replace('empleadoService.guardarEmpleado(empleado, "ADMIN");', 'empleadoService.guardarEmpleado(empleado, org.kinalrh.service.SesionService.getInstance().getUsuarioAutenticado() != null ? org.kinalrh.service.SesionService.getInstance().getUsuarioAutenticado().getRol() : "");')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("EmpleadoFormController features restored!")