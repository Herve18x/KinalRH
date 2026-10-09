import os
import re

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\Login.fxml'
with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Add Image imports if not present
if 'javafx.scene.image.ImageView' not in content:
    content = content.replace('<?import javafx.scene.shape.Circle?>', '<?import javafx.scene.shape.Circle?>\n<?import javafx.scene.image.ImageView?>\n<?import javafx.scene.image.Image?>')

# Fix encodings in labels
content = content.replace('Iniciar Sesin', 'Iniciar Sesión')
content = content.replace('Contrasea', 'Contraseña')
content = content.replace('contrasea', 'contraseña')
content = content.replace('fundacin', 'fundación')

# Replace branding section
old_branding = '''<StackPane prefHeight="130.0" prefWidth="130.0">
               <children>
                  <Circle radius="60.0" styleClass="logo-circle" />
                  <Label text="RH" styleClass="logo-text" />
               </children>
               <VBox.margin><Insets bottom="30.0" /></VBox.margin>
            </StackPane>
            <Label styleClass="title-light" text="fundacin" />
            <Label styleClass="title-bold"  text="Kinal RH" />'''

# Since we already fixed "fundacin" to "fundación" above in memory, let's match the fixed version or use regex.
content = re.sub(r'<StackPane.*?</StackPane>\s*<Label styleClass="title-light".*?/>\s*<Label styleClass="title-bold".*?/>',
                 '''<ImageView fitHeight="200.0" preserveRatio="true">
               <image>
                  <Image url="@../image/logo_kinal.png" />
               </image>
            </ImageView>''', content, flags=re.DOTALL)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Login.fxml updated with the actual logo!")