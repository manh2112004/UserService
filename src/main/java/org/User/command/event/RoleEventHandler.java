package org.User.command.event;

import org.User.command.data.Permission;
import org.User.command.data.PermissionRepository;
import org.User.command.data.Role;
import org.User.command.data.RoleRepository;
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
}
