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

import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    final static String username = "your gmail here";
    final static String password = "your password here";
    final static String smtpHost = "smtp.gmail.com";
    final static String smtpPort = "587";
    final static String smtpAuth = "true";
    final static String starttlsEnable = "true";
    final static String sslProtocols = "TLSv1.2";
    final static String sslTrust = "smtp.gmail.com";
    final static XssSanitizer sanitizer = new XssSanitizerImpl();

    private static void sendEmail(String recipient, String subject, String body, String contentType) {
        recipient = sanitizer.sanitizeInput(recipient);
        subject = sanitizer.sanitizeInput(subject);
        // Note: body is NOT sanitized to preserve HTML formatting for email templates
        contentType = sanitizer.sanitizeInput(contentType);

        // Set up mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.ssl.protocols", sslProtocols);
        props.put("mail.smtp.ssl.trust", sslTrust);

        // Create a mail session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Compose and send the email
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setContent(body, contentType);

            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public static void sendTempPasswordEmail(String recipient, String tempPassword) {
        tempPassword = sanitizer.sanitizeInput(tempPassword);
        String subject = "IMPORTANT: Your Password Reset Information For FlowerSSO";
        String body = String.format("Hello!<br><br>"
                                    +"It looks like you forgot your password, but we've got you covered.<br>"
                                    +"Your new one-time password is: %s<br><br>"
                                    +"This will allow you to sign back into your account, but only ONCE.<br>"
                                    +"*Your original password has NOT been changed.*<br>"
                                    +"Be sure to make a note of your original password or create a new one after logging in.<br><br>"
                                    +"Not you? Someone may be trying to gain unauthorized access to your account.<br>"
                                    +"We recommend logging in to FlowerSSO as soon as possible with your one-time password to deactivate it just in case.<br><br>"
                                    +"Best regards,<br>FlowerSSO Team<br><br><br>"
                                    +"Concerned this may be a phishing attempt? We have gone to great lengths to ensure your peace of mind:<br>"
                                    +"1. Only the real FlowerSSO can create a password that will truly allow you to log back in on the official site.<br>"
                                    +"2. No phishy links here! Sign in directly at the regular FlowerSSO URL you know and trust.<br>"
                                    +"3. We will never ask you for your information via email.<br>"
                                    +"4. Still unsure? Contact your administrator to verify this email's authenticity.<br><br><br>", tempPassword);
        sendEmail(recipient, subject, body, "text/html; charset=utf-8");
    }
}
