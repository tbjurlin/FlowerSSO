package com.flowerSSO;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        System.out.println("Hello world!");
        CredentialsDAO cred = new CredentialsDAO();


        // cred.insertUser();
        // cred.getUser();

        LoadSqlFile.sourceSqlFile("database/src/main/resources/sql/schema.sql");

        Credentials user = new Credentials();
        LoginCredentials userLogin = new LoginCredentials();

        user.setEmail("user@example.com");
        user.setPassword("pAsSwOrD1234");
        user.setIsAdmin(true);
        user.setFirstName("John");
        user.setLastName("Johnson");
        user.setTitle("Manager");
        user.setDepartment("Sales");
        user.setLocation("Japan");
        user.setUserRole("Manager");

        userLogin.setEmail("user@example.com");
        userLogin.setPassword("pAsSwOrD1234");

        cred.insertCredentials(user);
        Credentials userData = cred.getCredentials(userLogin);

        System.out.println(userData.getLocation());


    

    }
}