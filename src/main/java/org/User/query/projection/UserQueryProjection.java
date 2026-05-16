package org.User.query.projection;

import org.User.command.data.Permission;
import org.User.command.data.Role;
import org.User.command.data.User;
import org.User.command.data.UserRepository;
import org.User.query.model.response.UserResponse;
import org.User.query.queries.GetUserProfileQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class UserQueryProjection {
    @Autowired
    private UserRepository userRepository;

    @QueryHandler
    public UserResponse handle(GetUserProfileQuery query) {
        User user = userRepository.findByKeycloakUid(query.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .userType(user.getUserType().name())
                .isActive(user.isActive())
                .roles(user.getRoles().stream()
                        .map(Role::getRoleName)
                        .toList())
                .permissions(user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permission::getPermissionName)
                        .distinct()
                        .toList())
                .build();
    }
}
