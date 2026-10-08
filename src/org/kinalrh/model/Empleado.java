package org.kinalrh.dao;
 
import java.sql.SQLException;
import java.util.Optional;
import org.kinalrh.model.Empleado;
 
/**
* Interfaz para las operaciones de acceso a datos de Empleado.
*
* @author danielcatalan
*/
public interface EmpleadoDAO {
 
    /**
     * Devuelve el empleado o Optional.empty() si no existe.
     */
    Optional<Empleado> buscarPorId(long idEmpleado) throws SQLException;
 
    /**
     * Inserta el empleado y devuelve el ID generado por la base de datos.
     * Si falla la inserción o no se obtiene el ID, lanza SQLException.
     */
    long insertar(Empleado empleado) throws SQLException;
 
    /**
     * Actualiza usando empleado.getIdEmpleado(). Devuelve true si el registro
     * existe y la operación tiene éxito, incluso si los datos eran iguales.
     * Devuelve false si el ID no existe. Los errores SQL se propagan.
     */
    boolean actualizar(Empleado empleado) throws SQLException;
}
