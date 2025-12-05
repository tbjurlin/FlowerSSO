package com.flowerSSO;

import org.apache.commons.validator.routines.EmailValidator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class LoginCredentials {

    @JsonProperty("email")
    @JsonAlias({"email"})
    private String email;

    @JsonProperty("password")
    @JsonAlias({"password"})
    private String password;

    private XssSanitizer mySanitizer;

    private final Logger logger = LoggerFactory.getEventLogger();

    public LoginCredentials() {
        mySanitizer = new XssSanitizerImpl();
        logger.debug("finishing the default constructor");
    }

    public LoginCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {

        logger.debug("setting the email");
        final int maxLenth = 64;

        if (email == null) {
            logger.error("email must not be null.");
            throw new IllegalArgumentException("email must not be null.");
        }

        String sanitizedEmail = mySanitizer.sanitizeInput(email);

        if (sanitizedEmail.isEmpty()) {
            logger.error("name must not be empty.");
            throw new IllegalArgumentException("name must not be empty.");
        }
        if (sanitizedEmail.length() > maxLenth ) {
            logger.error("email must not exceed 64 characters");
            throw new IllegalArgumentException("email must not exceed 64 characters");
        }

        if (!EmailValidator.getInstance().isValid(sanitizedEmail)) {
            throw new IllegalArgumentException("Invalid email");
        }
        this.email = sanitizedEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {

        logger.debug("setting the password");
        final int maxLenth = 64;
        final int minLenth = 12;

        if (password == null) {
            logger.error("password must not be null.");
            throw new IllegalArgumentException("password must not be null.");
        }

        String sanitizedPassword = mySanitizer.sanitizeInput(password);

        if (sanitizedPassword.isEmpty()) {
            logger.error("name must not be empty.");
            throw new IllegalArgumentException("name must not be empty.");
        }

        if (sanitizedPassword.length() > maxLenth ) {
            logger.error("password must not exceed 64 characters");
            throw new IllegalArgumentException("password must not exceed 64 characters");
        }

        if (sanitizedPassword.length() < minLenth ) {
            logger.error("password must be at least 12 characters");
            throw new IllegalArgumentException("password must be at least 12 characters");
        }

        this.password = sanitizedPassword;
    }
}
