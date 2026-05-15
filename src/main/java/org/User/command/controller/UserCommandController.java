package org.User.command.controller;

import org.User.command.command.UpdateUserStatusCommand;
import org.User.command.model.request.AssignRoleRequest;
import org.User.command.service.UserService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
public class UserCommandController {
    @Autowired
    private UserService userService;
    @Autowired
    private CommandGateway commandGateway;
    @PostMapping("/assign-roles")
    public CompletableFuture<String> assignRolesToUser(@RequestBody AssignRoleRequest request) {
        // Controller nhận request và chuyển tiếp sang Service xử lý
        return userService.assignRoles(request);
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_BAN')")
    public CompletableFuture<String> updateStatus(
            @PathVariable String id,
            @RequestParam boolean active) {
        return commandGateway.send(new UpdateUserStatusCommand(id, active));
    }
}
