package org.User.command.event;

import org.User.command.data.*;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component
public class RoleEventHandler {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private UserRepository userRepository;
    @EventHandler
    public void on(RoleCreatedEvent event) {
        // Lưu Role với ID từ Keycloak
        Role role = new Role();
        role.setId(event.getId());
        role.setRoleName(event.getRoleName());
        role.setDescription(event.getDescription());
        // Ánh xạ các Permission từ DB dựa trên tên
        if (event.getPermissionNames() != null) {
            Set<Permission> permissions = permissionRepository
                    .findAllByPermissionNameIn(event.getPermissionNames());
            role.setPermissions(permissions);
        }
        roleRepository.save(role);
    }
    @EventHandler
    public void on(RolesAssignedToUserEvent event) {
        // 1. Tìm User trong DB
        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 2. Tìm các Role tương ứng trong DB dựa trên tên
        Set<Role> roles = new HashSet<>(roleRepository.findAllByRoleNameIn(event.getRoleNames()));
        // 3. Cập nhật danh sách Role cho User
        user.getRoles().addAll(roles);
        // 4. Lưu lại vào MySQL
        userRepository.save(user);
    }
}
