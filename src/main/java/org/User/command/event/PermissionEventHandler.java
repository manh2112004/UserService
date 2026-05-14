package org.User.command.event;

import org.User.command.data.Permission;
import org.User.command.data.PermissionRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PermissionEventHandler {
    @Autowired
    private PermissionRepository permissionRepository;
    @EventHandler
    public void on(PermissionCreatedEvent event) {
        if (event.getId() == null) {
            System.out.println("Bỏ qua Event vì ID bị null: " + event.getPermissionName());
            return;
        }
        if (permissionRepository.existsById(event.getId())) {
            return;
        }
        Permission permission = new Permission();
        // Bạn phải lấy ID (đã lấy từ Keycloak) gán vào Entity
        permission.setId(event.getId());
        permission.setPermissionName(event.getPermissionName());
        permission.setDescription(event.getDescription());
        permissionRepository.save(permission);
    }
}
