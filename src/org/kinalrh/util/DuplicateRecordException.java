package org.kinalrh.util;

/**
 * Excepción lanzada cuando se intenta registrar o actualizar una entidad
 * con valores únicos duplicados (por ejemplo, códigos o nombres únicos en catálogos).
 */
public class DuplicateRecordException extends RuntimeException {

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
