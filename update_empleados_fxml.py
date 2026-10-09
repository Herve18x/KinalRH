import os
import re

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\Empleados.fxml'
with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Add imports for TextField and ComboBox
if '<?import javafx.scene.control.TextField?>' not in content:
    content = content.replace('<?import javafx.scene.control.TableView?>', '<?import javafx.scene.control.TableView?>\n<?import javafx.scene.control.TextField?>\n<?import javafx.scene.control.ComboBox?>')

# Fix text encodings
content = content.replace('Gestin', 'Gestión')
content = content.replace('?rea', 'Área')

# Create the search and filter UI
filters_ui = '''   <VBox spacing="10.0" style="-fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #f9f9f9;">
      <HBox spacing="10.0" alignment="CENTER_LEFT">
         <Label text="Búsqueda:" style="-fx-font-weight: bold;" />
         <TextField fx:id="txtBusqueda" promptText="Nombre, DPI, NIT, Código..." prefWidth="300" />
         <Button fx:id="btnLimpiarFiltros" onAction="#eventoLimpiarFiltros" text="Limpiar Filtros" />
      </HBox>
      <HBox spacing="10.0" alignment="CENTER_LEFT">
         <Label text="Filtros:" style="-fx-font-weight: bold;" />
         <ComboBox fx:id="cmbFiltroArea" promptText="Área" prefWidth="150" />
         <ComboBox fx:id="cmbFiltroPuesto" promptText="Puesto" prefWidth="150" />
         <ComboBox fx:id="cmbFiltroNivel" promptText="Nivel Académico" prefWidth="150" />
         <ComboBox fx:id="cmbFiltroEstado" promptText="Estado" prefWidth="150" />
         <ComboBox fx:id="cmbFiltroJefe" promptText="Jefe Inmediato" prefWidth="150" />
      </HBox>
   </VBox>
   
   <HBox spacing="10.0">'''

content = content.replace('   <HBox spacing="10.0">', filters_ui)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Empleados.fxml updated with Search and Filters")