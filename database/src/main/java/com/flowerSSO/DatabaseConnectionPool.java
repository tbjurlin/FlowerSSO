package com.flowerSSO;

import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;

public class DatabaseConnectionPool {

    private static BasicDataSource dataSource;

    static {
        ConfigurationManager config = ConfigurationManagerImpl.getInstance();
        
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // Construct JDBC URL from configuration
        String jdbcUrl = config.getDatabaseHost() + ":" + config.getDatabasePort() + "/" + config.getDatabaseName();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(config.getDatabaseUsername());
        dataSource.setPassword(config.getDatabasePassword());

        // Connection pool settings from configuration
        dataSource.setMinIdle(config.getDatabasePoolMinIdle());
        dataSource.setMaxIdle(config.getDatabasePoolMaxIdle());
        dataSource.setMaxTotal(config.getDatabasePoolMaxTotal());
        dataSource.setMaxWaitMillis(config.getDatabasePoolMaxWaitMillis());
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
