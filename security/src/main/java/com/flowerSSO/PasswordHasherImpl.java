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

import org.mindrot.jbcrypt.BCrypt;

/**
 * Implementation of PasswordHasher using BCrypt hashing algorithm.
 * BCrypt is a secure password hashing function designed specifically for passwords.
 * 
 * @author Ben Edens
 * @version 1.0
 */
public class PasswordHasherImpl implements PasswordHasher {

    private final Logger logger = LoggerFactory.getSecurityLogger();
    
    // BCrypt work factor - higher is more secure but slower (10-12 is recommended)
    private static final int BCRYPT_ROUNDS = 12;

    /**
     * {@inheritDoc}
     */
    @Override
    public String hash(String plaintext) {
        logger.debug("Hashing password");
        
        if (plaintext == null) {
            logger.error("Attempt to hash null password");
            throw new IllegalArgumentException("Password cannot be null");
        }
        
        if (plaintext.isEmpty()) {
            logger.error("Attempt to hash empty password");
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        String hashed = BCrypt.hashpw(plaintext, BCrypt.gensalt(BCRYPT_ROUNDS));
        logger.debug("Password hashed successfully");
        return hashed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verify(String plaintext, String hashed) {
        logger.debug("Verifying password");
        
        if (plaintext == null || plaintext.isEmpty()) {
            logger.error("Attempt to verify with null or empty plaintext password");
            throw new IllegalArgumentException("Plaintext password cannot be null or empty");
        }
        
        if (hashed == null || hashed.isEmpty()) {
            logger.error("Attempt to verify with null or empty hashed password");
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }
        
        try {
            boolean matches = BCrypt.checkpw(plaintext, hashed);
            logger.debug("Password verification result: " + matches);
            return matches;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid hash format during verification: " + e.getMessage());
            return false;
        }
    }
}
