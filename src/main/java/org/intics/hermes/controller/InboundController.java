package org.intics.hermes.controller;

import lombok.RequiredArgsConstructor;
import org.intics.hermes.HermesApplication;
import org.intics.hermes.dto.InboundRequest;
import org.intics.hermes.service.InboundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(HermesApplication.API_V_2+"transaction")
@RequiredArgsConstructor
@CrossOrigin
public class InboundController {

    private static final Logger logger = LoggerFactory.getLogger(InboundController.class);
    private final InboundService inboundService;
    @PostMapping(value = "/create/pipeline/fileUrl/{documentType}")
    public ResponseEntity<?> docTypeBasedRedirect(@RequestParam final Long tenantId,
                                                                @RequestBody InboundRequest inboundRequest,
                                                                @RequestParam(required = false) String channel,
                                                                @PathVariable String documentType,
                                                                final Principal principal,
                                                                @RequestHeader(value = "Authorization") String authHeader,
                                                                @RequestHeader(value = "instanceType", required = false) String source) {

        logger.info("Completed processing docTypeBasedRedirect for tenantId={}, documentType={}", tenantId, documentType);
        return inboundService.docTypeRedirect(tenantId, inboundRequest, channel, documentType, principal, authHeader, source);
    }
}
