package com.flowerSSO;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CredentialsDAO {

    public CredentialsDAO() {

    }

    public void insertUser(Credentials credentials) throws SQLException {

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            String sql = """
                        INSERT INTO Credentials (email, password, firstName, lastName, titleId, departmentId, locationId, userRoleId) 
                        VALUES (?, ?, ?, ?, (SELECT id FROM Titles WHERE title=?), (SELECT id FROM Departments WHERE department=?), 
                        (SELECT id FROM Locations WHERE location=?), (SELECT id FROM UserRoles WHERE userRole=?));
                        """;

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, credentials.getEmail());
            statement.setString(2, credentials.getPassword());
            statement.setString(3, credentials.getFirstName());
            statement.setString(4, credentials.getLastName());
            statement.setString(5, credentials.getTitle());
            statement.setString(6, credentials.getDepartment());
            statement.setString(7, credentials.getLocation());
            statement.setString(8, credentials.getUserRole());
            
            statement.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Credentials getCredentialsByEmail(String email) throws SQLException {
        Credentials credentials = null;
        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            
            String query = "SELECT Credentials.id, Credentials.email, Credentials.password, "
                        + "Credentials.firstName, Credentials.lastName, "
                        + "Titles.title, Departments.department, Locations.location, "
                        + "UserRoles.userRole FROM Credentials "
                        + "INNER JOIN Titles ON Credentials.titleID = Titles.id "
                        + "INNER JOIN Departments ON Credentials.departmentId = Departments.id "
                        + "INNER JOIN Locations ON Credentials.locationId = Locations.id "
                        + "INNER JOIN UserRoles ON Credentials.userRoleId = UserRoles.id "
                        + "WHERE email=?;";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
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
    
}
