import os

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\EmpleadoController.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('tablaEmpleados.getItems().setAll(empleados);', 'masterData.setAll(empleados);')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed List modification bug in EmpleadoController")