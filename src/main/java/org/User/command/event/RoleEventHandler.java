package org.User.command.event;

import org.User.command.data.Permission;
import org.User.command.data.PermissionRepository;
import org.User.command.data.Role;
import org.User.command.data.RoleRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashSet;
@Component
public class RoleEventHandler {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @EventHandler
    public void on(RoleCreatedEvent event) {
        Role role = new Role();
        role.setRoleName(event.getRoleName());
        role.setDescription(event.getDescription());

        if (event.getPermissionIds() != null) {
            role.setPermissions(new HashSet<>(permissionRepository.findAllById(event.getPermissionIds())));
        }
        roleRepository.save(role);
    }
}
