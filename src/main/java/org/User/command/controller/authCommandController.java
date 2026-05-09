package org.User.command.controller;
import jakarta.validation.Valid;
import org.User.command.command.ForgotPasswordCommand;
import org.User.command.command.ResetPasswordCommand;
import org.User.command.command.VerifyEmailCommand;
import org.User.command.model.request.*;
import org.User.command.model.response.LoginResponseDTO;
import org.User.command.service.authService;
import org.axonframework.commandhandling.gateway.CommandGateway;
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
    @Autowired
    private  CommandGateway commandGateway;

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
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token là bắt buộc để đăng xuất");
        }

        try {
            authService.logout(refreshToken);
            return ResponseEntity.ok("Đăng xuất thành công và đã hủy phiên làm việc trên Keycloak");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/verify-email")
    public CompletableFuture<String> verifyEmail(@RequestBody VerifyEmailRequest request) {
        return commandGateway.send(new VerifyEmailCommand(request.getUserId()))
                .thenApply(it -> "Xác thực email thành công!")
                .exceptionally(e -> "Lỗi: " + e.getMessage());
    }
    @PostMapping("/forgot-password")
    public CompletableFuture<String> forgotPassword(@RequestParam String email) {
        // 1. Tìm userId từ email thông qua AuthService (gọi Keycloak/DB)
        String userId = authService.findUserIdByEmail(email);

        if (userId == null) {
            return CompletableFuture.completedFuture("Email không tồn tại!");
        }
        // 2. Gửi Command bằng userId thay vì email
        return commandGateway.send(new ForgotPasswordCommand(userId, email))
                .thenApply(it -> "Yêu cầu đã được tiếp nhận!")
                .exceptionally(e -> "Lỗi: " + e.getMessage());
    }
    @PostMapping("/reset-password")
    public CompletableFuture<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        return commandGateway.send(new ResetPasswordCommand(request.getUserId(), request.getNewPassword()))
                .thenApply(it -> "Đặt lại mật khẩu thành công!")
                .exceptionally(e -> "Lỗi: " + e.getMessage());
    }
}
