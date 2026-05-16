package org.User.command.service;

import org.User.command.model.request.AssignRoleRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    void updateUserStatusInKeycloak(String userId,Boolean isActive);
    void checkAdminPermission();
    void assignRolesToUserInKeycloak(String keycloakUserId, List<String> roleNames);
    CompletableFuture<String> assignRoles(AssignRoleRequest request);
}
