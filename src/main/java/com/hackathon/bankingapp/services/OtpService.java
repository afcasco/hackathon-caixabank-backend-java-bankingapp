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

    private final Map<String, OtpCode> otpStore = new ConcurrentHashMap<>();
    private final Map<String, ResetToken> resetTokenStore = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void sendOtp(String identifier) {
        User user = userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UserNotFoundException("User not found for identifier: " + identifier));

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(identifier, new OtpCode(otp, user.getEmail(), Instant.now().plusSeconds(60)));

        emailService.sendEmail(user.getEmail(), "OTP Code", "OTP: " + otp);
        logger.debug("OTP sent to {}: {}", identifier, otp);
    }

    public String verifyOtp(String identifier, String otp) {
        OtpCode otpCode = otpStore.get(identifier);
        logger.debug("Verifying OTP for identifier {}: expected={}, received={}", identifier, otpCode, otp);

        if (otpCode == null || !otpCode.getCode().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP.");
        }

        if (otpCode.getExpiration().isBefore(Instant.now())) {
            otpStore.remove(identifier);
            throw new InvalidOtpException("OTP has expired.");
        }

        otpStore.remove(identifier);

        String resetToken = UUID.randomUUID().toString();
        resetTokenStore.put(identifier, new ResetToken(resetToken, Instant.now().plusSeconds(3600)));
        logger.debug("Generated reset token for identifier {}: {}", identifier, resetToken);

        return resetToken;
    }

    public void resetPassword(String identifier, String resetToken, String newPassword) {
        User user = userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UserNotFoundException("User not found for identifier: " + identifier));

        ResetToken storedToken = resetTokenStore.get(identifier);
        logger.debug("Resetting password for identifier {}: expected={}, provided={}", identifier, storedToken, resetToken);

        if (storedToken == null || !storedToken.getToken().equals(resetToken) || Instant.now().isAfter(storedToken.getExpiration())) {
            throw new InvalidResetTokenException("Invalid or expired reset token.");
        }

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenStore.remove(identifier);
        logger.debug("Password reset successfully for identifier {}", identifier);

    }
}
