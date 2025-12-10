package com.flowerSSO;

import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;

public class DatabaseConnectionPool {

    private static BasicDataSource dataSource;

    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/flowerdb";
    private static final String username = "root";
    private static final String password = "root";

    static {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setMinIdle(5);
        dataSource.setMaxIdle(10);
        dataSource.setMaxTotal(20);
        dataSource.setMaxWaitMillis(1000);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeDataSource() throws SQLException {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
