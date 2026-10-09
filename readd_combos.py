import os
import re

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\FormularioEmpleado.fxml'
with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Fix existing encodings while we're here
content = content.replace('Tel. Mvil:', 'Tel. Móvil:')
content = content.replace('Direccin:', 'Dirección:')
content = content.replace('?rea Principal:', 'Área Principal:')
content = content.replace('Validacin', 'Validación')
content = content.replace('dgitos', 'dígitos')

# Add Nivel Academico
if 'cmbNivelAcademico' not in content:
    personal_insertion = '''<Label text="Correo Personal:" GridPane.rowIndex="4" />
                        <TextField fx:id="txtCorreo" GridPane.columnIndex="1" GridPane.rowIndex="4" />
                        <Label text="Nivel Académico:" GridPane.rowIndex="5" />
                        <ComboBox fx:id="cmbNivelAcademico" prefWidth="2000" GridPane.columnIndex="1" GridPane.rowIndex="5" />'''
    content = re.sub(r'<Label text="Correo Personal:" GridPane\.rowIndex="4" />\s*<TextField fx:id="txtCorreo" GridPane\.columnIndex="1" GridPane\.rowIndex="4" />', personal_insertion, content)

# Add Jefe Inmediato
if 'cmbJefe' not in content:
    institucional_insertion = '''<Label text="Estado (*):" GridPane.rowIndex="3" />
                        <ComboBox fx:id="cmbEstado" prefWidth="2000" GridPane.columnIndex="1" GridPane.rowIndex="3" />
                        <Label text="Jefe Inmediato:" GridPane.rowIndex="4" />
                        <ComboBox fx:id="cmbJefe" prefWidth="2000" GridPane.columnIndex="1" GridPane.rowIndex="4" />'''
    content = re.sub(r'<Label text="Estado \(\*\):" GridPane\.rowIndex="3" />\s*<ComboBox fx:id="cmbEstado" prefWidth="2000" GridPane\.columnIndex="1" GridPane\.rowIndex="3" />', institucional_insertion, content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("FormularioEmpleado.fxml updated with ComboBoxes!")