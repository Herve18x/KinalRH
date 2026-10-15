package org.kinalrh.dao;

import java.sql.SQLException;
import java.util.List;
import org.kinalrh.model.NivelAcademico;

/**
 * Interfaz DAO para gestión del catálogo nivel_academico.
 * T3.09 Lectura/gestión básica de nivel académico y tipo de logro.
 * T3.12 Verificar que solo se guarda un nivel válido y activo.
 */
public interface NivelAcademicoDAO {

    List<NivelAcademico> listarTodos() throws SQLException;

    List<NivelAcademico> listarActivos();

    NivelAcademico buscarPorId(Long id);

    long insertar(NivelAcademico nivel) throws SQLException;

    void actualizar(NivelAcademico nivel) throws SQLException;

    void cambiarEstado(Long id, boolean activo) throws SQLException;

    boolean esNivelValidoYActivo(Long id);

    boolean existeCodigo(String codigo, Long idExcluido);

    boolean existeNombre(String nombre, Long idExcluido);
}