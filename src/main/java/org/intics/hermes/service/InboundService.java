package org.intics.hermes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.intics.hermes.dto.InboundRequest;
import org.intics.hermes.dto.InboundResponse;
import org.intics.hermes.exception.HermesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InboundService {

    private static final Logger logger = LoggerFactory.getLogger(InboundService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ph.inbound.url}")
    private String phInboundUrl;

    @Value("${bh.inbound.url}")
    private String bhInboundUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public ResponseEntity<InboundResponse> docTypeRedirect(Long tenantId,
                                                           InboundRequest inboundRequest,
                                                           String channel,
                                                           String documentType,
                                                           Principal principal,
                                                           String authHeader) {

        String healthPlan = inboundRequest.getHealthPlan();
        logger.info("docTypeRedirect called with tenantId={}, documentType={}, healthPlan={}, channel={}",
                tenantId, documentType, healthPlan, channel);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new HermesException("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED.value());
        }

        String targetUrl = switch (healthPlan) {
            case "PH" -> String.format("%s/%s", phInboundUrl, documentType);
            case "BH" -> String.format("%s/%s", bhInboundUrl, documentType);
            default ->
                    throw new HermesException("Unsupported health plan: " + healthPlan, HttpStatus.BAD_REQUEST.value());
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        if (principal != null) {
            headers.add("X-User", principal.getName());
        }

        String finalUrl = targetUrl +
                "?tenantId=" + tenantId +
                (channel != null ? "&channel=" + channel : "") +
                "&documentType=" + documentType;

        logger.info("Forwarding request to URL: {}", finalUrl);

        HttpEntity<InboundRequest> requestEntity = new HttpEntity<>(inboundRequest, headers);

        try {
            ResponseEntity<InboundResponse> response = restTemplate.exchange(finalUrl, HttpMethod.POST, requestEntity, InboundResponse.class);

            if (response.getBody() == null) {
                throw new HermesException("Empty response body from external service", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            if (logger.isInfoEnabled()) {
                logger.info("Response body received {}", objectMapper.writeValueAsString(response.getBody()));
            }

            logger.info("Response received with status code: {}", response.getStatusCode());
            HttpHeaders incomingHeaders = response.getHeaders();
            HttpHeaders outgoingHeaders = new HttpHeaders();
            incomingHeaders.forEach((key, values) -> {
                if (!HOP_BY_HOP_HEADERS.contains(key)) {
                    outgoingHeaders.put(key, values);
                }
            });

            return new ResponseEntity<>(
                    response.getBody(),
                    outgoingHeaders,
                    response.getStatusCode()
            );

        } catch (HttpStatusCodeException ex) {
            logger.error("Error from external service: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new HermesException( ex.getResponseBodyAsString(), ex.getStatusCode().value());
        } catch (Exception ex) {
            logger.error("Unexpected error forwarding request", ex);
            throw new HermesException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}

