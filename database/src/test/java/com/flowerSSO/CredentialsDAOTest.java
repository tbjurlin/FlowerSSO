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
        private Token testToken;
        private Credentials adminCredentials;

        @BeforeEach
        void setupInsertTests() throws Exception {
            testCredentials = createTestCredentials();
            testToken = createTestToken();
            adminCredentials = createAdminCredentials();

            // Mock authenticator to return admin credentials - use lenient() to avoid unnecessary stubbing warnings
            lenient().when(mockAuthenticator.authenticate(testToken)).thenReturn(adminCredentials);
            
            lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        }

        @Test
        void insertCredentials_Success() throws SQLException {
            // Arrange - setup is in @BeforeEach

            // Act
            assertDoesNotThrow(() -> credentialsDAO.insertCredentials(testCredentials, testToken));

            // Assert
            verify(mockAuthenticator).authenticate(testToken); // Verify authenticator was called
            verify(mockConnection).prepareStatement(anyString());
            verify(mockPreparedStatement).setString(1, testCredentials.getEmail());
            verify(mockPreparedStatement).setString(2, testCredentials.getPassword());
            verify(mockPreparedStatement).setBoolean(3, testCredentials.getIsAdmin());
            verify(mockPreparedStatement).setString(4, testCredentials.getFirstName());
            verify(mockPreparedStatement).setString(5, testCredentials.getLastName());
            verify(mockPreparedStatement).setString(6, testCredentials.getTitle());
            verify(mockPreparedStatement).setString(7, testCredentials.getDepartment());
            verify(mockPreparedStatement).setString(8, testCredentials.getLocation());
            verify(mockPreparedStatement).setString(9, testCredentials.getUserRole());
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConnection).close();
        }

        @Test
        void insertCredentials_SQLException() throws SQLException {
            // Arrange
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

            // Act & Assert
            assertDoesNotThrow(() -> credentialsDAO.insertCredentials(testCredentials, testToken));
            
            verify(mockConnection).close();
        }

        @Test
        void insertCredentials_NullCredentials() throws SQLException {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                credentialsDAO.insertCredentials(null, testToken));
        }
    }

    @Nested
    class UpdateCredentialsTests {

        private Credentials testCredentials;
        private Token testToken;
        private Credentials adminCredentials;

        @BeforeEach
        void setupUpdateTests() throws Exception {
            testCredentials = createTestCredentials();
            testToken = createTestToken();
            adminCredentials = createAdminCredentials();

            // Mock authenticator to return admin credentials
            when(mockAuthenticator.authenticate(testToken)).thenReturn(adminCredentials);
            
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        }

        @Test
        void updateCredentials_Success() throws SQLException {
            // Act
            assertDoesNotThrow(() -> credentialsDAO.updateCredentials(testCredentials, testToken));

            // Assert
            verify(mockAuthenticator).authenticate(testToken); // Verify authenticator was called
            verify(mockConnection).prepareStatement(anyString());
            verify(mockPreparedStatement).setString(1, testCredentials.getEmail());
            verify(mockPreparedStatement).setString(2, testCredentials.getPassword());
            verify(mockPreparedStatement).setBoolean(3, testCredentials.getIsAdmin());
            verify(mockPreparedStatement).executeUpdate();
            verify(mockConnection).close();
        }

        @Test
        void updateCredentials_SQLException() throws SQLException {
            // Arrange
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Update failed"));

            // Act & Assert
            assertDoesNotThrow(() -> credentialsDAO.updateCredentials(testCredentials, testToken));
            
            verify(mockConnection).close();
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
            lenient().when(mockResultSet.getString("userRole")).thenReturn("Employee");
        }

        @Test
        void getCredentials_Success() {
            // Act
            Credentials result = credentialsDAO.getCredentials(loginCredentials);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals("Developer", result.getTitle());
            assertEquals("IT", result.getDepartment());
            assertEquals("USA", result.getLocation());
            assertEquals("Employee", result.getUserRole());
            
            try {
                verify(mockPreparedStatement).setString(1, loginCredentials.getEmail());
                verify(mockPreparedStatement).setString(2, loginCredentials.getPassword());
                verify(mockPreparedStatement).executeQuery();
            } catch (SQLException e) {
                fail("SQLException should not be thrown in test verification");
            }
        }

        @Test
        void getCredentials_NotFound() throws SQLException {
            // Arrange
            when(mockResultSet.next()).thenReturn(false);

            // Act
            Credentials result = credentialsDAO.getCredentials(loginCredentials);

            // Assert
            assertNull(result);
            verify(mockPreparedStatement).executeQuery();
        }

        @Test
        void getCredentials_SQLException() throws SQLException {
            // Arrange
            when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

            // Act
            Credentials result = credentialsDAO.getCredentials(loginCredentials);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    class GetAllCredentialsTests {

        private Token testToken;
        private Credentials adminCredentials;

        @BeforeEach
        void setupGetAllTests() throws Exception {
            testToken = createTestToken();
            adminCredentials = createAdminCredentials();

            // Mock authenticator to return admin credentials
            when(mockAuthenticator.authenticate(testToken)).thenReturn(adminCredentials);
            
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        }

        @Test
        void getAllCredentials_Success_MultipleRecords() throws SQLException {
            // Arrange
            when(mockResultSet.next()).thenReturn(true, true, true, false);
            
            when(mockResultSet.getInt("id")).thenReturn(1, 2, 3);
            when(mockResultSet.getString("email")).thenReturn("user1@test.com", "user2@test.com", "user3@test.com");
            when(mockResultSet.getString("password")).thenReturn("password123456", "password234567", "password345678");
            when(mockResultSet.getBoolean("isAdmin")).thenReturn(false, true, false);
            when(mockResultSet.getString("firstName")).thenReturn("John", "Jane", "Bob");
            when(mockResultSet.getString("lastName")).thenReturn("Doe", "Smith", "Johnson");
            when(mockResultSet.getString("title")).thenReturn("Developer", "Manager", "Analyst");
            when(mockResultSet.getString("department")).thenReturn("IT", "IT", "Finance");
            when(mockResultSet.getString("location")).thenReturn("USA", "Canada", "UK");
            when(mockResultSet.getString("userRole")).thenReturn("Employee", "Admin", "Employee");

            // Act
            var result = credentialsDAO.getAllCredentials(testToken);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            
            assertEquals(1, result.get(0).getId());
            assertEquals("user1@test.com", result.get(0).getEmail());
            assertEquals("John", result.get(0).getFirstName());
            
            assertEquals(2, result.get(1).getId());
            assertEquals("user2@test.com", result.get(1).getEmail());
            assertTrue(result.get(1).getIsAdmin());
            
            verify(mockAuthenticator).authenticate(testToken); // Verify authenticator was called
            verify(mockPreparedStatement).executeQuery();
        }

        @Test
        void getAllCredentials_EmptyResult() throws SQLException {
            // Arrange
            when(mockResultSet.next()).thenReturn(false);

            // Act
            var result = credentialsDAO.getAllCredentials(testToken);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void getAllCredentials_SQLException() throws SQLException {
            // Arrange
            when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

            // Act
            var result = credentialsDAO.getAllCredentials(testToken);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class DeleteCredentialsTests {

        @BeforeEach
        void setupDeleteTests() throws Exception {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        }

        @Test
        void deleteCredentials_Success() {
            // Arrange
            int testId = 42;

            Token adminToken = createTestToken();

            // Act
            assertDoesNotThrow(() -> credentialsDAO.deleteCredentials(testId, adminToken));

            // Assert
            try {
                verify(mockPreparedStatement).setInt(1, testId);
                verify(mockPreparedStatement).executeUpdate();
                verify(mockConnection, times(1)).close();
            } catch (SQLException e) {
                fail("SQLException should not be thrown in test verification");
            }
        }

        @Test
        void deleteCredentials_SQLException() throws SQLException {
            // Arrange
            int testId = 42;
            Token adminToken = createTestToken();
            when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Delete failed"));

            // Act & Assert
            assertDoesNotThrow(() -> credentialsDAO.deleteCredentials(testId, adminToken));
        }

        @Test
        void deleteCredentials_ZeroId() {
            Token adminToken = createTestToken();
            // Act
            assertDoesNotThrow(() -> credentialsDAO.deleteCredentials(0, adminToken));

            // Assert
            try {
                verify(mockPreparedStatement, times(1)).setInt(1, 0);
            } catch (SQLException e) {
                fail("SQLException should not be thrown in test verification");
            }
        }

        @Test
        void deleteCredentials_NegativeId() {
            
            Token adminToken = createTestToken();
            
            // Act
            assertDoesNotThrow(() -> credentialsDAO.deleteCredentials(-1, adminToken));

            // Assert
            try {
                verify(mockPreparedStatement, times(1)).setInt(1, -1);
            } catch (SQLException e) {
                fail("SQLException should not be thrown in test verification");
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
        credentials.setUserRole("Employee");
        return credentials;
    }

    private Credentials createAdminCredentials() {
        Credentials credentials = new Credentials();
        credentials.setId(100);
        credentials.setEmail("admin@example.com");
        credentials.setPassword("adminPassword");
        credentials.setIsAdmin(true);
        credentials.setFirstName("Admin");
        credentials.setLastName("User");
        credentials.setTitle("Administrator");
        credentials.setDepartment("IT");
        credentials.setLocation("USA");
        credentials.setUserRole("Admin");
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
