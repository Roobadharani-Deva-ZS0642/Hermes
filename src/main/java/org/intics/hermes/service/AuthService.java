package org.intics.hermes.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.intics.hermes.dto.CurrentUser;
import org.intics.hermes.dto.LoginRequest;
import org.intics.hermes.dto.ResponseStatusPayload;
import org.intics.hermes.exception.HermesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Value("${bh.login.url}")
    private String bhLoginUrl;

    @Value("${ph.login.url}")
    private String phLoginUrl;

    public ResponseStatusPayload<CurrentUser> loginRedirect(LoginRequest loginRequest, String loginSource) {
        logger.info("loginRedirect called with loginSource={}, username={}", loginSource, loginRequest.getUsername());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequest> entity = new HttpEntity<>(loginRequest, headers);
        ResponseEntity<ResponseStatusPayload> response;

        try {
            response = switch (loginSource) {
                case "BH" -> {
                    logger.info("Calling BH login URL: {}", bhLoginUrl);
                    yield restTemplate.postForEntity(bhLoginUrl, entity, ResponseStatusPayload.class);
                }
                case "PH" -> {
                    logger.info("Calling PH login URL: {}", phLoginUrl);
                    yield restTemplate.postForEntity(phLoginUrl, entity, ResponseStatusPayload.class);
                }
                default -> {
                    logger.warn("No valid login source found for loginSource={}", loginSource);
                    throw new HermesException("Invalid login source: " + loginSource);
                }
            };

            ResponseStatusPayload responseBody = response.getBody();
            if (responseBody == null) {
                logger.error("Response body is empty for loginSource={}", loginSource);
                throw new HermesException("Response body is empty");
            }
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Received successful response with status code: {}", response.getStatusCode());

                CurrentUser currentUser = objectMapper.convertValue(
                        responseBody.getPayload(),
                        CurrentUser.class
                );

                return new ResponseStatusPayload<>(currentUser);
            } else {
                HttpStatusCode statusCode = response.getStatusCode();
                logger.error("Unexpected response status: {}", statusCode);
                throw new HermesException("Unexpected response status");
            }
        } catch (Exception e) {
            logger.error("Error calling external API: {}", e.getMessage(), e);
            throw new HermesException(e.getMessage(), e);
        }
    }
}
