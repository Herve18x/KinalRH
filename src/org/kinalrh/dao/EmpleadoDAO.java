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
}