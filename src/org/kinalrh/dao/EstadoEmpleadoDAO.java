package org.kinalrh.dao;

import java.util.List;
import org.kinalrh.model.EstadoEmpleado;

public interface EstadoEmpleadoDAO {
    List<EstadoEmpleado> listarTodos();
    EstadoEmpleado buscarPorId(Long id);
    void guardar(EstadoEmpleado estado);
    void actualizar(EstadoEmpleado estado);
    void cambiarEstadoLogico(Long id, boolean activo);
}