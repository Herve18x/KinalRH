package org.kinalrh.util;

import java.util.UUID;
import org.kinalrh.model.Usuario;

public class SessionManager {
    private static SessionManager instance;
    private Usuario currentUser;
    private String sessionUUID;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Usuario user) {
        this.currentUser = user;
        this.sessionUUID = UUID.randomUUID().toString();
    }

    public void logout() {
        this.currentUser = null;
        this.sessionUUID = null;
    }

    public Usuario getCurrentUser() {
        return currentUser;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }
}