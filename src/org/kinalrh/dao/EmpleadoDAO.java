package org.kinalrh.dao;
 
import java.sql.SQLException;
import java.util.Optional;
import org.kinalrh.model.Empleado;
 
public interface EmpleadoDAO {
    Optional<Empleado> buscarPorId(long idEmpleado) throws SQLException;
    long insertar(Empleado empleado) throws SQLException;
    boolean actualizar(Empleado empleado) throws SQLException;
}