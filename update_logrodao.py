import os
filepath = r'C:\danielmendia\KinalRH\src\org\kinalrh\dao\impl\LogroDAOImpl.java'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add the new method
new_methods = '''
    public List<org.kinalrh.model.LogroEmpleadoDTO> listarLogrosConEmpleados() throws SQLException {
        List<org.kinalrh.model.LogroEmpleadoDTO> lista = new ArrayList<>();
        String sql = "SELECT l.id_logro, e.primer_nombre, e.primer_apellido, l.nombre AS logro, l.institucion, l.fecha_obtencion " +
                     "FROM logro l " +
                     "JOIN empleado_logro el ON l.id_logro = el.id_logro " +
                     "JOIN empleado e ON el.id_empleado = e.id_empleado " +
                     "WHERE l.activo = 1 " +
                     "ORDER BY l.id_logro DESC";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String empName = rs.getString("primer_nombre") + " " + (rs.getString("primer_apellido") != null ? rs.getString("primer_apellido") : "");
                java.time.LocalDate fecha = rs.getDate("fecha_obtencion") != null ? rs.getDate("fecha_obtencion").toLocalDate() : null;
                lista.add(new org.kinalrh.model.LogroEmpleadoDTO(
                    rs.getLong("id_logro"),
                    empName.trim(),
                    rs.getString("logro"),
                    rs.getString("institucion"),
                    fecha
                ));
            }
        }
        return lista;
    }

    public void asignarLogroAEmpleado(Long idLogro, Long idEmpleado) throws SQLException {
        String sql = "INSERT INTO empleado_logro (id_empleado, id_logro, destacado) VALUES (?, ?, 0) ON DUPLICATE KEY UPDATE id_empleado=id_empleado";
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idEmpleado);
            stmt.setLong(2, idLogro);
            stmt.executeUpdate();
        }
    }
'''

content = content.replace('return stmt.executeUpdate() > 0;\n        }\n    }', 'return stmt.executeUpdate() > 0;\n        }\n    }\n' + new_methods)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("LogroDAOImpl updated.")