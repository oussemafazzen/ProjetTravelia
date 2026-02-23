package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private String username = "VOTRE_EMAIL_ICI";
    private String password = "VOTRE_MOT_DE_PASSE_ICI";
    private final Properties props;

    public EmailService() {
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
    }

    private Session getSession() {
        if (username == null || password == null) return null;
        return Session.getInstance(props, new Authenticator() {
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

    public boolean sendPasswordResetEmail(String toEmail, String resetCode) {
        String subject = "Réinitialisation de votre mot de passe - Travelia";
        String body = "Bonjour,\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe.\n" +
                "Votre code de vérification est : " + resetCode + "\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer cet email.\n\n" +
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
