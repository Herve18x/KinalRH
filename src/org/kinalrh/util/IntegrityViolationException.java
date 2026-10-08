package org.kinalrh.util;

/**
 * Excepción lanzada cuando una operación viola la integridad referencial del sistema,
 * por ejemplo al intentar eliminar físicamente un catálogo que posee colaboradores vinculados.
 */
public class IntegrityViolationException extends RuntimeException {

    public IntegrityViolationException(String message) {
        super(message);
    }

    public IntegrityViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
