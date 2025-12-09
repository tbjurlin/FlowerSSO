package com.flowerSSO;

/*
 * This is free and unencumbered software released into the public domain.
 * Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software,
 * either in source code form or as a compiled binary, for any purpose, commercial or
 * non-commercial, and by any means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors of this
 * software dedicate any and all copyright interest in the software to the public domain.
 * We make this dedication for the benefit of the public at large and to the detriment of
 * our heirs and successors. We intend this dedication to be an overt act of relinquishment in
 * perpetuity of all present and future rights to this software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to: https://unlicense.org/
*/

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
