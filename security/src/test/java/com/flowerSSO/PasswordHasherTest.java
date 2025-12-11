package com.flowerSSO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PasswordHasherTest {

    private PasswordHasher hasher;

    @BeforeEach
    void setup() {
        hasher = new PasswordHasherImpl();
    }

    @Test
    void testHashNotNull() {
        String plaintext = "testPassword123";
        String hashed = hasher.hash(plaintext);
        assertTrue(hashed != null && !hashed.isEmpty());
    }

    @Test
    void testHashDifferentFromPlaintext() {
        String plaintext = "mySecurePassword";
        String hashed = hasher.hash(plaintext);
        assertNotEquals(plaintext, hashed);
    }

    @Test
    void testHashStartsWithBCryptPrefix() {
        String plaintext = "password12345";
        String hashed = hasher.hash(plaintext);
        // BCrypt hashes start with $2a$, $2b$, or $2y$
        assertTrue(hashed.startsWith("$2"));
    }

    @Test
    void testHashSamePasswordTwiceProducesDifferentHashes() {
        String plaintext = "samePassword123";
        String hash1 = hasher.hash(plaintext);
        String hash2 = hasher.hash(plaintext);
        // BCrypt uses salt, so same password should produce different hashes
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testVerifyCorrectPassword() {
        String plaintext = "correctPassword";
        String hashed = hasher.hash(plaintext);
        assertTrue(hasher.verify(plaintext, hashed));
    }

    @Test
    void testVerifyIncorrectPassword() {
        String plaintext = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashed = hasher.hash(plaintext);
        assertFalse(hasher.verify(wrongPassword, hashed));
    }

    @Test
    void testHashNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            hasher.hash(null);
        });
    }

    @Test
    void testHashEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            hasher.hash("");
        });
    }

    @Test
    void testVerifyNullPlaintextThrowsException() {
        String hashed = hasher.hash("password");
        assertThrows(IllegalArgumentException.class, () -> {
            hasher.verify(null, hashed);
        });
    }

    @Test
    void testVerifyNullHashThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            hasher.verify("password", null);
        });
    }

    @Test
    void testVerifyEmptyPlaintextThrowsException() {
        String hashed = hasher.hash("password");
        assertThrows(IllegalArgumentException.class, () -> {
            hasher.verify("", hashed);
        });
    }

    @Test
    void testVerifyEmptyHashThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            hasher.verify("password", "");
        });
    }

    @Test
    void testVerifyInvalidHashReturnsFalse() {
        // Invalid hash format should return false instead of throwing exception
        assertFalse(hasher.verify("password", "not-a-valid-hash"));
    }
}
