package com.wissenup.domain.identity.service.impl;

import com.wissenup.domain.identity.exception.AuthenticationException;
import com.wissenup.domain.identity.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private static final int OTP_LENGTH = 6;
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private final JavaMailSender mailSender;

    public OtpServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void generateAndSendOtp(String email) {
        String otp = generateOtp();
        long expiryTime = Instant.now().toEpochMilli() + (otpExpiryMinutes * 60 * 1000L);
        otpStore.put(email, new OtpData(otp, expiryTime, 0));
        sendOtpEmail(email, otp);
        log.debug("OTP generated and sent for email: {}", email);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStore.get(email);

        if (otpData == null) {
            throw AuthenticationException.otpInvalid();
        }

        long now = Instant.now().toEpochMilli();
        if (now > otpData.expiryTime) {
            otpStore.remove(email);
            throw AuthenticationException.otpExpired();
        }

        if (otpData.attempts >= maxAttempts) {
            otpStore.remove(email);
            throw AuthenticationException.tooManyAttempts();
        }

        otpData.attempts++;

        if (!otpData.code.equals(otp)) {
            throw AuthenticationException.otpInvalid();
        }

        return true;
    }

    @Override
    public void clearOtp(String email) {
        otpStore.remove(email);
        log.debug("OTP cleared for email: {}", email);
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private void sendOtpEmail(String email, String otp) {
        if (mailFrom == null || mailFrom.isBlank()) {
            log.warn("SMTP is not configured. Development OTP for {}: {}", email, otp);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("WissenUp - Your OTP Code");
            message.setText("Your OTP code is: " + otp + "\n\nThis code will expire in " + otpExpiryMinutes + " minutes.");
            mailSender.send(message);
            log.debug("OTP email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", email, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private static class OtpData {
        String code;
        long expiryTime;
        int attempts;

        OtpData(String code, long expiryTime, int attempts) {
            this.code = code;
            this.expiryTime = expiryTime;
            this.attempts = attempts;
        }
    }
}
