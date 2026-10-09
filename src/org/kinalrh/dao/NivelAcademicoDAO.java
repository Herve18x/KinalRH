package org.kinalrh.dao;
import java.sql.SQLException;
import java.util.List;
import org.kinalrh.model.NivelAcademico;

public interface NivelAcademicoDAO {
    List<NivelAcademico> listarTodos() throws SQLException;
}