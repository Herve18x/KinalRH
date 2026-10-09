package org.kinalrh.dao;

import java.sql.SQLException;
import java.util.List;
import org.kinalrh.model.Logro;

public interface LogroDAO {
    List<Logro> listarTodos() throws SQLException;
    long insertar(Logro logro) throws SQLException;
    boolean actualizar(Logro logro) throws SQLException;
}