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

import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;

/**
 * REST API Controller Endpoint for the Flower Single-Sign-On (SSO) Server.
 * 
 * @author Ben Edens
 * @version 1.0
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("SSO")
public class SSOEndpoint {

    @GetMapping("/admin/users")
    public ResponseEntity<String> getAllUsers(@Valid @RequestHeader("Bearer") String tokenStr) {
        // Verify admin token
        // Return all user data
        return ResponseEntity.ok("Admin data retrieved successfully");
    }

    @PutMapping("/admin/review/{userId}")
    public ResponseEntity<String> reviewChanges(@Valid @RequestHeader("Bearer") String tokenStr, @PathVariable("userId") int userId) {
        // Verify admin token
        // Update user data marked for admin review
        return ResponseEntity.ok("User review data retrieved successfully");
    }

    @PostMapping("/admin/user/{userId}")
    public ResponseEntity<String> createUser(@Valid @RequestHeader("Bearer") String tokenStr, @PathVariable("userId") int userId) {
        // Verify admin token
        // Create new user in database
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
    }

    @DeleteMapping("/admin/user/{userId}")
    public ResponseEntity<String> deleteUser(@Valid @RequestHeader("Bearer") String tokenStr, @PathVariable("userId") int userId) {
        // Verify admin token
        // Delete user from database
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/profile")
    public ResponseEntity<String> profile(@Valid @RequestHeader("Bearer") String tokenStr) {
        // Retrieve user profile from database using token
        // Return user profile data
        return ResponseEntity.ok("User profile retrieved successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginCredentials userLogin) {
        // Check login credentials against database
        // If valid, generate token and return success response including token
        return ResponseEntity.ok("User signed in successfully");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody Credentials userCredentials) {
        // Create new user in database
        return ResponseEntity.status(HttpStatus.CREATED).body("User signed up successfully");
    }

    @PutMapping("/update")
    public ResponseEntity<String> update(@Valid @RequestHeader("Bearer") String tokenStr, @Valid @RequestBody Credentials userCredentials) {
        // Update user information in database
        return ResponseEntity.ok("User information updated successfully");
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody LoginCredentials userLogin) {
        // Handle forgot password process
        return ResponseEntity.ok("Password reset successfully");
    }
}