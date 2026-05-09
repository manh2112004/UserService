package org.User.command.service;

import org.User.command.model.request.LoginRequestModel;
import org.User.command.model.request.RegisterRequestModel;
import org.User.command.model.response.LoginResponseDTO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface authService {
    CompletableFuture<String> registerUser(RegisterRequestModel model);
    LoginResponseDTO login(LoginRequestModel model);
    Map<String,Object>refreshToken(String refreshToken);
    void logout(String refreshToken);
    void verifyEmailInKeycloak(String userId);
    String findUserIdByEmail(String email);
    void updatePasswordInKeycloak(String userId,String newPassword);
}
