package com.example.imgr.mail;

import com.example.imgr.dto.AccountRegistrationMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {
    private static final String NOREPLY_ADDRESS = "no-reply@imgr.com";

    private JavaMailSender javaMailSender;

    private SpringTemplateEngine thymeleafTemplateEngine;


    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender, SpringTemplateEngine thymeleafTemplateEngine) {
        this.javaMailSender = javaMailSender;
        this.thymeleafTemplateEngine = thymeleafTemplateEngine;
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(NOREPLY_ADDRESS);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        javaMailSender.send(message);
    }

    @Override
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment) {

    }

    @Override
    public void sendSimpleMessageUsingTemplate(String to, String subject, String text, String... templateModel) {
        sendSimpleMessage(to, subject, String.format(text, templateModel));
    }

    @Override
    public void sendMessageUsingTemplate(String to, String subject, String templatePath, Map<String, Object> templateModel) throws IOException, MessagingException
    {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);

        String htmlBody = thymeleafTemplateEngine.process(templatePath, thymeleafContext);

        sendHtmlMessage(to, subject, htmlBody);
    }

    public void sendAccountConfirmationEmail(String to, @ModelAttribute @Valid AccountRegistrationMail accountRegistrationMail) throws MessagingException, IOException {
        //Simple email
        Map<String, Object> templateModel = new HashMap<>();
        //Email with template
        templateModel.put("link", accountRegistrationMail.getLink());
        templateModel.put("recipientName", accountRegistrationMail.getRecipientName());
        sendMessageUsingTemplate(to, "Account registration", "AccountRegistration.html", templateModel);
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(NOREPLY_ADDRESS);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        javaMailSender.send(message);
    }
}

