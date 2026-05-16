package org.User.command.service.Impl;

import org.User.command.command.AssignRoleToUserCommand;
import org.User.command.command.UpdateUserStatusCommand;
import org.User.command.data.*;
import org.User.command.model.request.AssignRoleRequest;
import org.User.command.service.UserService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId;

    @Override
    public void updateUserStatusInKeycloak(String userId, Boolean isActive) {
        // Tìm user trong DB để lấy keycloak_uid
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Cập nhật trạng thái trên Keycloak
        UserResource userResource = keycloak.realm(realm).users().get(user.getKeycloakUid());
        UserRepresentation userRep = userResource.toRepresentation();
        userRep.setEnabled(isActive);
        userResource.update(userRep);
    }

    @Override
    public void checkAdminPermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("USER_MANAGEMENT"));

        if (!isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền thay đổi trạng thái người dùng!");
        }
    }

    @Override
    public void assignRolesToUserInKeycloak(String keycloakUserId, List<String> roleNames) {
        RealmResource realmResource = keycloak.realm(realm);

        UserResource userResource = realmResource
                .users()
                .get(keycloakUserId);
        ClientRepresentation client = realmResource
                .clients()
                .findByClientId(clientId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy client: " + clientId));

        ClientResource clientResource = realmResource
                .clients()
                .get(client.getId());

        List<RoleRepresentation> keycloakRoles = roleNames.stream()
                .map(roleName -> clientResource.roles()
                        .get(roleName)
                        .toRepresentation())
                .toList();

        userResource.roles()
                .clientLevel(client.getId())
                .add(keycloakRoles);
    }
    @Override
    public CompletableFuture<String> assignRoles(AssignRoleRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new RuntimeException("userId không được để trống");
        }

        if (request.getRoleNames() == null || request.getRoleNames().isEmpty()) {
            throw new RuntimeException("roleNames không được để trống");
        }

        if (!userRepository.existsByKeycloakUid(request.getUserId())) {
            throw new RuntimeException("User không tồn tại");
        }

        Set<String> requestedNames = new HashSet<>(request.getRoleNames());

        Set<Permission> permissions =
                permissionRepository.findAllByPermissionNameIn(requestedNames);

        if (!permissions.isEmpty()) {
            Set<String> permissionNames = permissions.stream()
                    .map(Permission::getPermissionName)
                    .collect(Collectors.toSet());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không được gán permission trực tiếp cho user: " + permissionNames
            );
        }

        Set<Role> roles = roleRepository.findAllByRoleNameIn(requestedNames);

        if (roles.size() != requestedNames.size()) {
            Set<String> existingRoleNames = roles.stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toSet());

            requestedNames.removeAll(existingRoleNames);

            throw new RuntimeException("Role không tồn tại: " + requestedNames);
        }

        return commandGateway.send(
                new AssignRoleToUserCommand(
                        request.getUserId(),
                        new ArrayList<>(requestedNames)
                )
        );
    }
}
