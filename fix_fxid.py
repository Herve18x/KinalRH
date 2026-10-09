import os
import re

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\FormularioEmpleado.fxml'
with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Replace any broken fx:id for txtDireccion
content = re.sub(r'fx:id="txtDirecc.*?"', 'fx:id="txtDireccion"', content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed fx:id in FXML")