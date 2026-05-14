package org.User.command.controller;

import org.User.command.command.AssignPermissionToRoleCommand;
import org.User.command.command.CreatePermissionCommand;
import org.User.command.command.CreateRoleCommand;
import org.User.command.data.RoleRepository;
import org.User.command.model.request.AssignPermissionToRoleRequest;
import org.User.command.model.request.AssignRoleRequest;
import org.User.command.model.request.CreatePermissionRequest;
import org.User.command.model.request.CreateRoleRequest;
import org.User.command.service.RoleService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
@RestController
@RequestMapping("/api/v1")
public class RoleCommandController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private RoleRepository roleRepository;
    @PostMapping("/roles")
    public CompletableFuture<String> createRole(@RequestBody CreateRoleRequest request) {
        if (roleRepository.existsByRoleName(request.getRoleName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role already exists");
        }
        return commandGateway.send(new CreateRoleCommand(
                UUID.randomUUID().toString(),
                request.getRoleName(),
                request.getDescription(),
                request.getPermissionNames()
        ));
    }
    @PostMapping("/permissions")
    public CompletableFuture<String> createPermission(@RequestBody CreatePermissionRequest request) {
        return roleService.processCreatePermission(request);
    }
    @PostMapping("/assign-permissions")
    public CompletableFuture<String> assignPermissions(@RequestBody AssignPermissionToRoleRequest request) {
        // Controller gọi Service xử lý
        return roleService.assignPermissionsToRole(request);
    }
}
