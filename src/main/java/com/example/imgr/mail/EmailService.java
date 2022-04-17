package com.example.imgr.mail;

import com.example.imgr.dto.AccountRegistrationMail;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

public interface EmailService {
    void sendSimpleMessage(String to,
                           String subject,
                           String text);

    void sendSimpleMessageUsingTemplate(String to,
                                        String subject,
                                        String text,
                                        String... templateModel);

    void sendMessageWithAttachment(String to,
                                   String subject,
                                   String text,
                                   String pathToAttachment);

    void sendMessageUsingTemplate(String to,
                                  String subject,
                                  String templatePath,
                                  Map<String, Object> templateModel)
            throws IOException, MessagingException;

    void sendAccountConfirmationEmail(String to, @ModelAttribute("mailObject") @Valid AccountRegistrationMail accountRegistrationMail) throws MessagingException, IOException;

}
