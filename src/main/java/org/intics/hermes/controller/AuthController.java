package org.intics.hermes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.intics.hermes.HermesApplication;
import org.intics.hermes.dto.CurrentUser;
import org.intics.hermes.dto.LoginRequest;
import org.intics.hermes.dto.ResponseStatusPayload;
import org.intics.hermes.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(HermesApplication.API_V_2 + "auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseStatusPayload<CurrentUser>> loginRedirect(@RequestBody @Valid final LoginRequest loginRequest, @RequestHeader(value = "instanceType") String source) {

        logger.info("Login response success for username");
        return authService.loginRedirect(loginRequest,source);
    }
}
