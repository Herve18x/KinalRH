import os

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\Dashboard.fxml'
with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

import re
content = re.sub(r'text=".*?Colaboradores"', 'text="👥 Colaboradores"', content)
content = re.sub(r'text=".*?Cat.*?logos Generales"', 'text="📂 Catálogos Generales"', content)
content = re.sub(r'text=".*?Importar desde Excel"', 'text="📥 Importar desde Excel"', content)
content = re.sub(r'text=".*?Reportes Exportables"', 'text="📊 Reportes Exportables"', content)
content = re.sub(r'text=".*?Bandeja de Solicitudes"', 'text="📥 Bandeja de Solicitudes"', content)
content = re.sub(r'text=".*?Mi Equipo"', 'text="👥 Mi Equipo"', content)
content = re.sub(r'text=".*?Gesti.*?n de Usuarios"', 'text="👥 Gestión de Usuarios"', content)
content = re.sub(r'text=".*?Roles y Permisos"', 'text="🛡️ Roles y Permisos"', content)
content = re.sub(r'text=".*?Log de Auditor.*?"', 'text="📜 Log de Auditoría"', content)
content = re.sub(r'text="Cerrar Sesi.*?"', 'text="Cerrar Sesión"', content)
content = re.sub(r'text=".*?Directorio P.*?blico"', 'text="📸 Directorio Público"', content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

# EmpleadoFormController.java fixes
filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\EmpleadoFormController.java'
with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

content = re.sub(r'Validaci.*?n de formato', 'Validación de formato', content)
content = re.sub(r'd.*?gitos num.*?ricos', 'dígitos numéricos', content)
content = re.sub(r'v.*?lido', 'válido', content)
content = re.sub(r'Tel\. M.*?vil:', 'Tel. Móvil:', content)
content = re.sub(r'Direcci.*?n', 'Dirección', content)
content = re.sub(r'', '', content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

# FormularioEmpleado.fxml fixes
filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\FormularioEmpleado.fxml'
with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

content = re.sub(r'Tel\. M.*?vil:', 'Tel. Móvil:', content)
content = re.sub(r'Direcci.*?n', 'Dirección', content)
content = re.sub(r'', '', content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Files cleaned up!")