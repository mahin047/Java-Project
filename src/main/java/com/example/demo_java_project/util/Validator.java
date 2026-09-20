package com.example.demo_java_project.util;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern NAME  = Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,99}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern STUDENT_ID = Pattern.compile("^[A-Za-z0-9-]{4,30}$");

    private Validator() { }

    public static boolean isValidName(String s) {
        return s != null && NAME.matcher(s).matches();
    }

    public static boolean isValidEmail(String s) {
        return s != null && s.length() <= 120 && EMAIL.matcher(s).matches();
    }

    public static boolean isValidStudentId(String s) {
        return s != null && STUDENT_ID.matcher(s).matches();
    }

    /** Returns an error message, or null if the password is acceptable. */
    public static String passwordError(String pw) {
        if (pw == null || pw.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        if (pw.length() > 72) {                       // BCrypt ignores anything after 72 bytes
            return "Password must be at most 72 characters.";
        }
        boolean hasLetter = pw.chars().anyMatch(Character::isLetter);
        boolean hasDigit  = pw.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            return "Password must contain at least one letter and one digit.";
        }
        return null;
    }

    /** Strength meter score: 0 (empty) to 4 (strong). */
    public static int passwordStrength(String pw) {
        if (pw == null || pw.isEmpty()) return 0;

        int score = 0;
        if (pw.length() >= 8) score++;
        if (pw.chars().anyMatch(Character::isUpperCase)
                && pw.chars().anyMatch(Character::isLowerCase)) score++;
        if (pw.chars().anyMatch(Character::isDigit)) score++;
        if (pw.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) score++;
        return score;
    }
}