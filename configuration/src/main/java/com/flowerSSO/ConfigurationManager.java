package com.flowerSSO;

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

/**
 * 
 * @author Ben Edens
 * @version 1.0
 */
public interface ConfigurationManager {

    /**
     * Get the allowed origins for CORS configuration.
     * @return Allowed CORS origins.
     */
    public abstract String getCorsAllowedOrigins();

    /**
     * Get the database host.
     * @return The database host.
     */
    public abstract String getDatabaseHost();

    /**
     * Get the database port.
     * @return The database port.
     */
    public abstract int getDatabasePort();

    /**
     * Get the database name.
     * @return The database name.
     */
    public abstract String getDatabaseName();

    public abstract String getDatabaseUrl();

    /**
     * Get the database username.
     * @return The database username.
     */
    public abstract String getDatabaseUsername();

    /**
     * Get the database password.
     * @return The database password.
     */
    public abstract String getDatabasePassword();

    /**
     * Get the database connection pool minimum idle connections.
     * @return Minimum idle connections.
     */
    public abstract int getDatabasePoolMinIdle();

    /**
     * Get the database connection pool maximum idle connections.
     * @return Maximum idle connections.
     */
    public abstract int getDatabasePoolMaxIdle();

    /**
     * Get the database connection pool maximum total connections.
     * @return Maximum total connections.
     */
    public abstract int getDatabasePoolMaxTotal();

    /**
     * Get the database connection pool maximum wait time in milliseconds.
     * @return Maximum wait time in milliseconds.
     */
    public abstract int getDatabasePoolMaxWaitMillis();

    /**
     * Get the authentication server host.
     * @return The authentication server host.
     */
    public abstract String getAuthServerHost();

    /**
     * Get the authentication server port.
     * @return The authentication server port.
     */
    public abstract int getAuthServerPort();

    /**
     * Get the authentication server subdomain.
     * @return The authentication server subdomain.
     */
    public abstract String getAuthServerSubdomain();

    /**
     * Get the authentication server URL.
     * @return The authentication server URL.
     */
    public abstract String getAuthServerUrl();

    /**
     * Get the email username.
     * @return The email username.
     */
    public abstract String getEmailUsername();

    /**
     * Get the email password.
     * @return The email password.
     */
    public abstract String getEmailPassword();

    /**
     * Get the email SMTP host.
     * @return The email SMTP host.
     */
    public abstract String getEmailSmtpHost();

    /**
     * Get the email SMTP port.
     * @return The email SMTP port.
     */
    public abstract int getEmailSmtpPort();

    /**
     * Get whether SMTP authentication is enabled.
     * @return true if SMTP authentication is enabled, false otherwise.
     */
    public abstract boolean getEmailSmtpAuth();

    /**
     * Get whether SMTP STARTTLS is enabled.
     * @return true if SMTP STARTTLS is enabled, false otherwise.
     */
    public abstract boolean getEmailStarttlsEnable();

    /**
     * Get the email SSL protocols.
     * @return The email SSL protocols.
     */
    public abstract String getEmailSslProtocols();

    /**
     * Get the email SSL trust.
     * @return The email SSL trust.
     */
    public abstract String getEmailSslTrust();
}
