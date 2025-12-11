package com.flowerSSO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CredentialsDAO {

    private final Authenticator authenticator;

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
        try {
            String authServerUrl = String.format("%s:%d/%s", 
                                                config.getAuthServerHost(), 
                                                config.getAuthServerPort(), 
                                                config.getAuthServerSubdomain());
            this.authenticator = new AuthenticatorImpl(authServerUrl);
        } catch (Exception e) {
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
        
        if (!verifyIsAdmin(adminToken)) {
            throw new AuthenticationException("Only admin can insert a new user");
        }

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            String sql = """
                        INSERT INTO Credentials (email, password, isAdmin, firstName, lastName, titleId, departmentId, locationId, userRoleId) 
                        VALUES (?, ?, ?, ?, ?, (SELECT id FROM Titles WHERE title=?), (SELECT id FROM Departments WHERE department=?), 
                        (SELECT id FROM Locations WHERE location=?), (SELECT id FROM UserRoles WHERE userRole=?));
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
            statement.setString(9, credentials.getUserRole());
            
            statement.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCredentials(Credentials credentials, Token adminToken) {
        //      login cred for admin, cred for user

       if (!verifyIsAdmin(adminToken)) {
            throw new AuthenticationException("Only admin can insert a new user");
        }

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            String sql = """
                        UPDATE Credentials (email, password, isAdmin, firstName, lastName, titleId, departmentId, locationId, userRoleId) 
                        VALUES (?, ?, ?, ?, ?, (SELECT id FROM Titles WHERE title=?), (SELECT id FROM Departments WHERE department=?), 
                        (SELECT id FROM Locations WHERE location=?), (SELECT id FROM UserRoles WHERE userRole=?)) 
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
            statement.setString(9, credentials.getUserRole());
            
            statement.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePassword(String newPassword, Token userToken) {
        Credentials credentials = authenticator.authenticate(userToken);
        credentials.setPassword(newPassword);

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            String sql = "UPDATE Credentials (password) VALUES (?) WHERE id=?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, credentials.getPassword());
            statement.setInt(2, credentials.getId());

            statement.executeUpdate();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Credentials getCredentialsFromToken(Token userToken) {
        Credentials credentials = authenticator.authenticate(userToken);

        try (Connection connection = DatabaseConnectionPool.getConnection()) {

            String query = """
                    SELECT Credentials.email, Credentials.password, Credentials.isAdmin, 
                    UserRoles.userRole FROM Credentials 
                    INNER JOIN UserRoles ON Credentials.userRoleId = UserRoles.id 
                    WHERE Credentials.id=?;
                    """;

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, credentials.getId());
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                credentials.setEmail(resultSet.getString("email"));
                credentials.setPassword(resultSet.getString("password"));
                credentials.setIsAdmin(resultSet.getBoolean("isAdmin"));
                credentials.setUserRole(resultSet.getString("userRole"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return credentials;
    }

    public Credentials getCredentialsFromLogin(LoginCredentials loginCredentials) {

        Credentials credentials = null;

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            
            String query = "SELECT Credentials.id, Credentials.firstName, Credentials.lastName, "
                        + "Titles.title, Departments.department, Locations.location, "
                        + "UserRoles.userRole FROM Credentials "
                        + "INNER JOIN Titles ON Credentials.titleID = Titles.id "
                        + "INNER JOIN Departments ON Credentials.departmentId = Departments.id "
                        + "INNER JOIN Locations ON Credentials.locationId = Locations.id "
                        + "INNER JOIN UserRoles ON Credentials.userRoleId = UserRoles.id "
                        + "WHERE email=? AND password=?;";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, loginCredentials.getEmail());
            statement.setString(2, loginCredentials.getPassword());
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                credentials = new Credentials();
                credentials.setId(resultSet.getInt("id"));
                credentials.setFirstName(resultSet.getString("firstName"));
                credentials.setLastName(resultSet.getString("lastName"));
                credentials.setTitle(resultSet.getString("title"));
                credentials.setDepartment(resultSet.getString("department"));
                credentials.setLocation(resultSet.getString("location"));
                credentials.setUserRole(resultSet.getString("userRole"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return credentials;
    }

    public List<Credentials> getAllCredentials(Token adminToken) {

        if (!verifyIsAdmin(adminToken)) {
            throw new AuthenticationException("Only admin can insert a new user");
        }

        List<Credentials> credList = new ArrayList<>();

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            
            String query = "SELECT Credentials.id, Credentials.email, Credentials.password, "
                        + "Credentials.isAdmin, Credentials.firstName, Credentials.lastName, "
                        + "Titles.title, Departments.department, Locations.location, "
                        + "UserRoles.userRole FROM Credentials "
                        + "INNER JOIN Titles ON Credentials.titleID = Titles.id "
                        + "INNER JOIN Departments ON Credentials.departmentId = Departments.id "
                        + "INNER JOIN Locations ON Credentials.locationId = Locations.id "
                        + "INNER JOIN UserRoles ON Credentials.userRoleId = UserRoles.id ;";

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
                credentials.setUserRole(resultSet.getString("userRole"));

                credList.add(credentials);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return credList;
    }

    public void deleteCredentials(int id, Token adminToken) {
        if (!verifyIsAdmin(adminToken)) {
            throw new AuthenticationException("Only admin can delete credentials");
        }

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            String sql = "DELETE * FROM Credentials WHERE id=?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean verifyIsAdmin(Token adminToken) {
        Credentials adminCredentials = authenticator.authenticate(adminToken);

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            String sql = "SELECT isAdmin FROM Credentials WHERE id=?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, adminCredentials.getId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("isAdmin");
            }
            else {
                return false;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
    }

    // X Update all fields
    //      login cred for admin, cred and id for user

    // X Check user name and password return bool

    // isAdmin and in db return bool
    

    // X delete user
    //      login cred for admin, id for user

    // X listall

    // user change password

    //authServerUrl = "http://172.16.0.51:8080/auth_service/api/auth/verify";
    // Authenticator auth = new AuthenticatorImpl(authServerUrl);
    //     Credentials userCredentials = auth.authenticate(token);

    
}
