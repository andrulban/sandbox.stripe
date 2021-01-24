package com.andrulban.sandbox.stripe.service.impl;

import com.andrulban.sandbox.stripe.exception.ApiException;
import com.andrulban.sandbox.stripe.exception.ExceptionType;
import com.andrulban.sandbox.stripe.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.logging.Logger;

@Service
public class EmailServiceImpl implements EmailService {
  private final JavaMailSender javaMailSender;
  private final TemplateEngine templateEngine;
  private final String webAppDomainUrl;

  public EmailServiceImpl(
      JavaMailSender javaMailSender,
      TemplateEngine templateEngine,
      @Value("${web.app.domain.url}") String webAppDomainUrl) {
    this.javaMailSender = javaMailSender;
    this.templateEngine = templateEngine;
    this.webAppDomainUrl = webAppDomainUrl;
  }

  public void sendPasswordRecoveryMail(String recipientEmail, String title, String resetToken) {
    try {
      MimeMessage mail = javaMailSender.createMimeMessage();

      Context context = new Context();
      context.setVariable("resetLink", webAppDomainUrl + "/password-reset/" + resetToken);
      String content = templateEngine.process("emailTemplate", context);

      MimeMessageHelper helper = new MimeMessageHelper(mail, true);
      helper.setTo(recipientEmail);
      helper.setSubject(title);
      helper.setText(content, true);

      javaMailSender.send(mail);
    } catch (MessagingException e) {
      throw new ApiException("Error during email sending", ExceptionType.ERROR);
    }
  }
}
