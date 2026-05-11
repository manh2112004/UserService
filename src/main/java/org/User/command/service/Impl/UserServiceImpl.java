package org.User.command.service.Impl;

import lombok.RequiredArgsConstructor;
import org.User.command.command.UpdateUserCommand;
import org.User.command.data.UserRepository;
import org.User.command.model.request.UserUpdateRequest;
import org.User.command.service.userService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements userService {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private UserRepository userRepository;
    public CompletableFuture<String> updateUser(String userId, UserUpdateRequest request) {
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new RuntimeException("Email đã được sử dụng bởi người dùng khác!");
        }
        if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
            throw new RuntimeException("Số điện thoại đã được sử dụng!");
        }
        UpdateUserCommand command = UpdateUserCommand.builder()
                .userId(userId)
                .username(request.getUsername())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .build();
        return commandGateway.send(command);
    }
}
