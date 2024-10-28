package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.entities.UserAsset;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final DecimalFormat df = new DecimalFormat("0.00");

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvestmentConfirmation(User user, String assetSymbol, double quantity, double amount, String subject) {
        String message = generateMessage(user, assetSymbol, quantity, amount, subject, calculateNetWorth(user));
        sendEmail(user.getEmail(), subject, message);
    }

    public void sendSaleConfirmation(User user, String assetSymbol, double quantitySold, double profitOrLoss, String subject) {
        String message = generateSaleMessage(user, assetSymbol, quantitySold, profitOrLoss, calculateNetWorth(user));
        sendEmail(user.getEmail(), subject, message);
    }

    private String generateMessage(User user, String assetSymbol, double quantity, double amount, String subject, double netWorth) {
        double currentHoldings = getCurrentHoldings(user, assetSymbol);
        String assetSummary = generateAssetSummary(user);

        return String.format(
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
                df.format(netWorth)
        );
    }

    private String generateSaleMessage(User user, String assetSymbol, double quantitySold, double profitOrLoss, double netWorth) {
        double currentHoldings = getCurrentHoldings(user, assetSymbol);
        String assetSummary = generateAssetSummary(user);

        return String.format(
                """
                        Dear %s,
    
                        You have successfully sold %.2f units of %s.
    
                        Total Gain/Loss: $%.2f
    
                        Remaining holdings of %s: %.2f units
    
                        Summary of current assets:
                        %s
    
                        Account Balance: $%s
                        Net Worth: $%s
    
                        Thank you for using our investment services.
    
                        Best Regards,
                        Investment Management Team
                        """,
                user.getName(),
                quantitySold,
                assetSymbol,
                profitOrLoss,
                assetSymbol,
                currentHoldings,
                assetSummary,
                df.format(user.getBalance()),
                df.format(netWorth)
        );
    }

    private double getCurrentHoldings(User user, String assetSymbol) {
        return user.getAssets().stream()
                .filter(asset -> asset.getSymbol().equals(assetSymbol))
                .mapToDouble(UserAsset::getQuantity)
                .sum();
    }

    private String generateAssetSummary(User user) {
        Map<String, String> summary = user.getAssets().stream()
                .collect(Collectors.groupingBy(
                        UserAsset::getSymbol,
                        Collectors.collectingAndThen(
                                Collectors.summarizingDouble(UserAsset::getQuantity),
                                stats -> String.format("%.2f units purchased at $%s", stats.getSum(), df.format(stats.getAverage()))
                        )
                ));

        return summary.entrySet().stream()
                .map(entry -> String.format("- %s: %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
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
