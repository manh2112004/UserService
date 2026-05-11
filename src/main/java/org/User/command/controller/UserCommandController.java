package org.User.command.controller;

import lombok.RequiredArgsConstructor;
import org.User.command.command.UpdateUserAvatarCommand;
import org.User.command.model.request.UserUpdateRequest;
import org.User.command.service.Impl.UserServiceImpl;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserCommandController {
   @Autowired
   private UserServiceImpl userService;
    @Autowired
    private CommandGateway commandGateway;
    @PutMapping("/me")
    public CompletableFuture<String> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserUpdateRequest request) {
        return userService.updateUser(jwt.getSubject(), request);
    }
    @PostMapping("/me/avatar")
    public CompletableFuture<ResponseEntity<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        // Lấy userId từ token Keycloak để đảm bảo đúng chủ sở hữu
        String userId = jwt.getSubject();
        // Dùng thenCompose để nối 2 "lời hứa" lại với nhau
        return userService.uploadImage(file) // Giả sử hàm này trả về CompletableFuture<String>
                .thenCompose(url ->
                        commandGateway.send(new UpdateUserAvatarCommand(userId, url))
                                .thenApply(result -> ResponseEntity.ok(url))
                );
    }
    @DeleteMapping("/me/avatar")
    public CompletableFuture<ResponseEntity<String>> deleteAvatar(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return userService.deleteAvatar(jwt.getSubject())
                .thenApply(result -> ResponseEntity.ok("Đã xóa ảnh đại diện thành công!"));
    }
}
