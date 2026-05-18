package org.User.command.event;

import org.User.command.data.Permission;
import org.User.command.data.PermissionRepository;
import org.User.command.data.RoleRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PermissionEventHandler {
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RoleRepository roleRepository;
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

    @EventHandler
    public void on(PermissionUpdatedEvent event) {
        if (event.getId() == null || event.getId().isBlank()) {
            System.out.println("Bỏ qua PermissionUpdatedEvent vì ID bị null");
            return;
        }

        Permission permission = permissionRepository.findById(event.getId())
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        permission.setPermissionName(event.getPermissionName());
        permission.setDescription(event.getDescription());
        permissionRepository.save(permission);
    }

    @EventHandler
    @Transactional
    public void on(PermissionDeletedEvent event) {
        permissionRepository.findById(event.getId()).ifPresent(permission -> {
            roleRepository.findAll().forEach(role -> {
                if (role.getPermissions().removeIf(existingPermission ->
                        existingPermission.getId().equals(event.getId()))) {
                    roleRepository.save(role);
                }
            });

            permissionRepository.delete(permission);
        });
    }
}
