package com.flowerSSO;

import java.util.Properties;

/*
 * This is free and unencumbered software released into the public domain.
 * Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software,
 * either in source code form or as a compiled binary, for any purpose, commercial or
 * non-commercial, and by any means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors of this
 * software dedicate any and all copyright interest in the software to the public domain.
 * We make this dedication for the benefit of the public at large and to the detriment of
 * our heirs and successors. We intend this dedication to be an overt act of relinquishment in
 * perpetuity of all present and future rights to this software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to: https://unlicense.org/
*/

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 
 * @author Ben Edens
 * @version 1.0
 */
public class ConfigurationManagerImpl implements ConfigurationManager{

    private Properties propertiesFile = new Properties();

    private final Logger logger = LoggerFactory.getEventLogger();

    private static ConfigurationManagerImpl instance;

    /**
     * Constructs a ConfigurationManager.
     * <p>
     * Constructs a configuration instance, reading configuration information from the
     * application config file. The manager first looks for a FLOWERSSO_CONFIG environmental
     * variable specifying the configuration path. If this is not found, it looks for
     * a config file in the working directory.
     * @throws ConfigurationException if there is a configuration loading error
     */
    private ConfigurationManagerImpl()  {
        String configPath = System.getenv("FLOWERSSO_CONFIG");
        if (configPath == null) {
            configPath = "flowerSSO.properties";
        }
        logger.info("Looking for configuration file at path " + configPath);
    
        try {
            InputStream stream = new FileInputStream(configPath);
            propertiesFile.load(stream);
        } catch (FileNotFoundException e) {
            logger.error("No configuration file found.");
            throw new ConfigurationException("No configuration file found.");
        } catch (IOException e) {
            logger.error("Malformed configuration file.");
            throw new ConfigurationException("Malformed configuration file.");
        }
    }

    /**
     * Gets a reference to the ConfigurationManager.
     * @return a reference to the singleton instance of the ConfigurationManager
     */
    public static ConfigurationManagerImpl getInstance() {
        if (instance == null) {
            instance = new ConfigurationManagerImpl();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCorsAllowedOrigins() {
        String corsAllowedOrigins = propertiesFile.getProperty("cors.allowedOrigins");
        if(corsAllowedOrigins == null || corsAllowedOrigins.isEmpty()) {
            logger.error("CORS allowed origins is not configured.");
            throw new ConfigurationException("CORS allowed origins is not configured.");
        }
        return corsAllowedOrigins;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabaseHost() {
        String dbHost = propertiesFile.getProperty("database.host");
        if(dbHost == null || dbHost.isEmpty()) {
            logger.error("Database host is not configured.");
            throw new ConfigurationException("Database host is not configured.");
        }
        return dbHost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDatabasePort() {
        int dbPort = -1;
        try {
            dbPort = Integer.parseInt(propertiesFile.getProperty("database.port"));
        } catch (NumberFormatException e) {
            logger.error("Database port is not a valid integer.");
            throw new ConfigurationException("Database port is not a valid integer.");
        }
        if(dbPort <= 0 || dbPort > 65535) {
            logger.error("Database port is not configured.");
            throw new ConfigurationException("Database port is not configured.");
        }
        return dbPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabaseName() {
        String dbName = propertiesFile.getProperty("database.name");
        if(dbName == null || dbName.isEmpty()) {
            logger.error("Database name is not configured.");
            throw new ConfigurationException("Database name is not configured.");
        }
        return dbName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabaseUrl() {
        String dbHost = getDatabaseHost();
        int dbPort = getDatabasePort();
        String dbName = getDatabaseName();
        return String.format("%s:%d/%s", dbHost, dbPort, dbName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabaseUsername() {
        String dbUsername = propertiesFile.getProperty("database.username");
        if(dbUsername == null || dbUsername.isEmpty()) {
            logger.error("Database username is not configured.");
            throw new ConfigurationException("Database username is not configured.");
        }
        return dbUsername;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDatabasePassword() {
        String dbPassword = propertiesFile.getProperty("database.password");
        if(dbPassword == null || dbPassword.isEmpty()) {
            logger.error("Database password is not configured.");
            throw new ConfigurationException("Database password is not configured.");
        }
        return dbPassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDatabasePoolMinIdle() {
        int dbMinIdle = -1;
        try {
            dbMinIdle = Integer.parseInt(propertiesFile.getProperty("database.pool.minIdle"));
        } catch (NumberFormatException e) {
            logger.error("Database pool minIdle is not a valid integer.");
            throw new ConfigurationException("Database pool minIdle is not a valid integer.");
        }
        if(dbMinIdle < 0) {
            logger.error("Database pool minIdle is not configured.");
            throw new ConfigurationException("Database pool minIdle is not configured.");
        }
        return dbMinIdle;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getDatabasePoolMaxIdle() {
        int dbMaxIdle = -1;
        try {
            dbMaxIdle = Integer.parseInt(propertiesFile.getProperty("database.pool.maxIdle"));
        } catch (NumberFormatException e) {
            logger.error("Database pool maxIdle is not a valid integer.");
            throw new ConfigurationException("Database pool maxIdle is not a valid integer.");
        }
        int dbMinIdle = getDatabasePoolMinIdle();
        if(dbMaxIdle < 0 || dbMaxIdle < dbMinIdle) {
            logger.error("Database pool maxIdle is not configured.");
            throw new ConfigurationException("Database pool maxIdle is not configured.");
        }
        return dbMaxIdle;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getDatabasePoolMaxTotal() {
        int dbMaxTotal = -1;
        try {
            dbMaxTotal = Integer.parseInt(propertiesFile.getProperty("database.pool.maxTotal"));
        } catch (NumberFormatException e) {
            logger.error("Database pool maxTotal is not a valid integer.");
            throw new ConfigurationException("Database pool maxTotal is not a valid integer.");
        }
        int dbMinIdle = getDatabasePoolMinIdle();
        if(dbMaxTotal <= 0 || dbMaxTotal < dbMinIdle) {
            logger.error("Database pool maxTotal is not configured.");
            throw new ConfigurationException("Database pool maxTotal is not configured.");
        }
        return dbMaxTotal;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getDatabasePoolMaxWaitMillis() {
        int dbMaxWaitMillis = -1;
        try {
            dbMaxWaitMillis = Integer.parseInt(propertiesFile.getProperty("database.pool.maxWaitMillis"));
        } catch (NumberFormatException e) {
            logger.error("Database pool maxWaitMillis is not a valid integer.");
            throw new ConfigurationException("Database pool maxWaitMillis is not a valid integer.");
        }
        if(dbMaxWaitMillis < 0) {
            logger.error("Database pool maxWaitMillis is not configured.");
            throw new ConfigurationException("Database pool maxWaitMillis is not configured.");
        }
        return dbMaxWaitMillis;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthServerHost() {
        String authUrl = propertiesFile.getProperty("authentication.host");
        if(authUrl == null || authUrl.isEmpty()) {
            logger.error("Auth server URL is not configured.");
            throw new ConfigurationException("Auth server URL is not configured.");
        }
        return authUrl;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getAuthServerPort() {
        int authPort = -1;
        try {
            authPort = Integer.parseInt(propertiesFile.getProperty("authentication.port"));
        } catch (NumberFormatException e) {
            logger.error("Auth server port is not a valid integer.");
            throw new ConfigurationException("Auth server port is not a valid integer.");
        }
        if(authPort <= 0 || authPort > 65535) {
            logger.error("Auth server port is not configured.");
            throw new ConfigurationException("Auth server port is not configured.");
        }
        return authPort;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthServerSubdomain() {
        String authSubdomain = propertiesFile.getProperty("authentication.subdomain");
        if(authSubdomain == null || authSubdomain.isEmpty()) {
            logger.error("Auth server subdomain is not configured.");
            throw new ConfigurationException("Auth server subdomain is not configured.");
        }
        return authSubdomain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthServerUrl() {
        String authHost = getAuthServerHost();
        int authPort = getAuthServerPort();
        String authSubdomain = getAuthServerSubdomain();
        return String.format("%s:%d/%s", authHost, authPort, authSubdomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailUsername() {
        String emailUsername = propertiesFile.getProperty("email.username");
        if(emailUsername == null || emailUsername.isEmpty()) {
            logger.error("Email username is not configured.");
            throw new ConfigurationException("Email username is not configured.");
        }
        return emailUsername;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailPassword() {
        String emailPassword = propertiesFile.getProperty("email.password");
        if(emailPassword == null || emailPassword.isEmpty()) {
            logger.error("Email password is not configured.");
            throw new ConfigurationException("Email password is not configured.");
        }
        return emailPassword;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailSmtpHost() {
        String emailSmtpHost = propertiesFile.getProperty("email.smtp.host");
        if(emailSmtpHost == null || emailSmtpHost.isEmpty()) {
            logger.error("Email SMTP host is not configured.");
            throw new ConfigurationException("Email SMTP host is not configured.");
        }
        return emailSmtpHost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEmailSmtpPort() {
        int emailSmtpPort = -1;
        try {
            emailSmtpPort = Integer.parseInt(propertiesFile.getProperty("email.smtp.port"));
        } catch (NumberFormatException e) {
            logger.error("Email SMTP port is not a valid integer.");
            throw new ConfigurationException("Email SMTP port is not a valid integer.");
        }
        if(emailSmtpPort <= 0 || emailSmtpPort > 65535) {
            logger.error("Email SMTP port is not configured.");
            throw new ConfigurationException("Email SMTP port is not configured.");
        }
        return emailSmtpPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEmailSmtpAuth() {
        String emailSmtpAuth = propertiesFile.getProperty("email.smtp.auth");
        if(emailSmtpAuth == null || emailSmtpAuth.isEmpty()) {
            logger.error("Email SMTP auth is not configured.");
            throw new ConfigurationException("Email SMTP auth is not configured.");
        }
        return Boolean.parseBoolean(emailSmtpAuth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEmailStarttlsEnable() {
        String emailSmtpStarttls = propertiesFile.getProperty("email.starttls.enable");
        if(emailSmtpStarttls == null || emailSmtpStarttls.isEmpty()) {
            logger.error("Email SMTP STARTTLS is not configured.");
            throw new ConfigurationException("Email SMTP STARTTLS is not configured.");
        }
        return Boolean.parseBoolean(emailSmtpStarttls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailSslProtocols() {
        String emailSslProtocols = propertiesFile.getProperty("email.ssl.protocols");
        if(emailSslProtocols == null || emailSslProtocols.isEmpty()) {
            logger.error("Email SSL protocols is not configured.");
            throw new ConfigurationException("Email SSL protocols is not configured.");
        }
        return emailSslProtocols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailSslTrust() {
        String emailSslTrust = propertiesFile.getProperty("email.ssl.trust");
        if(emailSslTrust == null || emailSslTrust.isEmpty()) {
            logger.error("Email SSL trust is not configured.");
            throw new ConfigurationException("Email SSL trust is not configured.");
        }
        return emailSslTrust;
    }



}
