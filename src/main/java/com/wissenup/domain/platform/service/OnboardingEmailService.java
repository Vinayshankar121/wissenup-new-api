package com.wissenup.domain.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public void sendCredentials(String recipient, String schoolName, String temporaryPassword) {
        if (mailFrom == null || mailFrom.isBlank()) {
            log.warn("Onboarding email not sent to {} because MAIL_USERNAME is not configured", recipient);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipient);
            message.setSubject("Welcome to WissenUp - Your administrator account");
            message.setText("Hello,\n\nYour administrator account for " + schoolName
                + " has been created.\n\nLogin email: " + recipient
                + "\nTemporary password: " + temporaryPassword
                + "\n\nPlease sign in and change your password immediately.\n\nWissenUp Team");
            mailSender.send(message);
            log.info("Onboarding credentials email sent to {}", recipient);
        } catch (Exception exception) {
            // Account creation must remain successful even if the mail provider is temporarily unavailable.
            log.error("Organization was created, but its onboarding email could not be sent to {}", recipient,
                exception);
        }
    }

    public void sendStaffCredentials(String recipient, String staffName, String role, String temporaryPassword) {
        if (mailFrom == null || mailFrom.isBlank()) {
            log.warn("Staff credentials email not sent to {} because MAIL_USERNAME is not configured", recipient);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipient);
            message.setSubject("Welcome to WissenUp - Your staff account");
            message.setText("Hello " + staffName + ",\n\nYour " + role
                + " account has been created.\n\nLogin email: " + recipient
                + "\nTemporary password: " + temporaryPassword
                + "\n\nPlease sign in and change your password immediately.\n\nWissenUp Team");
            mailSender.send(message);
            log.info("Staff credentials email sent to {}", recipient);
        } catch (Exception exception) {
            log.error("Staff was created, but the credentials email could not be sent to {}", recipient, exception);
        }
    }
}
