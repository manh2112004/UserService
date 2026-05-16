package org.User.command.event;

import lombok.RequiredArgsConstructor;
import org.User.command.data.Role;
import org.User.command.data.RoleRepository;
import org.User.command.data.User;
import org.User.command.data.UserRepository;
import org.User.constant.UserType;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class authEventsHandler {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @EventHandler
    public void on(UserCreatedEvent event) {
        User userEntity = new User();
        userEntity.setId(event.getUserId()); // Dùng ID từ Keycloak
        userEntity.setEmail(event.getEmail());
        userEntity.setUserType(UserType.valueOf(event.getUserType()));
        userEntity.setKeycloakUid(event.getUserId());
        userRepository.save(userEntity);
    }
    @EventHandler
    public void on(UserEmailVerifiedEvent event) {
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
    }
    @EventHandler
    public void on(RolesAssignedToUserEvent event) {
        User user = userRepository.findByKeycloakUid(event.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy User có keycloak_uid = " + event.getUserId()
                ));
        Set<Role> assignedRoles =
                roleRepository.findAllByRoleNameIn(event.getRoleNames());
        user.getRoles().addAll(assignedRoles);
        userRepository.save(user);
        System.out.println(
                "Đã gán role cho user " + event.getUserId()
                        + ": " + event.getRoleNames()
        );
    }
}
