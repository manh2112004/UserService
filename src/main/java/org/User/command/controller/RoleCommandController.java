package org.User.command.controller;

import org.User.command.command.CreateRoleCommand;
import org.User.command.model.request.CreateRoleRequest;
import org.User.command.service.RoleService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
@RestController
@RequestMapping("/api/v1/roles")
public class RoleCommandController {
    @Autowired
    private RoleService roleService;

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> createRole(@RequestBody CreateRoleRequest request) {
        return roleService.createRole(request)
                .thenApply(result -> ResponseEntity.ok("Role creation process started"));
    }
}
