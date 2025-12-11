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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CredentialsDAO class using Mockito for database mocking.
 * Tests all CRUD operations and edge cases.
 */
@ExtendWith(MockitoExtension.class)
public class CredentialsDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Authenticator mockAuthenticator;

    private CredentialsDAO credentialsDAO;
    private MockedStatic<DatabaseConnectionPool> mockedConnectionPool;

    @BeforeEach
    void setup() {
        // Inject the mock authenticator via constructor
        credentialsDAO = new CredentialsDAO(mockAuthenticator);
        
        // Mock the static DatabaseConnectionPool.getConnection() method
        mockedConnectionPool = mockStatic(DatabaseConnectionPool.class);
        mockedConnectionPool.when(DatabaseConnectionPool::getConnection).thenReturn(mockConnection);
    }

    @AfterEach
    void teardown() {
        if (mockedConnectionPool != null) {
            mockedConnectionPool.close();
        }
    }

    @Nested
    class InsertCredentialsTests {

        private Credentials testCredentials;
        private Token adminToken;
        private Token nonAdminToken;
        private Credentials adminCredentials;
        private Credentials nonAdminCredentials;

        @BeforeEach
        void setupInsertTests() throws Exception {
            testCredentials = createTestCredentials();
            adminToken = createTestToken();
            nonAdminToken = createTestToken();
            adminCredentials = createAdminCredentials();
            nonAdminCredentials = createNonAdminCredentials();

            // Mock authenticator to return admin credentials for admin token
            lenient().when(mockAuthenticator.authenticate(adminToken)).thenReturn(adminCredentials);
            lenient().when(mockAuthenticator.authenticate(nonAdminToken)).thenReturn(nonAdminCredentials);
            
            // Mock verifyIsAdmin - returns true for admin, needs separate ResultSet mocking
            lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Mock ResultSet to return true for isAdmin check
            lenient().when(mockResultSet.next()).thenReturn(true);
            lenient().when(mockResultSet.getBoolean("isAdmin")).thenReturn(true);
        }

        @Test
        void insertCredentials_Success_WithAdminToken() throws SQLException {
            // Arrange - admin verification mocked in @BeforeEach

            // Act
            assertDoesNotThrow(() -> credentialsDAO.insertCredentials(testCredentials, adminToken));

            // Assert - verify authenticator was called for admin check
            verify(mockAuthenticator, atLeastOnce()).authenticate(adminToken);
            verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
        }

        @Test
        void insertCredentials_FailsWithNonAdminToken() throws SQLException {
            // Arrange - mock verifyIsAdmin to return false for non-admin
            when(mockResultSet.getBoolean("isAdmin")).thenReturn(false);

            // Act & Assert - should throw AuthenticationException
            Exception exception = assertThrows(Exception.class, 
                () -> credentialsDAO.insertCredentials(testCredentials, nonAdminToken));
            
            // Verify the exception message
            assertTrue(exception.getMessage().contains("admin"));
        }

        @Test
        void insertCredentials_SQLException() throws SQLException {
            // Arrange
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

            // Act & Assert - method catches SQLException and prints, doesn't throw
            assertDoesNotThrow(() -> credentialsDAO.insertCredentials(testCredentials, adminToken));
        }

        @Test
        void insertCredentials_NullCredentials() throws SQLException {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                credentialsDAO.insertCredentials(null, adminToken));
        }
    }

    @Nested
    class UpdateCredentialsTests {

        private Credentials testCredentials;
        private Token adminToken;
        private Token nonAdminToken;
        private Credentials adminCredentials;
        private Credentials nonAdminCredentials;

        @BeforeEach
        void setupUpdateTests() throws Exception {
            testCredentials = createTestCredentials();
            adminToken = createTestToken();
            nonAdminToken = createTestToken();
            adminCredentials = createAdminCredentials();
            nonAdminCredentials = createNonAdminCredentials();

            // Mock authenticator
            lenient().when(mockAuthenticator.authenticate(adminToken)).thenReturn(adminCredentials);
            lenient().when(mockAuthenticator.authenticate(nonAdminToken)).thenReturn(nonAdminCredentials);
            
            lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Mock ResultSet for verifyIsAdmin
            lenient().when(mockResultSet.next()).thenReturn(true);
            lenient().when(mockResultSet.getBoolean("isAdmin")).thenReturn(true);
        }

        @Test
        void updateCredentials_Success_WithAdminToken() throws SQLException {
            // Act
            assertDoesNotThrow(() -> credentialsDAO.updateCredentials(testCredentials, adminToken));

            // Assert - verify the update was called
            verify(mockAuthenticator, atLeastOnce()).authenticate(adminToken);
            verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
        }

        @Test
        void updateCredentials_FailsWithNonAdminToken() throws SQLException {
            // Arrange - mock verifyIsAdmin to return false
            when(mockResultSet.getBoolean("isAdmin")).thenReturn(false);

            // Act & Assert - should throw AuthenticationException
            Exception exception = assertThrows(Exception.class, 
                () -> credentialsDAO.updateCredentials(testCredentials, nonAdminToken));
            
            // Verify the exception message
            assertTrue(exception.getMessage().contains("admin"));
        }

        @Test
        void updateCredentials_SQLException() throws SQLException {
            // Arrange
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Update failed"));

            // Act & Assert - method catches and swallows exception
            assertDoesNotThrow(() -> credentialsDAO.updateCredentials(testCredentials, adminToken));
        }
    }

    @Nested
    class UpdatePasswordTests {

        private Token testToken;
        private String newPassword;
        private Credentials userCredentials;

        @BeforeEach
        void setupPasswordTests() throws Exception {
            testToken = createTestToken();
            newPassword = "newSecurePassword123!";
            userCredentials = createTestCredentials();

            // Mock authenticator to return user credentials
            when(mockAuthenticator.authenticate(testToken)).thenReturn(userCredentials);
            
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        }

        @Test
        void updatePassword_Success() {
            // Act
            assertDoesNotThrow(() -> credentialsDAO.updatePassword(newPassword, testToken));

            // Assert - Verify that the prepared statement was used
            try {
                verify(mockAuthenticator).authenticate(testToken); // Verify authenticator was called
                verify(mockConnection, times(1)).prepareStatement(anyString());
                verify(mockPreparedStatement, times(1)).executeUpdate();
            } catch (SQLException e) {
                fail("SQLException should not be thrown in test verification");
            }
        }

        @Test
        void updatePassword_SQLException() throws SQLException {
            // Arrange
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Password update failed"));

            // Act & Assert
            assertDoesNotThrow(() -> credentialsDAO.updatePassword(newPassword, testToken));
        }
    }

    @Nested
    class GetCredentialsTests {

        private LoginCredentials loginCredentials;

        @BeforeEach
        void setupGetTests() throws Exception {
            loginCredentials = createLoginCredentials();

            lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            lenient().when(mockResultSet.next()).thenReturn(true);
            
            // Mock ResultSet data - use lenient() to avoid unnecessary stubbing warnings
            lenient().when(mockResultSet.getInt("id")).thenReturn(1);
            lenient().when(mockResultSet.getString("firstName")).thenReturn("John");
            lenient().when(mockResultSet.getString("lastName")).thenReturn("Doe");
            lenient().when(mockResultSet.getString("title")).thenReturn("Developer");
            lenient().when(mockResultSet.getString("department")).thenReturn("IT");
            lenient().when(mockResultSet.getString("location")).thenReturn("USA");
        }

        @Test
        void getCredentialsFromLogin_Success() {
            // Act
            Credentials result = credentialsDAO.getCredentialsFromLogin(loginCredentials);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals("Developer", result.getTitle());
            assertEquals("IT", result.getDepartment());
            assertEquals("USA", result.getLocation());
            
            try {
                verify(mockPreparedStatement).setString(1, loginCredentials.getEmail());
                verify(mockPreparedStatement).setString(2, loginCredentials.getPassword());
                verify(mockPreparedStatement).executeQuery();
            } catch (SQLException e) {
                fail("SQLException should not be thrown in test verification");
            }
        }

        @Test
        void getCredentialsFromLogin_NotFound() throws SQLException {
            // Arrange
            when(mockResultSet.next()).thenReturn(false);

            // Act
            Credentials result = credentialsDAO.getCredentialsFromLogin(loginCredentials);

            // Assert
            assertNull(result);
            verify(mockPreparedStatement).executeQuery();
        }

        @Test
        void getCredentialsFromLogin_SQLException() throws SQLException {
            // Arrange
            when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

            // Act
            Credentials result = credentialsDAO.getCredentialsFromLogin(loginCredentials);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    class GetAllCredentialsTests {

        private Token adminToken;
        private Token nonAdminToken;
        private Credentials adminCredentials;
        private Credentials nonAdminCredentials;

        @BeforeEach
        void setupGetAllTests() throws Exception {
            adminToken = createTestToken();
            nonAdminToken = createTestToken();
            adminCredentials = createAdminCredentials();
            nonAdminCredentials = createNonAdminCredentials();

            // Mock authenticator
            lenient().when(mockAuthenticator.authenticate(adminToken)).thenReturn(adminCredentials);
            lenient().when(mockAuthenticator.authenticate(nonAdminToken)).thenReturn(nonAdminCredentials);
            
            lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            // Mock ResultSet for verifyIsAdmin - use lenient for all
            lenient().when(mockResultSet.next()).thenReturn(true);
            lenient().when(mockResultSet.getBoolean(anyString())).thenReturn(true);
            lenient().when(mockResultSet.getInt(anyString())).thenReturn(1);
            lenient().when(mockResultSet.getString(anyString())).thenReturn("testPassword1234");
        }

        @Test
        void getAllCredentials_Success_MultipleRecords_WithAdminToken() throws SQLException {
            // Arrange
            // First call to next() is for verifyIsAdmin check (returns true),
            // then three calls for the actual data retrieval, then false to end loop
            lenient().when(mockResultSet.next()).thenReturn(true, true, true, true, false);
            
            // For verifyIsAdmin check, then for three records
            lenient().when(mockResultSet.getBoolean("isAdmin")).thenReturn(true, false, true, false);
            lenient().when(mockResultSet.getInt("id")).thenReturn(1, 2, 3);
            lenient().when(mockResultSet.getString("email")).thenReturn("user1@test.com", "user2@test.com", "user3@test.com");
            // Note: passwords are NOT returned in getAllCredentials for security
            lenient().when(mockResultSet.getString("firstName")).thenReturn("John", "Jane", "Bob");
            lenient().when(mockResultSet.getString("lastName")).thenReturn("Doe", "Smith", "Johnson");
            lenient().when(mockResultSet.getString("title")).thenReturn("Developer", "Manager", "Analyst");
            lenient().when(mockResultSet.getString("department")).thenReturn("IT", "IT", "Finance");
            lenient().when(mockResultSet.getString("location")).thenReturn("USA", "Canada", "UK");

            // Act
            var result = credentialsDAO.getAllCredentials(adminToken);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            
            assertEquals(1, result.get(0).getId());
            assertEquals("user1@test.com", result.get(0).getEmail());
            assertEquals("John", result.get(0).getFirstName());
            
            assertEquals(2, result.get(1).getId());
            assertEquals("user2@test.com", result.get(1).getEmail());
            assertTrue(result.get(1).getIsAdmin());
            
            verify(mockAuthenticator, atLeastOnce()).authenticate(adminToken);
        }

        @Test
        void getAllCredentials_FailsWithNonAdminToken() throws SQLException {
            // Arrange - mock verifyIsAdmin to return false
            when(mockResultSet.getBoolean("isAdmin")).thenReturn(false);

            // Act & Assert - should throw AuthenticationException
            Exception exception = assertThrows(Exception.class, 
                () -> credentialsDAO.getAllCredentials(nonAdminToken));
            
            // Verify the exception message
            assertTrue(exception.getMessage().contains("admin"));
        }

        @Test
        void getAllCredentials_EmptyResult() throws SQLException {
            // Arrange
            when(mockResultSet.next()).thenReturn(true).thenReturn(false); // First for verifyIsAdmin, then for query

            // Act
            var result = credentialsDAO.getAllCredentials(adminToken);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void getAllCredentials_SQLException() throws SQLException {
            // Arrange  
            // First executeQuery succeeds (for verifyIsAdmin), second one throws exception
            when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet) // First call for verifyIsAdmin
                .thenThrow(new SQLException("Query failed")); // Second call for getAllCredentials

            // Act
            var result = credentialsDAO.getAllCredentials(adminToken);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class DeleteCredentialsTests {

        private Token adminToken;
        private Token nonAdminToken;
        private Credentials adminCredentials;
        private Credentials nonAdminCredentials;

        @BeforeEach
        void setupDeleteTests() throws Exception {
            adminToken = createTestToken();
            nonAdminToken = createTestToken();
            adminCredentials = createAdminCredentials();
            nonAdminCredentials = createNonAdminCredentials();
            
            // Mock authenticator
            lenient().when(mockAuthenticator.authenticate(adminToken)).thenReturn(adminCredentials);
            lenient().when(mockAuthenticator.authenticate(nonAdminToken)).thenReturn(nonAdminCredentials);
            
            lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            
            // Mock ResultSet for verifyIsAdmin
            lenient().when(mockResultSet.next()).thenReturn(true);
            lenient().when(mockResultSet.getBoolean("isAdmin")).thenReturn(true);
        }

        @Test
        void deleteCredentials_Success_WithAdminToken() {
            // Arrange
            int testId = 42;

            // Act
            assertDoesNotThrow(() -> credentialsDAO.deleteCredentials(testId, adminToken));

            // Assert
            try {
                verify(mockAuthenticator, atLeastOnce()).authenticate(adminToken);
                verify(mockPreparedStatement, atLeastOnce()).executeUpdate();
            } catch (SQLException e) {
                fail("SQLException should not be thrown in test verification");
            }
        }

        @Test
        void deleteCredentials_FailsWithNonAdminToken() throws SQLException {
            // Arrange
            int testId = 42;
            when(mockResultSet.getBoolean("isAdmin")).thenReturn(false);

            // Act & Assert - should throw AuthenticationException
            Exception exception = assertThrows(Exception.class, 
                () -> credentialsDAO.deleteCredentials(testId, nonAdminToken));
            
            // Verify the exception message
            assertTrue(exception.getMessage().contains("admin"));
        }

        @Test
        void deleteCredentials_SQLException() throws SQLException {
            // Arrange
            int testId = 42;
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Delete failed"));

            // Act & Assert - method catches and swallows exception
            assertDoesNotThrow(() -> credentialsDAO.deleteCredentials(testId, adminToken));
        }

        @Test
        void deleteCredentials_ZeroId() {
            // Act
            assertDoesNotThrow(() -> credentialsDAO.deleteCredentials(0, adminToken));

            // Assert
            try {
                verify(mockAuthenticator, atLeastOnce()).authenticate(adminToken);
            } catch (Exception e) {
                fail("Exception should not be thrown in test verification");
            }
        }

        @Test
        void deleteCredentials_NegativeId() {
            // Act
            assertDoesNotThrow(() -> credentialsDAO.deleteCredentials(-1, adminToken));

            // Assert
            try {
                verify(mockAuthenticator, atLeastOnce()).authenticate(adminToken);
            } catch (Exception e) {
                fail("Exception should not be thrown in test verification");
            }
        }
    }

    // Helper methods to create test objects
    
    private Credentials createTestCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(1);
        credentials.setEmail("test@example.com");
        credentials.setPassword("securePassword123");
        credentials.setIsAdmin(false);
        credentials.setFirstName("John");
        credentials.setLastName("Doe");
        credentials.setTitle("Developer");
        credentials.setDepartment("IT");
        credentials.setLocation("USA");
        return credentials;
    }

    private Credentials createAdminCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(100);
        credentials.setEmail("admin@example.com");
        credentials.setPassword("adminPassword123456"); // Must be 12+ characters
        credentials.setIsAdmin(true);
        credentials.setFirstName("Admin");
        credentials.setLastName("User");
        credentials.setTitle("Administrator");
        credentials.setDepartment("IT");
        credentials.setLocation("USA");
        return credentials;
    }

    private Credentials createNonAdminCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(50);
        credentials.setEmail("user@example.com");
        credentials.setPassword("userPassword123");
        credentials.setIsAdmin(false);
        credentials.setFirstName("Regular");
        credentials.setLastName("User");
        credentials.setTitle("Developer");
        credentials.setDepartment("IT");
        credentials.setLocation("USA");
        return credentials;
    }

    private LoginCredentials createLoginCredentials() {
        LoginCredentials loginCreds = new LoginCredentials();
        loginCreds.setEmail("test@example.com");
        loginCreds.setPassword("securePassword123");
        return loginCreds;
    }

    private Token createTestToken() {
        Token token = new Token();
        // Use a valid JWT format (header.payload.signature) from the existing tests
        token.setToken("eyJhbGciOiJIUzI1NiJ9.eyJsYXN0X25hbWUiOiJHcmVzd2VsbCIsImxvY2F0aW9uIjoiSmFwYW4iLCJpZCI6MzEsImRlcGFydG1lbnQiOiJJbmZvcm1hdGlvbiBUZWNobm9sb2d5IiwidGl0bGUiOiJNYW5hZ2VyIiwiZmlyc3RfbmFtZSI6IlRpbW90aGVlIiwic3ViIjoiVGltb3RoZWUgR3Jlc3dlbGwiLCJpYXQiOjE3NjIyMjQ0OTgsImV4cCI6MTc2MjIyODA5OH0.9uPEIpUtJmrfmCnsFyK3pZXRhSFyIxe5JuHmb4WSyAk");
        return token;
    }
}
