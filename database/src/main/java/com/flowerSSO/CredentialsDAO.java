package com.flowerSSO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;


public class CredentialsDAO {

    private final Authenticator authenticator;
    private final Logger logger;
    private final Logger securityLogger;

    /**
     * Primary constructor with dependency injection for Authenticator.
     * This allows for better testability and configurability by injecting dependencies.
     * 
     * @param authenticator The authenticator to use for verifying tokens
     */
    public CredentialsDAO(Authenticator authenticator) {
        if (authenticator == null) {
            throw new IllegalArgumentException("Authenticator cannot be null");
        }
        this.authenticator = authenticator;
        this.logger = LoggerFactory.getEventLogger();
        this.securityLogger = LoggerFactory.getSecurityLogger();
        logger.info("CredentialsDAO initialized with custom authenticator");
    }

    /**
     * Convenience constructor that creates an AuthenticatorImpl using ConfigurationManager.
     * This constructor uses dependency injection for the configuration.
     * 
     * @param config The configuration manager to get auth server details
     */
    public CredentialsDAO(ConfigurationManager config) {
        if (config == null) {
            throw new IllegalArgumentException("ConfigurationManager cannot be null");
        }
        this.logger = LoggerFactory.getEventLogger();
        this.securityLogger = LoggerFactory.getSecurityLogger();
        try {
            String authServerUrl = String.format("%s:%d/%s", 
                                                config.getAuthServerHost(), 
                                                config.getAuthServerPort(), 
                                                config.getAuthServerSubdomain());
            this.authenticator = new AuthenticatorImpl(authServerUrl);
            logger.info("CredentialsDAO initialized with ConfigurationManager");
        } catch (Exception e) {
            logger.error("Failed to initialize authenticator: " + e.getMessage());
            throw new RuntimeException("Failed to initialize authenticator", e);
        }
    }

    /**
     * Default constructor that uses ConfigurationManagerImpl singleton.
     * This is provided for backward compatibility and simple usage.
     * For better testability, use the constructor that accepts dependencies.
     */
    public CredentialsDAO() {
        this(ConfigurationManagerImpl.getInstance());
    }

    public void insertCredentials(Credentials credentials, Token adminToken) {
        logger.debug("insertCredentials called for email: " + credentials.getEmail());
        
        if (!verifyIsAdmin(adminToken)) {
            securityLogger.warn("Unauthorized attempt to insert credentials for email: " + credentials.getEmail());
            throw new AuthenticationException("Only admin can insert a new user");
        }

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for insertCredentials");
            String sql = """
                        INSERT INTO Credentials (email, password, isAdmin, firstName, lastName, titleId, departmentId, locationId) 
                        VALUES (?, ?, ?, ?, ?, (SELECT id FROM Titles WHERE title=?), (SELECT id FROM Departments WHERE department=?), 
                        (SELECT id FROM Locations WHERE location=?));
                        """;

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, credentials.getEmail());
            statement.setString(2, credentials.getPassword());
            statement.setBoolean(3, credentials.getIsAdmin());
            statement.setString(4, credentials.getFirstName());
            statement.setString(5, credentials.getLastName());
            statement.setString(6, credentials.getTitle());
            statement.setString(7, credentials.getDepartment());
            statement.setString(8, credentials.getLocation());
            
            statement.executeUpdate();
            logger.info("Successfully inserted credentials for email: " + credentials.getEmail());

        }
        catch (SQLException e) {
            logger.error("SQLException in insertCredentials for email: " + credentials.getEmail() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateCredentials(Credentials credentials, Token adminToken) {
        logger.debug("updateCredentials called for email: " + credentials.getEmail());

       if (!verifyIsAdmin(adminToken)) {
            securityLogger.warn("Unauthorized attempt to update credentials for email: " + credentials.getEmail());
            throw new AuthenticationException("Only admin can insert a new user");
        }

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for updateCredentials");
            String sql = """
                        UPDATE Credentials SET 
                        email = ?, 
                        password = ?, 
                        isAdmin = ?, 
                        firstName = ?, 
                        lastName = ?, 
                        titleId = (SELECT id FROM Titles WHERE title=?), 
                        departmentId = (SELECT id FROM Departments WHERE department=?), 
                        locationId = (SELECT id FROM Locations WHERE location=?), 
                        WHERE id = ?;
                        ;""";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, credentials.getEmail());
            statement.setString(2, credentials.getPassword());
            statement.setBoolean(3, credentials.getIsAdmin());
            statement.setString(4, credentials.getFirstName());
            statement.setString(5, credentials.getLastName());
            statement.setString(6, credentials.getTitle());
            statement.setString(7, credentials.getDepartment());
            statement.setString(8, credentials.getLocation());
            statement.setInt(9, credentials.getId());
            
            statement.executeUpdate();
            logger.info("Successfully updated credentials for email: " + credentials.getEmail());

        }
        catch (SQLException e) {
            logger.error("SQLException in updateCredentials for email: " + credentials.getEmail() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updatePassword(String newPassword, Token userToken) {
        logger.debug("updatePassword called");
        Credentials credentials = authenticator.authenticate(userToken);
        logger.debug("Password update request for user ID: " + credentials.getId());
        credentials.setPassword(newPassword);

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for updatePassword");
            String sql = "UPDATE Credentials SET password = ? WHERE id=?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, credentials.getPassword());
            statement.setInt(2, credentials.getId());

            statement.executeUpdate();
            logger.info("Successfully updated password for user ID: " + credentials.getId());
            securityLogger.info("Password changed for user ID: " + credentials.getId());
        } 
        catch (SQLException e) {
            logger.error("SQLException in updatePassword for user ID: " + credentials.getId() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateTempPassword(String newTempPassword, String userEmail) {
        logger.debug("updateTempPassword called");
        LoginCredentials login = new LoginCredentials();
        login.setEmail(userEmail);
        logger.debug("Temporary password update request for email: " + login.getEmail());
        login.setTempPassword(newTempPassword);

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for updatePassword");
            String sql = "UPDATE Credentials SET tempPassword = ? WHERE email = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, login.getPassword());
            statement.setString(2, login.getEmail());

            statement.executeUpdate();
            logger.info("Successfully updated temporary password for email: " + login.getEmail());
            securityLogger.info("Temporary password changed for email: " + login.getEmail());
        } 
        catch (SQLException e) {
            logger.error("SQLException in updateTempPassword for email: " + login.getEmail() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Credentials getCredentialsFromToken(Token userToken) {
        logger.debug("getCredentialsFromToken called");
        Credentials credentials = authenticator.authenticate(userToken);
        logger.debug("Retrieving credentials for user ID: " + credentials.getId());

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for getCredentialsFromToken");

            String query = """
                    SELECT Credentials.email, Credentials.isAdmin 
                    FROM Credentials
                    WHERE Credentials.id=?;
                    """;

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, credentials.getId());
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                credentials.setEmail(resultSet.getString("email"));
                credentials.setIsAdmin(resultSet.getBoolean("isAdmin"));
                logger.info("Successfully retrieved credentials for user ID: " + credentials.getId());
            } else {
                logger.warn("No credentials found for user ID: " + credentials.getId());
            }
        }
        catch (SQLException e) {
            logger.error("SQLException in getCredentialsFromToken for user ID: " + credentials.getId() + " - " + e.getMessage());
            e.printStackTrace();
        }

        return credentials;
    }

    public Credentials getCredentialsFromLogin(LoginCredentials loginCredentials) {
        logger.debug("getCredentialsFromLogin called for email: " + loginCredentials.getEmail());

        Credentials credentials = null;

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for getCredentialsFromLogin");

            logger.error(String.format("LoginCredentials: %s", loginCredentials));
            
            String query = "SELECT Credentials.id, Credentials.password, Credentials.isAdmin, Credentials.firstName, Credentials.lastName, "
                        + "Titles.title, Departments.department, Locations.location "
                        + "FROM Credentials "
                        + "INNER JOIN Titles ON Credentials.titleID = Titles.id "
                        + "INNER JOIN Departments ON Credentials.departmentId = Departments.id "
                        + "INNER JOIN Locations ON Credentials.locationId = Locations.id "
                        + "WHERE email=?;";


            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, loginCredentials.getEmail());
            logger.error(String.format("%s", statement));
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next() && BCrypt.checkpw(loginCredentials.getPassword(), resultSet.getString("password"))) {
                credentials = new Credentials();
                credentials.setId(resultSet.getInt("id"));
                credentials.setFirstName(resultSet.getString("firstName"));
                credentials.setLastName(resultSet.getString("lastName"));
                credentials.setTitle(resultSet.getString("title"));
                credentials.setDepartment(resultSet.getString("department"));
                credentials.setLocation(resultSet.getString("location"));
                credentials.setEmail(loginCredentials.getEmail());
                credentials.setIsAdmin(resultSet.getBoolean("isAdmin"));
                logger.info("Successfully retrieved credentials for email: " + loginCredentials.getEmail());
                securityLogger.info("Successful login for email: " + loginCredentials.getEmail());
            } else {
                logger.info("Password doesn't match, checking tempPassword");

                query = "SELECT Credentials.id, Credentials.isAdmin, Credentials.tempPassword, Credentials.firstName, Credentials.lastName, "
                    + "Titles.title, Departments.department, Locations.location "
                    + "FROM Credentials "
                    + "INNER JOIN Titles ON Credentials.titleID = Titles.id "
                    + "INNER JOIN Departments ON Credentials.departmentId = Departments.id "
                    + "INNER JOIN Locations ON Credentials.locationId = Locations.id "
                    + "WHERE email=?;";

                statement = connection.prepareStatement(query);
                statement.setString(1, loginCredentials.getEmail());
                resultSet = statement.executeQuery();

                if (resultSet.next() && BCrypt.checkpw(loginCredentials.getPassword(), resultSet.getString("tempPassword"))) {
                    credentials = new Credentials();
                    credentials.setId(resultSet.getInt("id"));
                    credentials.setFirstName(resultSet.getString("firstName"));
                    credentials.setLastName(resultSet.getString("lastName"));
                    credentials.setTitle(resultSet.getString("title"));
                    credentials.setDepartment(resultSet.getString("department"));
                    credentials.setLocation(resultSet.getString("location"));
                    credentials.setEmail(loginCredentials.getEmail());
                    credentials.setIsAdmin(resultSet.getBoolean("isAdmin"));
                    logger.info("Successfully retrieved credentials for email: " + loginCredentials.getEmail());
                    securityLogger.info("Successful login for email: " + loginCredentials.getEmail());

                    query = "UPDATE Credentials SET tempPassword = NULL WHERE email=?";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, loginCredentials.getEmail());
                    statement.executeUpdate();
                    logger.info("Set tempPassword to NULL");
                } else {
                    logger.warn("Failed to find credentials for email: " + loginCredentials.getEmail());
                    securityLogger.warn("Failed login attempt for email: " + loginCredentials.getEmail());
                }
            }
        }
        catch (SQLException e) {
            logger.error("SQLException in getCredentialsFromLogin for email: " + loginCredentials.getEmail() + " - " + e.getMessage());
            e.printStackTrace();
        }

        return credentials;
    }

    public List<Credentials> getAllCredentials(Token adminToken) {
        logger.debug("getAllCredentials called");

        if (!verifyIsAdmin(adminToken)) {
            securityLogger.warn("Unauthorized attempt to retrieve all credentials");
            throw new AuthenticationException("Only admin can insert a new user");
        }

        List<Credentials> credList = new ArrayList<>();

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for getAllCredentials");
            
            String query = "SELECT Credentials.id, Credentials.email, Credentials.password, "
                        + "Credentials.isAdmin, Credentials.firstName, Credentials.lastName, "
                        + "Titles.title, Departments.department, Locations.location "
                        + "FROM Credentials "
                        + "INNER JOIN Titles ON Credentials.titleID = Titles.id "
                        + "INNER JOIN Departments ON Credentials.departmentId = Departments.id "
                        + "INNER JOIN Locations ON Credentials.locationId = Locations.id;";

            PreparedStatement statement = connection.prepareStatement(query);

            ResultSet resultSet = statement.executeQuery();
            
            Credentials credentials = null;
            while (resultSet.next()) {

                credentials = new Credentials();
                credentials.setId(resultSet.getInt("id"));
                credentials.setEmail(resultSet.getString("email"));
                credentials.setPassword(resultSet.getString("password"));
                credentials.setIsAdmin(resultSet.getBoolean("isAdmin"));
                credentials.setFirstName(resultSet.getString("firstName"));
                credentials.setLastName(resultSet.getString("lastName"));
                credentials.setTitle(resultSet.getString("title"));
                credentials.setDepartment(resultSet.getString("department"));
                credentials.setLocation(resultSet.getString("location"));

                credList.add(credentials);
            }
            logger.info("Successfully retrieved " + credList.size() + " credentials records");
        }
        catch (SQLException e) {
            logger.error("SQLException in getAllCredentials - " + e.getMessage());
            e.printStackTrace();
        }

        return credList;
    }

    public void deleteCredentials(int id, Token adminToken) {
        logger.debug("deleteCredentials called for user ID: " + id);
        
        if (!verifyIsAdmin(adminToken)) {
            securityLogger.warn("Unauthorized attempt to delete credentials for user ID: " + id);
            throw new AuthenticationException("Only admin can delete credentials");
        }

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for deleteCredentials");
            String sql = "DELETE FROM Credentials WHERE id=?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
            logger.info("Successfully deleted credentials for user ID: " + id);
            securityLogger.info("Admin deleted credentials for user ID: " + id);
        }
        catch (SQLException e) {
            logger.error("SQLException in deleteCredentials for user ID: " + id + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Boolean verifyIsAdmin(Token adminToken) {
        logger.debug("verifyIsAdmin called");
        Credentials adminCredentials = authenticator.authenticate(adminToken);
        logger.debug("Verifying admin status for user ID: " + adminCredentials.getId());

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            logger.debug("Database connection obtained for verifyIsAdmin");
            String sql = "SELECT isAdmin FROM Credentials WHERE id=?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, adminCredentials.getId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean isAdmin = resultSet.getBoolean("isAdmin");
                logger.debug("Admin verification result for user ID " + adminCredentials.getId() + ": " + isAdmin);
                if (isAdmin) {
                    securityLogger.info("Admin verification successful for user ID: " + adminCredentials.getId());
                } else {
                    securityLogger.info("Admin verification failed - user ID " + adminCredentials.getId() + " is not admin");
                }
                return isAdmin;
            }
            else {
                logger.warn("User ID not found in verifyIsAdmin: " + adminCredentials.getId());
                securityLogger.warn("Admin verification failed - user ID not found: " + adminCredentials.getId());
                return false;
            }
        }
        catch (SQLException e) {
            logger.error("SQLException in verifyIsAdmin for user ID: " + adminCredentials.getId() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
    }
    
}
