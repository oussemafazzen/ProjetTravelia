package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private String username;
    private String password;
    private final Properties mailProps;

    public EmailService() {
        mailProps = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        try {
            Properties configProps = new Properties();
            InputStream is = getClass().getResourceAsStream("/config.properties");
            if (is != null) {
                configProps.load(is);
                username = configProps.getProperty("smtp.email", "VOTRE_EMAIL_ICI");
                password = configProps.getProperty("smtp.password", "VOTRE_MOT_DE_PASSE_APP_ICI");
            }
        } catch (IOException e) {
            System.err.println("Erreur chargement config email: " + e.getMessage());
            username = "VOTRE_EMAIL_ICI";
            password = "VOTRE_MOT_DE_PASSE_APP_ICI";
        }

        mailProps.put("mail.smtp.host", "smtp.gmail.com");
        mailProps.put("mail.smtp.port", "587");
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
    }

    private Session getSession() {
        if (username == null || password == null) return null;
        return Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public boolean sendWelcomeEmail(String toEmail, String name) {
        String subject = "Bienvenue chez Travelia !";
        String body = "Bonjour " + name + ",\n\n" +
                "Votre compte a été créé avec succès. Bienvenue dans l'aventure Travelia !\n\n" +
                "Cordialement,\nL'équipe Travelia";
        return sendEmail(toEmail, subject, body);
    }

    public boolean sendPasswordResetEmail(String toEmail, String resetToken) {
        String subject = "Réinitialisation de votre mot de passe - Travelia";
        String body = "Bonjour,\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe.\n" +
                "Votre code de vérification est : " + resetToken + "\n\n" +
                "Ce code expire dans 1 heure.\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer cet email.\n\n" +
                "Cordialement,\nL'équipe Travelia";
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Envoie un email de confirmation d'inscription avec un token sécurisé.
     */
    public boolean sendConfirmationEmail(String toEmail, String name, String confirmToken) {
        String subject = "Confirmez votre inscription - Travelia";
        String body = "Bonjour " + name + ",\n\n" +
                "Merci de vous être inscrit chez Travelia !\n\n" +
                "Veuillez confirmer votre adresse email en utilisant le code suivant :\n\n" +
                "Code de confirmation : " + confirmToken + "\n\n" +
                "Ce code expire dans 1 heure.\n\n" +
                "Cordialement,\nL'équipe Travelia";
        return sendEmail(toEmail, subject, body);
    }

    private boolean sendEmail(String toEmail, String subject, String body) {
        try {
            Session session = getSession();
            if (session == null) {
                System.err.println("Session SMTP non initialisée. Vérifiez config.properties.");
                return false;
            }
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + toEmail);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            return false;
        }
    }
}
