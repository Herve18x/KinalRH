import os
import glob

def fix_encoding(filepath):
    with open(filepath, 'rb') as f:
        content = f.read()
    
    # Intenta decodificar como utf-8, si falla, es que tiene otra cosa.
    # Pero el problema es que el archivo ahora está en UTF-8 pero contiene caracteres rotos que representan UTF-8 decodificado como ANSI (cp1252) y vuelto a guardar.
    text = content.decode('utf-8', errors='ignore')
    
    # Diccionario de reemplazos comunes de doble codificación
    replacements = {
        'Ã¡': 'á',
        'Ã©': 'é',
        'Ã­': 'í',
        'Ã³': 'ó',
        'Ãº': 'ú',
        'Ã±': 'ñ',
        'Ã ': 'Á',
        'Ã‰': 'É',
        'Ã“': 'Ó',
        'Ãš': 'Ú',
        'Ã‘': 'Ñ',
        'Â¿': '¿',
        'Â¡': '¡',
        'SesiÃ³n': 'Sesión',
        'Cerrar Sesin': 'Cerrar Sesión',
        'CatÃ¡logos': 'Catálogos',
        'GestÃ³n': 'Gestión',
        'GestiÃ³n': 'Gestión',
        'AuditorÃ-a': 'Auditoría',
        'AuditorÃa': 'Auditoría',
        'PÃºblico': 'Público',
        'Mǟvil': 'Móvil',
        'Validaciǟ''Ń?Tǟ?s''n': 'Validación',
        'dǟ''Ń?Tǟ?s''gitos': 'dígitos',
        'numǟ''Ń?Tǟ?s''ricos': 'numéricos',
        'vǟ''Ń?Tǟ?s''lido': 'válido',
        'Direcciǟ''Ń?Tǟ?s''n': 'Dirección',
        'ðŸ’¼': '💼',
        'ðŸ“’': '📓',
        'ðŸ“‚': '📂',
        'ðŸ“Š': '📊',
        'ðŸ“¥': '📥',
        'ðŸ‘¥': '👥',
        'ðŸ“·': '📷',
        'ðŸ”’': '🔒',
        'ðŸ›¡': '🛡️',
        'ðŸ“œ': '📜',
        'CatÃ¡logos': 'Catálogos',
        'PÃºblico': 'Público',
        'AuditorÃ-a': 'Auditoría',
        'AuditorÃa': 'Auditoría'
    }
    
    changed = False
    for bad, good in replacements.items():
        if bad in text:
            text = text.replace(bad, good)
            changed = True
            
    # Fix the extreme corruption manually if found
    if 'Validaci' in text and 'n' in text and not 'Validación' in text:
        text = text.replace('ValidaciÃƒÂ³n', 'Validación')
        text = text.replace('dÃƒÂ­gitos', 'dígitos')
        text = text.replace('numÃƒÂ©ricos', 'numéricos')
        text = text.replace('vÃƒÂ¡lido', 'válido')
        changed = True

    if changed:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(text)

files = glob.glob('src/**/*.java', recursive=True) + glob.glob('src/**/*.fxml', recursive=True)
for f in files:
    fix_encoding(f)

print("Encoding fixed!")