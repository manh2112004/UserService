package org.User.command.service.Impl;

import org.User.command.command.CreateRoleCommand;
import org.User.command.data.Permission;
import org.User.command.data.PermissionRepository;
import org.User.command.model.request.CreateRoleRequest;
import org.User.command.model.request.PermissionRequest;
import org.User.command.service.RoleService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private PermissionRepository permissionRepository;
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

    @Override
    public void createPermission(PermissionRequest request) {
        Permission permission = new Permission();
        permission.setPermissionName(request.getPermissionName());
        permission.setDescription(request.getDescription());

        permissionRepository.save(permission);
    }
}
