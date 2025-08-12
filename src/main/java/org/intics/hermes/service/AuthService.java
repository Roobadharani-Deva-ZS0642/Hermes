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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

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

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            HttpHeaders.CONNECTION,
            HttpHeaders.PROXY_AUTHENTICATE,
            HttpHeaders.PROXY_AUTHORIZATION,
            HttpHeaders.TE,
            HttpHeaders.TRAILER,
            HttpHeaders.TRANSFER_ENCODING,
            HttpHeaders.UPGRADE,
            HttpHeaders.CONTENT_LENGTH
    );

    public ResponseEntity<ResponseStatusPayload<CurrentUser>> loginRedirect(LoginRequest loginRequest, String loginSource) {
        logger.info("loginRedirect called with loginSource={}, username={}", loginSource, loginRequest.getUsername());

        String targetUrl = switch (loginSource) {
            case "BH" -> bhLoginUrl;
            case "PH" -> phLoginUrl;
            default -> throw new HermesException("Invalid login source: " + loginSource, HttpStatus.BAD_REQUEST.value());
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, headers);

        logger.info("Forwarding login request to: {}", targetUrl);

        try {
            ResponseEntity<ResponseStatusPayload> response =
                    restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity, ResponseStatusPayload.class);

            CurrentUser currentUser = objectMapper.convertValue(response.getBody().getPayload(), CurrentUser.class);

            HttpHeaders outgoingHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, values) -> {
                if (!HOP_BY_HOP_HEADERS.contains(key)) {
                    outgoingHeaders.put(key, values);
                }
            });

            return new ResponseEntity<>(
                    new ResponseStatusPayload<>(currentUser),
                    outgoingHeaders,
                    response.getStatusCode()
            );

        }  catch (HttpStatusCodeException ex) {
            logger.error("error from alchemy "+ex.getMessage(), ex.getStatusCode().value());
            throw new HermesException(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
        } catch (Exception ex) {
            logger.error("Unexpected error forwarding request", ex);
            throw new HermesException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

}
