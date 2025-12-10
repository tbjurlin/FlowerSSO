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

        user.setEmail("user@example.com");
        user.setPassword("pAsSwOrD1234");
        user.setFirstName("John");
        user.setLastName("Johnson");
        user.setTitle("Manager");
        user.setDepartment("Sales");
        user.setLocation("Japan");
        user.setUserRole("Manager");

        cred.insertUser(user);
        Credentials userData = cred.getCredentialsByEmail(user.getEmail());

        System.out.println(userData.getLastName());


    

    }
}