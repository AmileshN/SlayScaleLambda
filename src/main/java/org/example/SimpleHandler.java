package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.sesv2.*;
import software.amazon.awssdk.services.sesv2.model.*;


import java.util.Map;

public class SimpleHandler implements RequestHandler<Map<String, Object>, LambdaResponse> {

    private static final String FROM_EMAIL = "slayscale@gmail.com";

    @Override
    public LambdaResponse handleRequest(Map<String, Object> event , Context context) {

        String email = (String) event.get("email");

        if (email == null || email.isBlank()) {
            return new LambdaResponse("No email provided.");
        }

        sendEmail(email, context);

        return new LambdaResponse("Email sent to " + email);
    }

    private void sendEmail(String toEmail, Context ctx) {
        ctx.getLogger().log("Sending email to " + toEmail);

        SesV2Client ses = SesV2Client.builder().build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .fromEmailAddress(FROM_EMAIL)
                .destination(Destination.builder()
                        .toAddresses(toEmail)
                        .build())
                .content(EmailContent.builder()
                        .simple(Message.builder()
                                .subject(Content.builder()
                                        .data("Your Lambda Email")
                                        .build())
                                .body(Body.builder()
                                        .text(Content.builder()
                                                .data("Hello! This email was sent from your AWS Lambda function.")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        ses.sendEmail(emailRequest);

        ctx.getLogger().log("Email successfully sent to: " + toEmail);
    }

}
