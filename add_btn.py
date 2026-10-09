import os

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\Dashboard.fxml'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

replacement = '''<VBox fx:id="seccionVisor" spacing="5.0">
                     <Label styleClass="nav-section-title" text="IMAGEN INSTITUCIONAL" />
                     <Button maxWidth="Infinity" onAction="#abrirDirectorio" styleClass="nav-button" text="📸 Directorio Público" />
                     <Button maxWidth="Infinity" onAction="#abrirLogros" styleClass="nav-button" text="🌟 Logros y Reconocimientos" />
                  </VBox>'''

import re
content = re.sub(r'<VBox fx:id="seccionVisor".*?</VBox>', replacement, content, flags=re.DOTALL)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Dashboard.fxml updated")