package org.User.command.service;

import org.User.command.model.request.LoginRequestModel;
import org.User.command.model.request.RegisterRequestModel;
import org.User.command.model.response.LoginResponseDTO;

import java.util.concurrent.CompletableFuture;

public interface authService {
    CompletableFuture<String> registerUser(RegisterRequestModel model);
    LoginResponseDTO login(LoginRequestModel model);
}
