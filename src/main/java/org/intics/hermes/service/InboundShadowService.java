package org.intics.hermes.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
public class InboundShadowService {

    private static final Logger logger = LoggerFactory.getLogger(InboundShadowService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ph.shadow.inbound.url}")
    private String phShadowInboundUrl;

    @Value("${bh.shadow.inbound.url}")
    private String bhShadowInboundUrl;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
                                                           String authHeader,
                                                           String source) {

        String healthPlan = inboundRequest.getHealthPlan();
        logger.info("docTypeRedirect called with tenantId={}, documentType={}, healthPlan={}, channel={}",
                tenantId, documentType, healthPlan, channel);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new HermesException("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED.value());
        }

        String targetUrl = switch (healthPlan) {
            case "PH" -> String.format("%s/%s", phShadowInboundUrl, documentType);
            case "BH" -> String.format("%s/%s", bhShadowInboundUrl, documentType);
            default ->
                    throw new HermesException("Unsupported health plan: " + healthPlan, HttpStatus.BAD_REQUEST.value());
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        headers.set("instanceType", source != null ? source : "");

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

            if (logger.isInfoEnabled()) {
                logger.info("Response body received {}", objectMapper.writeValueAsString(response.getBody()));
            }
            logger.info("Response received with status code: {}", response.getStatusCode());
            HttpHeaders outgoingHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, values) -> {
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
            throw new HermesException(ex.getMessage(), ex.getStatusCode().value());
        } catch (Exception ex) {
            logger.error("Unexpected error forwarding request", ex);
            throw new HermesException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
