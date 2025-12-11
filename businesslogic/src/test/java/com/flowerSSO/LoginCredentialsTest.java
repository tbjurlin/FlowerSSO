package com.flowerSSO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginCredentialsTest {

    private LoginCredentials credentials = null;

    @BeforeEach
    void setup() {
        credentials = new LoginCredentials();
    }

    @Test
    void testValidEmail() {
        String email = "good@email.com";
        credentials.setEmail(email);
        assertEquals(credentials.getEmail(), email);
    }

    @Test
    public void testNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            credentials.setEmail(null);
        });
    }

    @Test
    public void testLongEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            String longEmail = RandomStringUtils.insecure().nextAlphanumeric(65);
            credentials.setEmail(longEmail);
        });
    }

    @Test
    public void testSanitizedEmail() {
        String xssInput = "<script>alert('Sanitization Test');</script>good@email.com";
        String expected = "good@email.com";

        credentials.setEmail(xssInput);

        assertEquals(expected, credentials.getEmail());
    }

    @Test
    void testInvalidEmail() {
        String email = "bademail";
        assertThrows(IllegalArgumentException.class, () -> {
            credentials.setEmail(email);
        });
    }

    @Test
    void testValidPassword() {
        String password = "good@password.com";
        credentials.setPassword(password);
        // Test that hashed password is not the same as plaintext
        assertNotEquals(password, credentials.getPassword());
        // Test that verify method works
        assertTrue(credentials.verifyPassword(password));
    }

    @Test
    void testPasswordIsHashed() {
        String password = "testPassword123";
        credentials.setPassword(password);
        // Verify password is hashed and not stored in plaintext
        assertNotEquals(password, credentials.getPassword());
        // Verify the hash starts with BCrypt prefix
        assertTrue(credentials.getPassword().startsWith("$2"));
        // Verify verification works
        assertTrue(credentials.verifyPassword(password));
    }

    @Test
    public void testNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            credentials.setPassword(null);
        });
    }

    @Test
    public void testShortPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            String shortPassword = RandomStringUtils.insecure().nextAlphanumeric(11);
            System.out.println(shortPassword);
            credentials.setPassword(shortPassword);
        });
    }

    @Test
    public void testLongPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            String longPassword = RandomStringUtils.insecure().nextAlphanumeric(65);
            credentials.setPassword(longPassword);
        });
    }

    @Test
    public void testSanitizedPassword() {
        String randomPassword = RandomStringUtils.insecure().nextAlphanumeric(24);
        String xssInput = "<script>alert('Sanitization Test');</script>" + randomPassword;
        String expected = randomPassword;

        credentials.setPassword(xssInput);

        // Test that the password is hashed
        assertNotEquals(expected, credentials.getPassword());
        // Test that verification works with the sanitized password
        assertTrue(credentials.verifyPassword(expected));
    }

    @Test
    public void testTempPasswordIsHashed() {
        credentials.setTempPassword();
        // Verify temp password is hashed (not null)
        assertTrue(credentials.getTempPassword() != null);
        // Verify the hash starts with BCrypt prefix
        assertTrue(credentials.getTempPassword().startsWith("$2"));
    }

}
