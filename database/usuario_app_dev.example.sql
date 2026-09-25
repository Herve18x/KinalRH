-- ============================================================
-- Plantilla de creación de usuario MySQL con privilegios limitados
-- Reemplazar 'tu_password_aqui' localmente. NUNCA subir contraseñas reales.
-- ============================================================

CREATE USER IF NOT EXISTS 'kinalrh_app'@'localhost' IDENTIFIED BY 'tu_password_aqui';

GRANT SELECT, INSERT, UPDATE ON IN4CM.* TO 'kinalrh_app'@'localhost';

GRANT DELETE ON IN4CM.usuario_rol TO 'kinalrh_app'@'localhost';
GRANT DELETE ON IN4CM.empleado_area TO 'kinalrh_app'@'localhost';

FLUSH PRIVILEGES;
