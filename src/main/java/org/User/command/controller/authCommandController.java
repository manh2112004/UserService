package org.User.command.controller;
import jakarta.validation.Valid;
import org.User.command.model.request.LoginRequestModel;
import org.User.command.model.request.RefreshTokenRequest;
import org.User.command.model.request.RegisterRequestModel;
import org.User.command.model.response.LoginResponseDTO;
import org.User.command.service.authService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
    @RequestMapping("/api/v1/auth")
public class authCommandController {
    @Autowired
    private authService authService;

    @PostMapping("/register")
    public CompletableFuture<String> register(@Valid @RequestBody RegisterRequestModel model) {
        return authService.registerUser(model);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestModel loginRequest) {
        try {
            LoginResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        Map<String, Object> tokens = authService.refreshToken(request.getRefreshToken());
        // Trả về trực tiếp object chứa access_token, refresh_token, expires_in...
        return ResponseEntity.ok(tokens);
    }
}
