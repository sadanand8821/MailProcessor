package org.example;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class EmailManager {

    private String username = "yourgmailaccount@gmail.com"; // Enter your Gmail
    private String password = "your app password"; // Enter your App Password. For this you will have to generate an app password from Google account settings

    public void processEmails() {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com");
        properties.put("mail.imaps.port", "993");

        try (Store store = Session.getInstance(properties).getStore()) {
            store.connect(username, password);
            Folder inbox = store.getFolder("[Gmail]/Sent Mail");
            inbox.open(Folder.READ_WRITE);
            //System.out.println("Folder Can be read succesfully");

            Message[] messages = inbox.getMessages();
            // Sort by date
            /*Arrays.sort(messages, (m1, m2) -> {
                try {
                    return m2.getSentDate().compareTo(m1.getSentDate());
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return 0;
            });*/

            for (Message message : messages) {
                if (message.getSubject() != null){
                    String subject = message.getSubject().trim();
                    System.out.println(subject);
                    if (subject.contains("Order")) {
                        forwardEmail(message);
                        saveEmailLocally(message);
                    }
                }
            }
            inbox.close(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void forwardEmail(Message message) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password); // Use your Gmail and App Password
            }
        });

        Message forward = new MimeMessage(session);
        forward.setSubject("Fwd: " + message.getSubject());
        forward.setFrom(new InternetAddress(username));
        forward.addRecipient(Message.RecipientType.TO, new InternetAddress("forwardTo@email.com"));

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message.getContent(), message.getContentType());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        forward.setContent(multipart);
        Transport.send(forward);
        //System.out.println("Forwarded");
    }


    private void saveEmailLocally(Message message) {
        try (FileOutputStream out = new FileOutputStream(message.getSubject() + ".eml")) {
            message.writeTo(out);
            //System.out.println("Saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
