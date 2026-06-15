package org.User.command.controller;

import org.User.command.command.UpdateUserStatusCommand;
import org.User.command.model.request.AssignRoleRequest;
import org.User.command.model.request.ChangePasswordRequest;
import org.User.command.service.UserService;
import org.User.command.service.authService;
import org.User.event.KafkaEvent;
import org.User.event.KafkaEventProducer;
import org.User.event.KafkaTopic;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
public class UserCommandController {
    @Autowired
    private UserService userService;
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private authService authService;
    @Autowired
    private KafkaEventProducer kafkaEventProducer;

    @PostMapping("/assign-roles")
    public CompletableFuture<String> assignRolesToUser(@RequestBody AssignRoleRequest request) {
        return userService.assignRoles(request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_BAN')")
    public CompletableFuture<String> updateStatus(
            @PathVariable String id,
            @RequestParam boolean active) {
        return commandGateway.send(new UpdateUserStatusCommand(id, active)).thenApply(result -> {
            kafkaEventProducer.sendEvent(KafkaTopic.USER_EVENTS, KafkaEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(active ? "UserActivatedEvent" : "AccountLockedEvent")
                    .userId(id)
                    .referenceId(id)
                    .referenceType("USER")
                    .title(active ? "Tài khoản hoạt động" : "Tài khoản bị khóa")
                    .message(active ? "Tài khoản của bạn đã được kích hoạt lại." : "Tài khoản của bạn đã bị khóa.")
                    .createdAt(LocalDateTime.now())
                    .build());
            return (String) result;
        });
    }

    @PutMapping("/change-password")
    public CompletableFuture<String> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ChangePasswordRequest request) {
        String userId = jwt.getSubject();
        authService.updatePasswordInKeycloak(userId, request.getNewPassword());
        
        kafkaEventProducer.sendEvent(KafkaTopic.USER_EVENTS, KafkaEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PasswordChangedEvent")
                .userId(userId)
                .referenceId(userId)
                .referenceType("USER")
                .title("Thay đổi mật khẩu thành công")
                .message("Mật khẩu của bạn đã được thay đổi thành công.")
                .createdAt(LocalDateTime.now())
                .build());
                
        return CompletableFuture.completedFuture("Thay đổi mật khẩu thành công");
    }
}
