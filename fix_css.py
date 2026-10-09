import os
import re

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\Logros.fxml'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('stylesheets="@../style/estilos.css"', 'stylesheets="@styles.css"')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Logros.fxml CSS path fixed!")