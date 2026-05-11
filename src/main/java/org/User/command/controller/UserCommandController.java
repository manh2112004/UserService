package org.User.command.controller;

import lombok.RequiredArgsConstructor;
import org.User.command.model.request.UserUpdateRequest;
import org.User.command.service.Impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserCommandController {
   @Autowired
   private UserServiceImpl userService;

    @PutMapping("/me")
    public CompletableFuture<String> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserUpdateRequest request) {
        return userService.updateUser(jwt.getSubject(), request);
    }
}
