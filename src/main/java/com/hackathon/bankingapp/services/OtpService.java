package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.OtpCode;
import com.hackathon.bankingapp.entities.ResetToken;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.InvalidOtpException;
import com.hackathon.bankingapp.exceptions.InvalidResetTokenException;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_EXPIRATION_SECONDS = 60;
    private static final int RESET_TOKEN_EXPIRATION_SECONDS = 3600;

    private final Map<String, OtpCode> otpStore = new ConcurrentHashMap<>();
    private final Map<String, ResetToken> resetTokenStore = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void sendOtp(String identifier) {
        User user = getUserByEmail(identifier);
        String otp = generateOtp();
        otpStore.put(identifier, new OtpCode(otp, user.getEmail(), Instant.now().plusSeconds(OTP_EXPIRATION_SECONDS)));

        emailService.sendEmail(user.getEmail(), "OTP Code", "OTP: " + otp);
        logger.info("OTP sent to user with identifier: {}", identifier);
    }

    public String verifyOtp(String identifier, String otp) {
        OtpCode otpCode = otpStore.get(identifier);
        if (otpCode == null || !otpCode.getCode().equals(otp) || otpExpired(otpCode)) {
            otpStore.remove(identifier);
            throw new InvalidOtpException("Invalid or expired OTP.");
        }

        otpStore.remove(identifier);
        String resetToken = generateResetToken(identifier);
        logger.info("Generated reset token for identifier: {}", identifier);
        return resetToken;
    }

    public void resetPassword(String identifier, String resetToken, String newPassword) {
        User user = getUserByEmail(identifier);
        validateResetToken(identifier, resetToken);

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenStore.remove(identifier);
        logger.info("Password reset successfully for identifier: {}", identifier);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for identifier: " + email));
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private boolean otpExpired(OtpCode otpCode) {
        return otpCode.getExpiration().isBefore(Instant.now());
    }

    private String generateResetToken(String identifier) {
        String resetToken = UUID.randomUUID().toString();
        resetTokenStore.put(identifier, new ResetToken(resetToken, Instant.now().plusSeconds(RESET_TOKEN_EXPIRATION_SECONDS)));
        return resetToken;
    }

    private void validateResetToken(String identifier, String resetToken) {
        ResetToken storedToken = resetTokenStore.get(identifier);
        if (storedToken == null || !storedToken.getToken().equals(resetToken) || Instant.now().isAfter(storedToken.getExpiration())) {
            throw new InvalidResetTokenException("Invalid or expired reset token.");
        }
    }
}
