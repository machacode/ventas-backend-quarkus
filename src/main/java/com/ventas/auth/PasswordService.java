package com.ventas.auth;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class PasswordService {

    // Encriptar contraseña
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verificar contraseña contra su hash
    public boolean verify(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}