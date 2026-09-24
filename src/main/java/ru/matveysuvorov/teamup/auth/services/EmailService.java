package ru.matveysuvorov.teamup.auth.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendVerificationEmail(String email, String token) {
        String verificationUrl = "http://localhost:8080/auth/verify-email?token="+token;
        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);
        String html = templateEngine.process("email/verification", context);
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );
            helper.setTo(email);
            helper.setSubject("Добро пожаловать в Teamup!");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(
                    "Failed to send email for email verification",
                    e
            );
        }
    }

    public void sendResetPasswordEmail(String email, UUID token) {
        String resetPasswordUrl = String.format("http://localhost:3000/password/reset?email=%s&token=%s", email, token.toString());
        Context context = new Context();
        context.setVariable("recoveryUrl", resetPasswordUrl);
        String html = templateEngine.process("email/recovery", context);

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );
            helper.setTo(email);
            helper.setSubject("Восстановление пароля на Teamup");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(
                    "Failed to send email for password recovery",
                    e
            );
        }
    }

}
