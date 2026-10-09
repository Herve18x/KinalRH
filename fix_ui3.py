import os
import re

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\FormularioEmpleado.fxml'
with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
    content = f.read()

content = re.sub(r'<Label text="Tel.*?M.*?vil:"', '<Label text="Tel. Móvil:"', content)
content = re.sub(r'<Label text="Direcci.*?n:"', '<Label text="Dirección:"', content)
content = re.sub(r'<Label text=".*?rea Principal:"', '<Label text="Área Principal:"', content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\EmpleadoFormController.java'
with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
    content = f.read()

content = re.sub(r'Validaci.*?n de formato de DPI \(13 d.*?gitos\)', 'Validación de formato de DPI (13 dígitos)', content)
content = re.sub(r'exactamente 13 d.*?gitos num.*?ricos\.', 'exactamente 13 dígitos numéricos.', content)
content = re.sub(r'Validaci.*?n de formato de correo \(b.*?sico\)', 'Validación de formato de correo (básico)', content)
content = re.sub(r'no es v.*?lido\.', 'no es válido.', content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("FormularioEmpleado fixed!")