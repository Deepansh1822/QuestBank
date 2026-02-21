package in.sfp.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import in.sfp.main.dto.ContactRequest;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // The email address where contact form submissions should be sent
    private final String ADMIN_EMAIL = "deepanshshakya669@gmail.com";

    public void sendContactEmail(ContactRequest contactRequest) {
        SimpleMailMessage message = new SimpleMailMessage();

        // We set the FROM as the system email (authenticated user)
        // But we can put the user's email in the REPLY-TO or in the body
        message.setFrom(senderEmail);
        message.setTo(ADMIN_EMAIL);
        message.setSubject("New Contact Query: " + contactRequest.getSubject());

        String body = String.format("""
                New Contact Form Submission

                Name: %s
                Email: %s
                Subject: %s

                Message:
                %s
                """,
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getSubject(),
                contactRequest.getMessage());

        message.setText(body);
        message.setReplyTo(contactRequest.getEmail()); // So admin can reply directly to the user

        mailSender.send(message);
    }
}
