import os
filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\LogroController.java'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the method with a try-catch block
bad_method = '''    private void cargarEmpleados() {
        cmbEmpleado.getItems().setAll(empleadoDAO.listarTodos());
    }'''

good_method = '''    private void cargarEmpleados() {
        try {
            cmbEmpleado.getItems().setAll(empleadoDAO.listarTodos());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }'''

content = content.replace(bad_method, good_method)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed Exception handling in LogroController.java")