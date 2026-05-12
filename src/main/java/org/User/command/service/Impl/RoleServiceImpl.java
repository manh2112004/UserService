package org.User.command.service.Impl;

import org.User.command.command.CreateRoleCommand;
import org.User.command.model.request.CreateRoleRequest;
import org.User.command.service.RoleService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private CommandGateway commandGateway;
    @Override
    public CompletableFuture<Object> createRole(CreateRoleRequest request) {
        // Ánh xạ từ Request sang Command để gửi vào Axon Bus
        CreateRoleCommand command = CreateRoleCommand.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .permissionIds(request.getPermissionIds())
                .build();
        return commandGateway.send(command);
    }
}
