package org.kinalrh.controller;

import org.kinalrh.model.Usuario;

/**
 * Interfaz base que deben implementar todos los controladores de Dashboard.
 * Permite pasar el usuario autenticado desde el Login al panel correspondiente.
 * Mismo patrón que en PaginaLibreAPP.
 */
public interface BaseDashboardController {
    /**
     * Recibe el usuario ya autenticado y configura la vista según su rol y permisos.
     *
     * @param usuario El usuario que inició sesión correctamente.
     */
    void iniciarUsuario(Usuario usuario);
}
