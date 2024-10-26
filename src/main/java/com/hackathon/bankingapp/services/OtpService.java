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
        User user = getUserByIdentifier(identifier);
        String otp = generateOtp();
        otpStore.put(identifier, new OtpCode(otp, user.getEmail(), Instant.now().plusSeconds(OTP_EXPIRATION_SECONDS)));

        emailService.sendEmail(user.getEmail(), "OTP Code", "OTP: " + otp);
        logger.debug("OTP sent to {}: {}", identifier, otp);
    }

    public String verifyOtp(String identifier, String otp) {
        OtpCode otpCode = otpStore.get(identifier);
        validateOtp(otpCode, identifier, otp);
        otpStore.remove(identifier);

        String resetToken = generateResetToken(identifier);
        logger.debug("Generated reset token for identifier {}: {}", identifier, resetToken);
        return resetToken;
    }

    public void resetPassword(String identifier, String resetToken, String newPassword) {
        User user = getUserByIdentifier(identifier);
        validateResetToken(identifier, resetToken);

        updatePassword(user, newPassword);
        resetTokenStore.remove(identifier);
        logger.debug("Password reset successfully for identifier {}", identifier);
    }

    private User getUserByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UserNotFoundException("User not found for identifier: " + identifier));
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void validateOtp(OtpCode otpCode, String identifier, String otp) {
        if (otpCode == null || !otpCode.getCode().equals(otp)) {
            logger.warn("Invalid OTP for identifier {}: expected={}, received={}", identifier, otpCode, otp);
            throw new InvalidOtpException("Invalid OTP.");
        }
        if (otpCode.getExpiration().isBefore(Instant.now())) {
            otpStore.remove(identifier);
            logger.warn("Expired OTP for identifier {}: {}", identifier, otp);
            throw new InvalidOtpException("OTP has expired.");
        }
    }

    private String generateResetToken(String identifier) {
        String resetToken = UUID.randomUUID().toString();
        resetTokenStore.put(identifier, new ResetToken(resetToken, Instant.now().plusSeconds(RESET_TOKEN_EXPIRATION_SECONDS)));
        return resetToken;
    }

    private void validateResetToken(String identifier, String resetToken) {
        ResetToken storedToken = resetTokenStore.get(identifier);
        if (storedToken == null || !storedToken.getToken().equals(resetToken) || Instant.now().isAfter(storedToken.getExpiration())) {
            logger.warn("Invalid or expired reset token for identifier {}: expected={}, provided={}", identifier, storedToken, resetToken);
            throw new InvalidResetTokenException("Invalid or expired reset token.");
        }
    }

    private void updatePassword(User user, String newPassword) {
        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.debug("Password updated for user {}", user.getEmail());
    }
}
