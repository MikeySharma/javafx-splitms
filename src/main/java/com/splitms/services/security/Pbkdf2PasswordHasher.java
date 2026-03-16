package com.splitms.services.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    @Override
    public String hash(String plainTextPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        byte[] digest = pbkdf2(plainTextPassword.toCharArray(), salt);

        String saltPart = Base64.getEncoder().encodeToString(salt);
        String digestPart = Base64.getEncoder().encodeToString(digest);
        return ITERATIONS + ":" + saltPart + ":" + digestPart;
    }

    @Override
    public boolean matches(String plainTextPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expectedDigest = Base64.getDecoder().decode(parts[2]);
        byte[] providedDigest = pbkdf2(plainTextPassword.toCharArray(), salt, iterations, KEY_LENGTH);

        if (expectedDigest.length != providedDigest.length) {
            return false;
        }

        int diff = 0;
        for (int i = 0; i < expectedDigest.length; i++) {
            diff |= expectedDigest[i] ^ providedDigest[i];
        }
        return diff == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        return pbkdf2(password, salt, ITERATIONS, KEY_LENGTH);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }
}
