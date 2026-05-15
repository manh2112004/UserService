package org.User.command.service.Impl;

import org.User.command.command.AssignRoleToUserCommand;
import org.User.command.command.UpdateUserStatusCommand;
import org.User.command.data.User;
import org.User.command.data.UserRepository;
import org.User.command.model.request.AssignRoleRequest;
import org.User.command.service.UserService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private UserRepository userRepository;
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
    public void assignRolesToUserInKeycloak(String userId, List<String> roleNames) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        String clientUuid = keycloak.realm(realm).clients().findByClientId(clientId).get(0).getId();

        List<RoleRepresentation> rolesToAdd = roleNames.stream()
                .map(roleName -> keycloak.realm(realm).clients().get(clientUuid).roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());

        userResource.roles().clientLevel(clientUuid).add(rolesToAdd);
    }
    @Override
    public CompletableFuture<String> assignRoles(AssignRoleRequest request) {
        // 1. Kiểm tra User có tồn tại trong DB Read chưa (Guard Logic)
        if (!userRepository.existsById(request.getUserId())) {
            throw new RuntimeException("User không tồn tại!");
        }
        // 2. Gửi Command đi
        return commandGateway.send(new AssignRoleToUserCommand(
                request.getUserId(),
                request.getRoleNames()
        ));
    }
}
