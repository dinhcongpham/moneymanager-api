package com.example.moneymanager.service;

import lombok.RequiredArgsConstructor;
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
                throw new RuntimeException("❌ Failed to send email. Status: " + response.getStatusCode() + ", Response: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error sending email via Brevo", e);
        }
    }
}

