package com.example.moneymanager.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void send(String to, String subject, String body) {
        try {
            // Header with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Body of the request
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> sender = new HashMap<>();
            sender.put("email", senderEmail);
            sender.put("name", senderName);
            requestBody.put("sender", sender);

            Map<String, String> toRecipient = new HashMap<>();
            toRecipient.put("email", to);
            requestBody.put("to", List.of(toRecipient));

            requestBody.put("subject", subject);
            requestBody.put("textContent", body);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    brevoApiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to send email. Status: " + response.getStatusCode() + ", Response: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error sending email via Brevo", e);
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String fileName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> requestBody = new HashMap<>();

            // Sender
            Map<String, String> sender = new HashMap<>();
            sender.put("email", senderEmail);
            sender.put("name", senderName);
            requestBody.put("sender", sender);

            // Recipient
            Map<String, String> toRecipient = new HashMap<>();
            toRecipient.put("email", to);
            requestBody.put("to", List.of(toRecipient));

            // Email content
            requestBody.put("subject", subject);
            requestBody.put("textContent", body);

            // Encode the file to Base64
            String encodedFile = Base64.encodeBase64String(attachment);

            // Attachment object
            Map<String, String> attachmentObj = new HashMap<>();
            attachmentObj.put("name", fileName);
            attachmentObj.put("content", encodedFile);

            // Add attachments as a list
            requestBody.put("attachment", List.of(attachmentObj));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    brevoApiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to send email with attachment. Status: " +
                        response.getStatusCode() + ", Response: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error sending email with attachment via Brevo", e);
        }
    }
}

