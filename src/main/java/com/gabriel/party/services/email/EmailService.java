package com.gabriel.party.services.email;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@festconnect.com}")
    private String from;

    @WithSpan("email.enviar")
    public void enviarEmail(@SpanAttribute("email.destinatario") String destinatario,
                            @SpanAttribute("email.assunto") String assunto,
                            String corpoHtml) {
        if (mailSender == null) {
            logger.warning("JavaMailSender nao configurado. Email ignorado para: " + destinatario);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            mailSender.send(message);
            logger.info("Email enviado para: " + destinatario + " | " + assunto);
        } catch (MessagingException | MailException e) {
            logger.severe("Falha ao enviar email para " + destinatario + ": " + e.getMessage());
        }
    }
}
