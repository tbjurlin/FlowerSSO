package com.flowerSSO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class LoadSqlFile {

    public static void sourceSqlFile(String path) throws SQLException, IOException {

        String projectDir = System.getProperty("user.dir");
        File filePath = new File(projectDir + File.separator + path);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        StringBuilder query = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {

            query.append(line);
        }

        

        reader.close();



        try (Connection connection = DatabaseConnectionPool.getConnection()) {      
            Statement statement = connection.createStatement();
            statement.execute(query.toString());
            String[] commands = query.toString().split(";");

            for (String command : commands) {
                try {
                    statement.execute(command);
                } catch (Exception e) {
                    String message = e.getMessage();
                    if (! message.endsWith("does not exist.")) {
                        System.out.println(command);
                        System.out.println(e.getMessage());
                    }
                }

            }
        }

    }
}
