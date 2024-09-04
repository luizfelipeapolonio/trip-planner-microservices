package com.felipe.trip_planner_mail_service.services;

import com.felipe.trip_planner_mail_service.dtos.CreatedInviteDTO;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Value("${spring.mail.from}")
  private String emailFrom;

  private final JavaMailSender javaMailSender;
  private final Logger logger = LoggerFactory.getLogger(EmailService.class);

  public EmailService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  @KafkaListener(topics = "invite", groupId = "invite-group")
  public void sendEmail(CreatedInviteDTO inviteDTO) {
    try {
      MimeMessage message = this.javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      String text = String.format("""
        Olá, %s!
        
        O usuário '%s', de email '%s', adoraria ter a sua participação na seguinte viagem:
        Destino: %s
        Começa em: %s
        Termina em: %s
        
        Este é seu código de confirmação: %s
        """,
        inviteDTO.participant().name(),
        inviteDTO.trip().ownerName(),
        inviteDTO.trip().ownerEmail(),
        inviteDTO.trip().destination(),
        inviteDTO.trip().startsAt(),
        inviteDTO.trip().endsAt(),
        inviteDTO.inviteCode()
      );

      helper.setFrom(this.emailFrom);
      helper.setSubject("Convite da viagem para: " + inviteDTO.trip().destination());
      helper.setText(text);
      helper.setTo(inviteDTO.participant().email());

      logger.info("---- Enviando e-mail para: {} ----", inviteDTO.participant().email());
      this.javaMailSender.send(message);
    } catch(Exception e) {
      logger.error("Erro ao enviar e-mail. Message: {}", e.getMessage());
    }
  }
}
