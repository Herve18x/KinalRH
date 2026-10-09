import os

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\DashboardController.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

method = '''    @FXML
    public void abrirDirectorio() {
        System.out.println("Abrir directorio publico...");
    }

    @FXML
    public void abrirLogros() {
        cargarVista("/org/kinalrh/view/Logros.fxml");
    }'''

import re
content = re.sub(r'    @FXML\s*public void abrirDirectorio\(\) \{\s*System\.out\.println\("Abrir directorio publico\.\.\."\);\s*\}', method, content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("DashboardController.java updated")