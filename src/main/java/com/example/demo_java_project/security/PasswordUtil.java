package com.example.demo_java_project.security;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int LOG_ROUNDS = 12;   // higher = slower = harder to brute-force

    private PasswordUtil() { }

    /** Turns a plain password into a salted BCrypt hash (60 chars). */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /** Checks a plain password against a stored hash. */
    public static boolean verify(String plainPassword, String storedHash) {
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;   // stored hash is malformed
        }
    }
}