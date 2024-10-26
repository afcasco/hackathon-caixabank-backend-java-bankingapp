package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.entities.UserAsset;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private final DecimalFormat df = new DecimalFormat("0.00");

    public void sendInvestmentConfirmation(User user, String assetSymbol, double quantity, double amount, String subject) {
        double currentHoldings = user.getAssets().stream()
                .filter(asset -> asset.getSymbol().equals(assetSymbol))
                .mapToDouble(UserAsset::getQuantity)
                .sum();

        String assetSummary = user.getAssets().stream()
                .map(asset -> String.format("- %s: %.2f units purchased at $%s", asset.getSymbol(), asset.getQuantity(), df.format(asset.getPurchasePrice())))
                .collect(Collectors.joining("\n"));

        String message = String.format(
                """
                        Dear %s,
                        
                        You have successfully %s %.2f units of %s for a total amount of $%s.
                        
                        Current holdings of %s: %.2f units
                        
                        Summary of current assets:
                        %s
                        
                        Account Balance: $%s
                        Net Worth: $%s
                        
                        Thank you for using our investment services.
                        
                        Best Regards,
                        Investment Management Team""",
                user.getName(),
                subject.contains("Purchase") ? "purchased" : "sold",
                quantity,
                assetSymbol,
                df.format(amount),
                assetSymbol,
                currentHoldings,
                assetSummary,
                df.format(user.getBalance()),
                df.format(calculateNetWorth(user))
        );

        sendEmail(user.getEmail(), subject, message);
    }

    private double calculateNetWorth(User user) {
        return user.getBalance() + user.getAssets().stream()
                .mapToDouble(asset -> asset.getQuantity() * asset.getPurchasePrice())
                .sum();
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
