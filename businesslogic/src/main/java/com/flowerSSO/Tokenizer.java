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

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class Tokenizer {

    public static final String SECRET_KEY = "RockertSoftwareRocks2025ThisIsNotSecureEnough";

    public static String tokenize(Credentials userCredentials) {
        // Check for null credentials
        if(userCredentials == null) {
            throw new IllegalArgumentException("User credentials cannot be null");
        }

        // Turn the secret key string into a SecretKey object
        SecretKey jwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        // Parse user information from Credentials
        int id = userCredentials.getId();
        String fName = userCredentials.getFirstName();
        String lName = userCredentials.getLastName();
        String loc = userCredentials.getLocation();
        String dept = userCredentials.getDepartment();
        String title = userCredentials.getTitle();

        // Set parameters for JWT token
        Date now = new Date(System.currentTimeMillis());
        Date expiry = new Date(now.getTime() + TimeUnit.MINUTES.toMillis(60));
        Map<String, Object> claims = new HashMap<>();

        claims.put("id", id);
        claims.put("first_name", fName);
        claims.put("last_name", lName);
        claims.put("location", loc);
        claims.put("department", dept);
        claims.put("title", title);

        // Build the JWT token
        String token =  Jwts.builder()
                            .issuer("Auth Service")
                            .claims(claims)
                            .subject((fName + " " + lName))
                            .issuedAt(now)
                            .expiration(expiry)
                            .signWith(jwtSecretKey)
                            .compact();
        return token;
    }
}
