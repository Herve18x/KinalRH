package org.kinalrh.dao;

import java.sql.SQLException;
import java.util.List;
import org.kinalrh.model.Logro;
import org.kinalrh.model.LogroEmpleadoDTO;

public interface LogroDAO {
    List<Logro> listarTodos() throws SQLException;
    List<Logro> listarPorEmpleado(long idEmpleado) throws SQLException;
    List<LogroEmpleadoDTO> listarLogrosConEmpleados() throws SQLException;
    List<LogroEmpleadoDTO> listarLogrosPorEmpleadoDTO(long idEmpleado) throws SQLException;
    long insertar(Logro logro) throws SQLException;
    boolean actualizar(Logro logro) throws SQLException;
    void asignarLogroAEmpleado(Long idLogro, Long idEmpleado) throws SQLException;
    void asignarLogroAEmpleado(Long idLogro, Long idEmpleado, boolean destacado, String observaciones) throws SQLException;
}