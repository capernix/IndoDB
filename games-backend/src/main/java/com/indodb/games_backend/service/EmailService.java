package com.indodb.games_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending notifications
 * Uses Spring Mail with SMTP configuration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@indiadb.games}")
    private String fromEmail;
    
    /**
     * Send price drop alert email
     */
    public void sendPriceDropAlert(String toEmail, String gameTitle, String oldPrice, String newPrice, String discountPercentage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🔻 Price Drop Alert: " + gameTitle);
            message.setText(String.format("""
                Great news! A game on your wishlist has dropped in price!
                
                Game: %s
                Old Price: ₹%s
                New Price: ₹%s
                Discount: %s%%
                
                Don't miss this deal!
                
                - IndiaDB Games
                """, gameTitle, oldPrice, newPrice, discountPercentage));
            
            mailSender.send(message);
            
            log.info("📧 Price drop email sent to {} for game: {}", toEmail, gameTitle);
            
        } catch (Exception e) {
            log.error("❌ Failed to send price drop email to {}: {}", toEmail, e.getMessage());
        }
    }
    
    /**
     * Send target price reached alert
     */
    public void sendTargetPriceAlert(String toEmail, String gameTitle, String currentPrice, String targetPrice) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🎯 Target Price Reached: " + gameTitle);
            message.setText(String.format("""
                Your target price has been reached!
                
                Game: %s
                Current Price: ₹%s
                Your Target: ₹%s
                
                Time to grab this deal!
                
                - IndiaDB Games
                """, gameTitle, currentPrice, targetPrice));
            
            mailSender.send(message);
            
            log.info("📧 Target price email sent to {} for game: {}", toEmail, gameTitle);
            
        } catch (Exception e) {
            log.error("❌ Failed to send target price email to {}: {}", toEmail, e.getMessage());
        }
    }
    
    /**
     * Send free game alert
     */
    public void sendFreeGameAlert(String toEmail, String gameTitle) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("🎁 Free Game Alert: " + gameTitle);
            message.setText(String.format("""
                A game on your wishlist is now FREE!
                
                Game: %s
                Price: FREE (₹0)
                
                Claim it now before the offer ends!
                
                - IndiaDB Games
                """, gameTitle));
            
            mailSender.send(message);
            
            log.info("📧 Free game email sent to {} for game: {}", toEmail, gameTitle);
            
        } catch (Exception e) {
            log.error("❌ Failed to send free game email to {}: {}", toEmail, e.getMessage());
        }
    }
}
