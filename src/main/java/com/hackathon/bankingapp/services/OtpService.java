package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.OtpCode;
import com.hackathon.bankingapp.entities.ResetToken;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.InvalidOtpException;
import com.hackathon.bankingapp.exceptions.InvalidResetTokenException;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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

    private static final int OTP_EXPIRATION_SECONDS = 60;
    private static final int RESET_TOKEN_EXPIRATION_SECONDS = 3600;

    private final Random random = new Random();
    private final Map<String, OtpCode> otpStore = new ConcurrentHashMap<>();
    private final Map<String, ResetToken> resetTokenStore = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void sendOtp(String identifier) {
        User user = fetchUserByIdentifier(identifier);
        String otp = generateOtp();
        storeOtp(identifier, otp, user.getEmail());
        emailService.sendEmail(user.getEmail(), "OTP Code", "OTP:" + otp);
    }

    public String verifyOtp(String identifier, String otp) {
        OtpCode otpCode = otpStore.get(identifier);
        if (isInvalidOrExpiredOtp(otpCode, otp)) {
            otpStore.remove(identifier);
            throw new InvalidOtpException("Invalid or expired OTP.");
        }

        otpStore.remove(identifier);
        return generateAndStoreResetToken(identifier);
    }

    public void resetPassword(String identifier, String resetToken, String newPassword) {
        User user = fetchUserByIdentifier(identifier);
        validateResetToken(identifier, resetToken);
        updatePassword(user, newPassword);
    }

    private User fetchUserByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UserNotFoundException("User not found for identifier: " + identifier));
    }

    private void storeOtp(String identifier, String otp, String email) {
        otpStore.put(identifier, new OtpCode(otp, email, Instant.now().plusSeconds(OTP_EXPIRATION_SECONDS)));
    }

    private boolean isInvalidOrExpiredOtp(OtpCode otpCode, String otp) {
        return otpCode == null || !otpCode.getCode().equals(otp) || otpExpired(otpCode);
    }

    private boolean otpExpired(OtpCode otpCode) {
        return otpCode.getExpiration().isBefore(Instant.now());
    }

    private String generateAndStoreResetToken(String identifier) {
        String resetToken = UUID.randomUUID().toString();
        resetTokenStore.put(identifier, new ResetToken(resetToken, Instant.now().plusSeconds(RESET_TOKEN_EXPIRATION_SECONDS)));
        return resetToken;
    }

    private void validateResetToken(String identifier, String resetToken) {
        ResetToken storedToken = resetTokenStore.get(identifier);
        if (isInvalidOrExpiredResetToken(storedToken, resetToken)) {
            throw new InvalidResetTokenException("Invalid or expired reset token.");
        }
    }

    private boolean isInvalidOrExpiredResetToken(ResetToken token, String providedToken) {
        return token == null || !token.getToken().equals(providedToken) || tokenExpired(token);
    }

    private boolean tokenExpired(ResetToken token) {
        return Instant.now().isAfter(token.getExpiration());
    }

    private void updatePassword(User user, String newPassword) {
        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenStore.remove(user.getEmail());
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(999999));
    }
}
