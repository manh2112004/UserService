package org.User.command.service;

import org.User.command.command.CreateRoleCommand;
import org.User.command.model.request.AssignRoleRequest;
import org.User.command.model.request.CreatePermissionRequest;
import org.User.command.model.request.CreateRoleRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RoleService {
    CompletableFuture<Object> createRole(CreateRoleRequest request);
    CompletableFuture<String> processCreatePermission(CreatePermissionRequest request);
    String createPermissionInKeycloak(String name, String description);
    String createRoleInKeycloak(CreateRoleCommand command);
}
