import os

filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\controller\EmpleadoFormController.java'
with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Fix the garbage from 435a0f5
content = content.replace('ValidaciÃƒÂ³n', 'Validación')
content = content.replace('dÃƒÂ­gitos', 'dígitos')
content = content.replace('numÃƒÂ©ricos', 'numéricos')
content = content.replace('vÃƒÂ¡lido', 'válido')
content = content.replace('MÃƒÂ³vil', 'Móvil')
content = content.replace('DirecciÃƒÂ³n', 'Dirección')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Cleaned up!")