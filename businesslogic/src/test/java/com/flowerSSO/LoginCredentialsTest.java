package com.flowerSSO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            String longEmail = RandomStringUtils.insecure().next(65);
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
        assertEquals(credentials.getPassword(), password);
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
            String shortPassword = RandomStringUtils.insecure().next(11);
            credentials.setPassword(shortPassword);
        });
    }

    @Test
    public void testLongPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            String longPassword = RandomStringUtils.insecure().next(65);
            credentials.setPassword(longPassword);
        });
    }

    @Test
    public void testSanitizedPassword() {
        String randomPassword = RandomStringUtils.insecure().next(24);
        String xssInput = "<script>alert('Sanitization Test');</script>" + randomPassword;
        String expected = randomPassword;

        credentials.setPassword(xssInput);

        assertEquals(expected, credentials.getPassword());
    }

}
