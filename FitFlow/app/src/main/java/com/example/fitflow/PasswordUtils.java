package com.example.fitflow;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    // En una aplicación real, considera usar algoritmos más robustos como bcrypt o SCrypt
    // y siempre usa "salts" (sales) para cada contraseña.
    // Este es un ejemplo simplificado.
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            // Manejar la excepción (esto no debería suceder con SHA-256)
            e.printStackTrace();
            return null; // O lanzar una RuntimeException
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Método para verificar la contraseña (simplificado)
    // Compara el hash de la contraseña ingresada con el hash almacenado.
    public static boolean verifyPassword(String enteredPassword, String storedHash) {
        if (enteredPassword == null || storedHash == null) {
            return false;
        }
        String hashedEnteredPassword = hashPassword(enteredPassword);
        return hashedEnteredPassword != null && hashedEnteredPassword.equals(storedHash);
    }
}
