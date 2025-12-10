package com.flowerSSO;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CredentialsDAO {

    Connection db = null;

    public CredentialsDAO() {

    }


    public void insertUser(Credentials credentials) throws SQLException {

        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            Statement statement = connection.createStatement();
            String sql = "INSERT INTO users (email, password, firstName, lastName, title, department, location, userRole)"
                        + "VALUES ('"
                        + credentials.getEmail() + "', '" + credentials.getPassword() + "', '" 
                        + credentials.getFirstName() + "', '" + credentials.getLastName() + "', "
                        + "SELECT id FROM Titles WHERE title='" + credentials.getTitle() + "', " 
                        + "SELECT id FROM Departments WHERE department='" + credentials.getDepartment() + "', "
                        + "SELECT id FROM Locations WHERE location='" + credentials.getLocation() + "', "
                        + "SELECT id FROM UserRoles WHERE userRole='" + credentials.getUserRole() + "'"
                        + ");";
            
            
            statement.execute(sql);

        }
    }

    public Credentials getCredentialsByEmail(String email) throws SQLException {
        Credentials credentials = null;
        try (Connection connection = DatabaseConnectionPool.getConnection()) {
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM Credentials WHERE email=" + email;
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();

            credentials = new Credentials();
            credentials.setId(resultSet.getInt("id"));
            credentials.setFirstName(resultSet.getString("firstName"));
            credentials.setLastName(resultSet.getString("lastName"));
            credentials.setTitle(resultSet.getString("title"));
            credentials.setDepartment(resultSet.getString("department"));
            credentials.setLocation(resultSet.getString("location"));
        }
        catch (Exception e) {
            // TODO: handle exception
        }

        return credentials;

    }
    
}
