package org.User.command.service;

import org.User.command.command.CreateRoleCommand;
import org.User.command.model.request.CreateRoleRequest;
import org.User.command.model.request.PermissionRequest;

import java.util.concurrent.CompletableFuture;

public interface RoleService {
    CompletableFuture<Object> createRole(CreateRoleRequest request);
    void createPermission(PermissionRequest request);
}
