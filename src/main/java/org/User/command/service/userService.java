package org.User.command.service;

import org.User.command.model.request.UserUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface userService {
    CompletableFuture<String>updateUser(String userId, UserUpdateRequest request);
    CompletableFuture<String> uploadImage(MultipartFile file);
}
