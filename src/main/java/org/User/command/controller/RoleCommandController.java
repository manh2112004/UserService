package org.User.command.controller;

import org.User.command.command.CreateRoleCommand;
import org.User.command.model.request.CreateRoleRequest;
import org.User.command.model.request.PermissionRequest;
import org.User.command.service.RoleService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
@RestController
@RequestMapping("/api/v1/")
public class RoleCommandController {
    @Autowired
    private RoleService roleService;

    @PostMapping("/roles")
    public CompletableFuture<ResponseEntity<String>> createRole(@RequestBody CreateRoleRequest request) {
        return roleService.createRole(request)
                .thenApply(result -> ResponseEntity.ok("Role creation process started"));
    }
    @PostMapping("/permissions")
    public ResponseEntity<String> createPermission(@RequestBody PermissionRequest request) {
        roleService.createPermission(request);
        return new ResponseEntity<>("Permission created successfully", HttpStatus.CREATED);
    }
}
