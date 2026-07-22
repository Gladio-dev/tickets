package com.group.artifName.services;


//import com.microsoft.graph.models.*;
import com.group.artifName.entities.Ticket;
import com.group.artifName.entities.User;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.azure.identity.ClientSecretCredential;
import org.springframework.beans.factory.annotation.Value;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final GraphServiceClient graphServiceClient;
    private final ClientSecretCredential credential;
    private final SpringTemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private String sender;
    @Value("${mail.ticket-recipient}")
    private String ticketRecipient;

    @Value("${mail.portal-url}")
    private String portalUrl;


    public void sendActivationEmail(User user,String token) {
        // Construir el HTML

        user = new User();
        user.setEmail("alexis.castillo@rseguridad.com");
        Context context = new Context();
        context.setVariable("activationLink", portalUrl+"/activate/"+token);

        String html = templateEngine.process(
                "activation-email",
                context
        );

        // Crear el cuerpo del correo
        ItemBody body = new ItemBody();
        body.setContentType(BodyType.Html);
        body.setContent(html);

        // Destinatario
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setAddress(user.getEmail());

        Recipient recipient = new Recipient();
        recipient.setEmailAddress(emailAddress);

        // Mensaje
        Message message = new Message();
        message.setSubject("Activa tu cuenta");
        message.setBody(body);
        message.setToRecipients(List.of(recipient));

        // Petición
        SendMailPostRequestBody request = new SendMailPostRequestBody();
        request.setMessage(message);
        request.setSaveToSentItems(true);

        // Envío
        graphServiceClient
                .users()
                .byUserId(sender) // <-- correo remitente
                .sendMail()
                .post(request);
    }

    public void sendNewTicketNotification(Ticket ticket) {

        Context context = new Context();
        context.setVariable("date",
                ticket.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        context.setVariable("userName", ticket.getUser().getName());
        context.setVariable("company", ticket.getUser().getCompany());
        context.setVariable("portalUrl", portalUrl);

        String html = templateEngine.process(
                "new-ticket-notification",
                context
        );

        ItemBody body = new ItemBody();
        body.setContentType(BodyType.Html);
        body.setContent(html);

        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setAddress(ticketRecipient);

        Recipient recipient = new Recipient();
        recipient.setEmailAddress(emailAddress);

        Message message = new Message();
        message.setSubject("Nuevo ticket registrado");
        message.setBody(body);
        message.setToRecipients(List.of(recipient));

        SendMailPostRequestBody request = new SendMailPostRequestBody();
        request.setMessage(message);
        request.setSaveToSentItems(true);

        graphServiceClient
                .users()
                .byUserId(sender)
                .sendMail()
                .post(request);
    }


    public void testAuthentication() {
        System.out.println("GraphServiceClient creado correctamente: " + graphServiceClient);
    }

    public void testToken() {
        AccessToken token = credential.getToken(
                new TokenRequestContext()
                        .addScopes("https://graph.microsoft.com/.default")
        ).block();
        System.out.println(token.getToken());
    }

}
