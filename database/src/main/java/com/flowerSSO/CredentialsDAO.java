package com.flowerSSO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CredentialsDAO {

    String authServerUrl = "http://172.16.0.51:8080/auth_service/api/auth/verify";

    public CredentialsDAO() {

    }

    public void insertCredentials(Credentials credentials) throws SQLException {
        //      login cred for admin, cred for user

        // Authenticator auth = new AuthenticatorImpl(authServerUrl);
        // Credentials userCredentials = auth.authenticate(token);

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

    public void updateCredentials(Credentials credentials) throws SQLException {
        //      login cred for admin, cred for user

        // Authenticator auth = new AuthenticatorImpl(authServerUrl);
        // Credentials userCredentials = auth.authenticate(token);

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            String sql = """
                        UPDATE INTO Credentials (email, password, isAdmin, firstName, lastName, titleId, departmentId, locationId, userRoleId) 
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

    public void updatePassword() {
        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            String sql = "UPDATE INTO Credentials (password) VALUES (?) WHERE id=?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, newPassword);
            statement.setInt(2, id);

            statement.executeUpdate();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Credentials getCredentials(LoginCredentials loginCredentials) {

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
            resultSet.next();

            credentials = new Credentials();
            credentials.setId(resultSet.getInt("id"));
            credentials.setFirstName(resultSet.getString("firstName"));
            credentials.setLastName(resultSet.getString("lastName"));
            credentials.setTitle(resultSet.getString("title"));
            credentials.setDepartment(resultSet.getString("department"));
            credentials.setLocation(resultSet.getString("location"));
            credentials.setUserRole(resultSet.getString("userRole"));
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return credentials;
    }

    public List<Credentials> getAllCredentials() {
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

    public void deleteCredentials(int id) {
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
