package com.lzz.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = digest(password, salt);
        return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String password, String stored) {
        if (stored == null || !stored.contains("$")) {
            return false;
        }
        String[] parts = stored.split("\\$", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] hash = digest(password, salt);
        String actual = Base64.getEncoder().encodeToString(hash);
        return actual.equals(parts[1]);
    }

    private static byte[] digest(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            md.update(password.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (Exception e) {
            throw new IllegalStateException("密码处理失败");
        }
    }
}
