package org.kinalrh.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.kinalrh.model.Empleado;

public interface EmpleadoDAO {
    List<Empleado> listarTodos() throws SQLException;
    Optional<Empleado> buscarPorId(long idEmpleado) throws SQLException;
    long insertar(Empleado empleado) throws SQLException;
    boolean actualizar(Empleado empleado) throws SQLException;
    
    // T2.09
    boolean existeDpi(String dpi, Long idExcluido);
    boolean existeNit(String nit, Long idExcluido);
    boolean existeCorreoPersonal(String correo, Long idExcluido);
    boolean existeReferencia(String tabla, String pkColumna, Long id);
}