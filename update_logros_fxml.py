import os
import re

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\Logros.fxml'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure imports have ComboBox and DatePicker
if '<?import javafx.scene.control.ComboBox?>' not in content:
    content = content.replace('<?import javafx.scene.control.Button?>', '<?import javafx.scene.control.Button?>\n<?import javafx.scene.control.ComboBox?>\n<?import javafx.scene.control.DatePicker?>')

# Change columns
cols = '''<columns>
          <TableColumn fx:id="colEmpleado" prefWidth="250.0" text="Colaborador" />
          <TableColumn fx:id="colNombre" prefWidth="200.0" text="Logro/Certificación" />
          <TableColumn fx:id="colInstitucion" prefWidth="150.0" text="Institución" />
          <TableColumn fx:id="colFecha" prefWidth="120.0" text="Fecha Obtención" />
        </columns>'''
content = re.sub(r'<columns>.*?</columns>', cols, content, flags=re.DOTALL)

# Change TableView generic
content = content.replace('<TableView fx:id="tablaLogros"', '<TableView fx:id="tablaLogrosDTO"')

# Change formPane
form = '''<VBox fx:id="formPane" visible="false" managed="false" spacing="10">
           <Label text="Asignar Nuevo Logro" styleClass="subtitulo" />
           <HBox spacing="10" alignment="CENTER_LEFT">
               <Label text="Colaborador:" />
               <ComboBox fx:id="cmbEmpleado" prefWidth="200" />
               <Label text="Logro:" />
               <TextField fx:id="txtNombre" prefWidth="150" />
           </HBox>
           <HBox spacing="10" alignment="CENTER_LEFT">
               <Label text="Institución:" />
               <TextField fx:id="txtInstitucion" prefWidth="150" />
               <Label text="Fecha:" />
               <DatePicker fx:id="dpFecha" prefWidth="120" />
               <Button text="Guardar" onAction="#eventoGuardar" styleClass="btn-guardar" />
               <Button text="Cancelar" onAction="#eventoCancelar" />
           </HBox>
       </VBox>'''
content = re.sub(r'<VBox fx:id="formPane".*?</VBox>', form, content, flags=re.DOTALL)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Logros.fxml updated.")