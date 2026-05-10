package com.gabriel.party.services.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@festconnect.com}")
    private String from;

    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        if (mailSender == null) {
            logger.warning("JavaMailSender nao configurado. Email nao enviado para: " + destinatario);
            return;
        }

        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(from);
            email.setTo(destinatario);
            email.setSubject(assunto);
            email.setText(mensagem);
            mailSender.send(email);
            logger.info("Email enviado para: " + destinatario + " | Assunto: " + assunto);
        } catch (Exception e) {
            logger.severe("Falha ao enviar email para " + destinatario + ": " + e.getMessage());
        }
    }
}
