import os
import re

filepath = r'C:\danielmendia\KinalRH\nbproject\project.properties'
with open(filepath, 'rb') as f:
    content = f.read()

content_str = content.decode('utf-8', errors='ignore')

content_str = content_str.replace('javafx-sdk-21.0.11', 'javafx-sdk-21.0.12')

# Remove duplicate annotation lines at the top with BOM
content_str = re.sub(r'annotation.processing.enabled=true\njavadoc.version=false\n\xef\xbb\xbfannotation.processing.enabled=true', 'annotation.processing.enabled=true', content_str)
content_str = re.sub(r'annotation.processing.enabled=true\njavadoc.version=false\n\ufeffannotation.processing.enabled=true', 'annotation.processing.enabled=true', content_str)
content_str = re.sub(r'annotation.processing.enabled=true\njavadoc.version=false\n\?annotation.processing.enabled=true', 'annotation.processing.enabled=true', content_str)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content_str)

print("project.properties restored to 21.0.12 and cleaned")