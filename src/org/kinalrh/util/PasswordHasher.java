package org.kinalrh.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256; // 32 bytes = 256 bits

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return ALGORITHM + ":" + ITERATIONS + ":" + saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHashFormat) {
        if (storedHashFormat == null || !storedHashFormat.contains(":")) {
            // Compatibilidad con contraseñas antiguas (SHA-256 plano de T2.01) si es necesario
            // pero T1.12 dice verificar sin comparar texto plano y solo hashes.
            // Para cumplir estrictamente, asumimos el formato correcto.
            return false;
        }

        try {
            String[] parts = storedHashFormat.split(":");
            if (parts.length != 4) return false;

            String algorithm = parts[0];
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            String hashBase64 = parts[3];

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            byte[] testHash = skf.generateSecret(spec).getEncoded();
            String testHashBase64 = Base64.getEncoder().encodeToString(testHash);

            // Evitar ataques de tiempo
            return java.security.MessageDigest.isEqual(testHashBase64.getBytes(), hashBase64.getBytes());
        } catch (Exception e) {
            return false;
        }
    }
}