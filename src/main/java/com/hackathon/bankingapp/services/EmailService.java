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
    private final DecimalFormat df = new DecimalFormat("0.00");

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvestmentConfirmation(User user, String assetSymbol, double quantity, double amount, String subject) {
        double currentHoldings = user.getAssets().stream()
                .filter(asset -> asset.getSymbol().equals(assetSymbol))
                .mapToDouble(UserAsset::getQuantity)
                .sum();

        String assetSummary = user.getAssets().stream()
                .collect(Collectors.groupingBy(
                        UserAsset::getSymbol,
                        Collectors.teeing(
                                Collectors.summingDouble(UserAsset::getQuantity),
                                Collectors.averagingDouble(UserAsset::getPurchasePrice),
                                (totalQuantity, avgPrice) -> String.format("%.2f units purchased at $%s", totalQuantity, df.format(avgPrice))
                        )
                ))
                .entrySet().stream()
                .map(entry -> String.format("- %s: %s", entry.getKey(), entry.getValue()))
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

    public void sendSaleConfirmation(User user, String assetSymbol, double quantitySold, double profitOrLoss, String subject) {
        double currentHoldings = user.getAssets().stream()
                .filter(asset -> asset.getSymbol().equals(assetSymbol))
                .mapToDouble(UserAsset::getQuantity)
                .sum();

        String assetSummary = user.getAssets().stream()
                .collect(Collectors.groupingBy(UserAsset::getSymbol, Collectors.summingDouble(UserAsset::getQuantity)))
                .entrySet().stream()
                .map(entry -> String.format("- %s: %.2f units purchased at $%.2f", entry.getKey(), entry.getValue(), user.getAssets().getFirst().getPurchasePrice()))
                .collect(Collectors.joining("\n"));

        String message = String.format(
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
