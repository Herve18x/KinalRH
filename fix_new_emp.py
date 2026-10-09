import os
filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\EmpleadoFormController.java'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the beginning of setEmpleado
old_set = '''    public void setEmpleado(Empleado emp) {
        this.empleado = emp;
        if (emp != null && emp.getIdEmpleado() != null && emp.getIdEmpleado() > 0) {'''
new_set = '''    public void setEmpleado(Empleado emp) {
        if (emp == null) {
            this.empleado = new Empleado();
        } else {
            this.empleado = emp;
        }
        
        if (emp != null && emp.getIdEmpleado() != null && emp.getIdEmpleado() > 0) {'''

content = content.replace(old_set, new_set)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed Empleado initialization for new employees")