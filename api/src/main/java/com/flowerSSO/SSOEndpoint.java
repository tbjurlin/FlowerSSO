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
import jakarta.validation.constraints.Email;

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

    private String authServerUrl;
    private final Logger logger = LoggerFactory.getEventLogger();
    private final ConfigurationManager configManager = ConfigurationManagerImpl.getInstance();

    @PostConstruct
    public void init() {
        authServerUrl = String.format("%s:%d/%s",
                configManager.getAuthServerHost(),
                configManager.getAuthServerPort(),
                configManager.getAuthServerSubdomain());
    }

    @GetMapping("verification")
    public ResponseEntity<String> verifyCredentials(@Valid @RequestHeader("Bearer") String tokenStr) {
        Token token = new Token();
        token.setToken(tokenStr);
        Authenticator auth = new AuthenticatorImpl(authServerUrl);
        Credentials userCredentials = auth.authenticate(token);
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(String.format("{\"firstName\": \"%s\", \"lastName\": \"%s\"}", 
                                     userCredentials.getFirstName(),
                                     userCredentials.getLastName()));
    }

    @GetMapping("/profile")
    public ResponseEntity<String> profile(@Valid @RequestHeader("Bearer") String tokenStr) {
        Token token = new Token();
        token.setToken(tokenStr);
        CredentialsDAO dao = new CredentialsDAO();
        Credentials userCredentials = dao.getCredentialsFromToken(token);
        ObjectMapper mapper = new ObjectMapper();
        if(userCredentials == null) {
            logger.error("Cannot return a null credential.");
            throw new NullPointerException("Cannot return a null credential.");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String returnObj = objectMapper.writeValueAsString(userCredentials);
        
            logger.info("Returning HTTP response code 200.");
            return ResponseEntity.ok()
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .body(returnObj);
        } catch(JsonProcessingException e) {
            logger.error("Unable to parse JSON from list of resources.");
            throw new NullPointerException("Unable to parse JSON from list of resources.");
        }
    }

    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@Valid @RequestHeader("Bearer") String tokenStr, @Valid @RequestBody String newPassword) {
        Token token = new Token();
        CredentialsDAO dao = new CredentialsDAO();
        dao.updatePassword(newPassword, token);
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body("{\"msg\": \"Password updated successfully!\"}");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginCredentials userLogin) {
        logger.info(String.format("Receieved POST request (login) from user: %s", userLogin.getEmail()));
        CredentialsDAO dao = new CredentialsDAO();
        Credentials userCredentials = dao.getCredentialsFromLogin(userLogin);
        String jwt = Tokenizer.tokenize(userCredentials);
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .header("Authorization", jwt)
                             .body("{\"msg\": \"Login successful.\"}");
    }

    @GetMapping("/admin/users")
    public ResponseEntity<String> getAllUsers(@Valid @RequestHeader("Bearer") String tokenStr) {
        Token token = new Token();
        token.setToken(tokenStr);
        CredentialsDAO dao = new CredentialsDAO();
        List<Credentials> allUsers = dao.getAllCredentials(token);
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse;
        try {
            jsonResponse = mapper.writeValueAsString(allUsers);
        } catch (JsonProcessingException e) {
            logger.error("Error converting user list to JSON");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .body("{\"msg\": \"Error retrieving users.\"}");
        }
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(jsonResponse);
    }

    @PostMapping("/admin/add")
    public ResponseEntity<String> signup(@Valid @RequestHeader("Bearer") String tokenStr, @Valid @RequestBody Credentials userCredentials) {
        Token token = new Token();
        token.setToken(tokenStr);
        CredentialsDAO dao = new CredentialsDAO();
        dao.insertCredentials(userCredentials, token);
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body("{\"msg\": \"User updated successfully!\"}");
    }

    @PutMapping("/admin/update")
    public ResponseEntity<String> update(@Valid @RequestHeader("Bearer") String tokenStr, @Valid @RequestBody Credentials newUserCredentials) {
        Token token = new Token();
        token.setToken(tokenStr);
        CredentialsDAO dao = new CredentialsDAO();
        dao.updateCredentials(newUserCredentials, token);
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body("{\"msg\": \"User updated successfully!\"}");
    }

    @DeleteMapping("/admin/delete/{userId}")
    public ResponseEntity<String> delete(@Valid @RequestHeader("Bearer") String tokenStr, @PathVariable("userId") int userId) {
        Token token = new Token();
        token.setToken(tokenStr);
        CredentialsDAO dao = new CredentialsDAO();
        dao.deleteCredentials(userId, token);
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body("{\"msg\": \"User deleted successfully!\"}");
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody LoginCredentials userLogin) {
        String userEmail = userLogin.getEmail();
        userLogin.setTempPassword();
        CredentialsDAO dao = new CredentialsDAO();
        dao.updateTempPassword(userLogin.getTempPassword(), userLogin.getEmail());
        EmailService.sendTempPasswordEmail(userEmail, userLogin.getTempPassword());
        return ResponseEntity.ok("Password reset successfully");
    }
}