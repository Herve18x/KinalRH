import os
import re

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\view\Dashboard.fxml'
with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
    content = f.read()

# Replace VBox seccionRH
content = re.sub(r'<VBox fx:id="seccionRH".*?</VBox>', '''<VBox fx:id="seccionRH" spacing="5.0">
                     <Label styleClass="nav-section-title" text="RECURSOS HUMANOS" />
                     <Button maxWidth="Infinity" onAction="#abrirColaboradores" styleClass="nav-button" text="💼 Colaboradores" />
                     <Button maxWidth="Infinity" onAction="#abrirCatalogos" styleClass="nav-button" text="📂 Catálogos Generales" />
                     <Button maxWidth="Infinity" onAction="#abrirImportacion" styleClass="nav-button" text="📥 Importar desde Excel" />
                     <Button maxWidth="Infinity" onAction="#abrirReportes" styleClass="nav-button" text="📊 Reportes Exportables" />
                  </VBox>''', content, flags=re.DOTALL)

# Replace VBox seccionGerencia
content = re.sub(r'<VBox fx:id="seccionGerencia".*?</VBox>', '''<VBox fx:id="seccionGerencia" spacing="5.0">
                     <Label styleClass="nav-section-title" text="GESTIÓN Y APROBACIONES" />
                     <Button maxWidth="Infinity" onAction="#abrirBandeja" styleClass="nav-button" text="📥 Bandeja de Solicitudes" />
                     <Button maxWidth="Infinity" onAction="#abrirMiEquipo" styleClass="nav-button" text="👥 Mi Equipo" />
                  </VBox>''', content, flags=re.DOTALL)

# Replace VBox seccionVisor
content = re.sub(r'<VBox fx:id="seccionVisor".*?</VBox>', '''<VBox fx:id="seccionVisor" spacing="5.0">
                     <Label styleClass="nav-section-title" text="IMAGEN INSTITUCIONAL" />
                     <Button maxWidth="Infinity" onAction="#abrirDirectorio" styleClass="nav-button" text="📸 Directorio Público" />
                     <Button maxWidth="Infinity" onAction="#abrirLogros" styleClass="nav-button" text="🌟 Logros y Reconocimientos" />
                  </VBox>''', content, flags=re.DOTALL)

# Replace VBox seccionAdmin
content = re.sub(r'<VBox fx:id="seccionAdmin".*?</VBox>', '''<VBox fx:id="seccionAdmin" spacing="5.0">
                     <Label styleClass="nav-section-title" text="SEGURIDAD Y SISTEMA" />
                     <Button maxWidth="Infinity" onAction="#abrirUsuarios" styleClass="nav-button" text="👥 Gestión de Usuarios" />
                     <Button maxWidth="Infinity" onAction="#abrirRoles" styleClass="nav-button" text="🛡️ Roles y Permisos" />
                     <Button maxWidth="Infinity" onAction="#abrirAuditoria" styleClass="nav-button" text="📜 Log de Auditoría" />
                  </VBox>''', content, flags=re.DOTALL)

content = re.sub(r'text="Cerrar Sesi.*?"', 'text="Cerrar Sesión"', content)
content = re.sub(r'text="Seleccione una opci.*?n del men.*? para comenzar."', 'text="Seleccione una opción del menú para comenzar."', content)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Dashboard completely rewritten!")