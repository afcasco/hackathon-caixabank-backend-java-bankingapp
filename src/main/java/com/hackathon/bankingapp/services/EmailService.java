package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.entities.UserAsset;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvestmentConfirmation(User user, String assetSymbol, double quantity, double amount, String subject) {
        UserAsset userAsset = user.getAssets().stream()
                .filter(asset -> asset.getSymbol().equals(assetSymbol))
                .findFirst()
                .orElse(null);

        double currentQuantity = userAsset != null ? userAsset.getQuantity() : 0.0;

        String message = String.format(
                """
                        Dear %s,
                        
                        You have successfully %s %.2f units of %s for a total amount of $%.2f.
                        
                        Current holdings of %s: %.2f units
                        
                        Thank you for using our investment services.
                        
                        Best Regards,
                        Investment Management Team""",
                user.getName(),
                subject.contains("Purchase") ? "purchased" : "sold",
                quantity,
                assetSymbol,
                amount,
                assetSymbol,
                currentQuantity
        );

        sendEmail(user.getEmail(), subject, message);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
