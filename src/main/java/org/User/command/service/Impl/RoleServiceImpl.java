package org.User.command.service.Impl;

import jakarta.ws.rs.NotFoundException;
import org.User.command.command.*;
import org.User.command.data.*;
import org.User.command.model.request.AssignPermissionToRoleRequest;
import org.User.command.model.request.CreatePermissionRequest;
import org.User.command.model.request.CreateRoleRequest;
import org.User.command.model.request.UpdateRoleRequest;
import org.User.command.service.RoleService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Override
    public CompletableFuture<Object> createRole(CreateRoleRequest request) {
        // Ánh xạ từ Request sang Command để gửi vào Axon Bus
        CreateRoleCommand command = CreateRoleCommand.builder()

                .roleName(request.getRoleName())
                .description(request.getDescription())
                .build();
        return commandGateway.send(command);
    }

    @Override
    public CompletableFuture<String> processCreatePermission(CreatePermissionRequest request) {
       // Service chịu trách nhiệm tạo Command từ Request
        String tempId = java.util.UUID.randomUUID().toString();
        // 2. Gửi Command kèm theo ID này
        CreatePermissionCommand command = new CreatePermissionCommand(
                tempId, // Thêm field này vào constructor của Command
                request.getPermissionName(),
                request.getDescription()
        );
        return commandGateway.send(command);
    }

    @Override
    public String createPermissionInKeycloak(String name, String description){
        // 1. Khởi tạo RoleRepresentation
        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName(name);
        roleRep.setDescription(description);
        roleRep.setClientRole(true);

        // 2. Lấy UUID của Client (Dùng các biến từ application.yaml đã cấu hình)
        String clientUuid = keycloak.realm(realm)
                .clients()
                .findByClientId(clientId)
                .get(0)
                .getId();

        // 3. Thực hiện tạo Role (Hàm này trả về void nên không gán vào biến nào cả)
        keycloak.realm(realm)
                .clients()
                .get(clientUuid)
                .roles()
                .create(roleRep);

        // 4. Truy vấn lại ngay lập tức để lấy ID mà Keycloak vừa sinh ra
        // Vì permissionName là duy nhất nên cách này rất an toàn
        return keycloak.realm(realm)
                .clients()
                .get(clientUuid)
                .roles()
                .get(name)
                .toRepresentation()
                .getId();
    }

    @Override
    public String createRoleInKeycloak(CreateRoleCommand command) {
        // 1. Lấy UUID của Client
        String clientUuid = keycloak.realm(realm).clients().findByClientId(clientId).get(0).getId();
        RolesResource rolesResource = keycloak.realm(realm).clients().get(clientUuid).roles();

        // 2. Tạo Role trên Keycloak (xử lý nếu đã tồn tại)
        try {
            RoleRepresentation roleRep = new RoleRepresentation();
            roleRep.setName(command.getRoleName());
            roleRep.setDescription(command.getDescription());
            rolesResource.create(roleRep);
        } catch (jakarta.ws.rs.WebApplicationException e) {
            if (e.getResponse().getStatus() != 409) throw e;
        }

        RoleResource roleResource = rolesResource.get(command.getRoleName());

        // 3. Nếu permissionNames là mảng rỗng -> Trả về ID luôn (Composite sẽ tự là False)
        if (command.getPermissionNames() == null || command.getPermissionNames().isEmpty()) {
            return roleResource.toRepresentation().getId();
        }

        // 4. Lấy danh sách các quyền ĐÃ GÁN vào Role này trên Keycloak để tránh check trùng
        Set<String> existingSubRoles = roleResource.getRoleComposites().stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());

        // 5. Duyệt qua danh sách permissionNames gửi lên
        List<RoleRepresentation> subRolesToAdd = new ArrayList<>();

        for (String pName : command.getPermissionNames()) {
            // BƯỚC CHECK DB: Kiểm tra xem permission có tồn tại trong bảng permissions của MySQL không
            boolean existsInDb = permissionRepository.existsByPermissionName(pName);

            if (!existsInDb) {
                // Thông báo nếu permission chưa tồn tại trong DB
                System.err.println("Thông báo: Permission '" + pName + "' chưa tồn tại trong hệ thống!");
                continue; // Bỏ qua cái này, tiếp tục cái sau
            }

            // BƯỚC CHECK TRÙNG TRONG ROLE: Nếu đã gán rồi thì thôi, chưa gán mới thêm vào list
            if (!existingSubRoles.contains(pName)) {
                try {
                    subRolesToAdd.add(rolesResource.get(pName).toRepresentation());
                } catch (Exception e) {
                    System.err.println("Lỗi: Không tìm thấy permission '" + pName + "' trên Keycloak");
                }
            }
        }

        // 6. Gán các quyền hợp lệ vào Role
        if (!subRolesToAdd.isEmpty()) {
            roleResource.addComposites(subRolesToAdd);
        }

        return roleResource.toRepresentation().getId();
    }

    @Override
    public CompletableFuture<String> assignPermissionsToRole(AssignPermissionToRoleRequest request) {
        // 1. Kiểm tra Role có tồn tại trong DB MySQL không
        if (!roleRepository.existsById(request.getRoleId())) {
            throw new RuntimeException("Role không tồn tại!");
        }
        // 2. Gửi Command sang Aggregate
        return commandGateway.send(new AssignPermissionToRoleCommand(
                request.getRoleId(),
                request.getPermissionIds()
        ));
    }

    @Override
    public void assignPermissionsToRole(String roleId, List<String> permissionIds) {
        // 1. Tìm ID của Client trên Keycloak
        String clientUuid = keycloak.realm(realm).clients().findByClientId(clientId).get(0).getId();

        // 2. Chuyển đổi list ID từ Postman thành list NAME thực tế từ bảng permissions
        List<String> permissionNames = permissionRepository.findAllById(permissionIds)
                .stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toList());

        // 3. Lấy thông tin Role cha từ DB
        Role roleEntity = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại trong DB"));

        // 4. Tìm Role cha trên Keycloak bằng NAME (RECRUITER_LEAD)
        RoleResource roleResource = keycloak.realm(realm)
                .clients()
                .get(clientUuid)
                .roles()
                .get(roleEntity.getRoleName());

        // 5. Lấy danh sách RoleRepresentation của các Permission con
        List<RoleRepresentation> composites = permissionNames.stream()
                .map(pName -> keycloak.realm(realm).clients().get(clientUuid).roles().get(pName).toRepresentation())
                .collect(Collectors.toList());

        // 6. Gán vào Keycloak
        if (!composites.isEmpty()) {
            roleResource.addComposites(composites);
        }
    }

    @Override
    public CompletableFuture<String> deleteRole(String roleId) {
        checkDeleteRolePermission();

        if (!roleRepository.existsById(roleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role không tồn tại");
        }

        return commandGateway.send(new DeleteRoleCommand(roleId));
    }

    @Override
    public CompletableFuture<String> updateRole(String roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Role not found"
                        )
                );

        if (request.getRoleName() == null || request.getRoleName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role name is required"
            );
        }

        String oldRoleName = role.getRoleName();
        String newRoleName = request.getRoleName().trim();

        if (!oldRoleName.equalsIgnoreCase(newRoleName)
                && roleRepository.existsByRoleName(newRoleName)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role name already exists"
            );
        }

        updateRoleInKeycloak(
                oldRoleName,
                newRoleName,
                request.getDescription()
        );

        return commandGateway.send(
                UpdateRoleCommand.builder()
                        .id(roleId)
                        .roleName(newRoleName)
                        .description(request.getDescription())
                        .build()
        );
    }

    @Override
    public void updateRoleInKeycloak(String oldRoleName, String newRoleName, String description) {
        try {
            String clientUuid = keycloak.realm(realm)
                    .clients()
                    .findByClientId(clientId)
                    .get(0)
                    .getId();

            RoleResource roleResource = keycloak.realm(realm)
                    .clients()
                    .get(clientUuid)
                    .roles()
                    .get(oldRoleName);

            RoleRepresentation roleRep =
                    roleResource.toRepresentation();

            roleRep.setName(newRoleName);
            roleRep.setDescription(description);

            roleResource.update(roleRep);

        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Role not found in Keycloak"
            );
        }
    }

    @Override
    public void deleteRoleInKeycloak(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại trong DB"));

        String clientUuid = keycloak.realm(realm)
                .clients()
                .findByClientId(clientId)
                .get(0)
                .getId();

        try {
            keycloak.realm(realm)
                    .clients()
                    .get(clientUuid)
                    .roles()
                    .get(role.getRoleName())
                    .remove();
        } catch (NotFoundException ignored) {
            // Role đã không còn trên Keycloak, vẫn tiếp tục dọn dữ liệu trong DB.
        }
    }

    private void checkDeleteRolePermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasPermission = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("USER_DELETE")
                                || authority.getAuthority().equals("ROLE_USER_DELETE")
                );

        if (!hasPermission && authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            hasPermission = hasRoleInJwtClaims(jwtAuthentication.getTokenAttributes(), "USER_DELETE");
        }

        if (!hasPermission && authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            hasPermission = userRepository.findByKeycloakUid(jwtAuthentication.getName())
                    .map(user -> user.getRoles().stream()
                            .flatMap(role -> role.getPermissions().stream())
                            .anyMatch(permission -> permission.getPermissionName().equals("USER_DELETE")))
                    .orElse(false);
        }

        if (!hasPermission) {
            throw new AccessDeniedException("Bạn không có quyền USER_DELETE để xóa role");
        }
    }

    private boolean hasRoleInJwtClaims(Map<String, Object> claims, String roleName) {
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> realmAccessMap
                && containsRole(realmAccessMap.get("roles"), roleName)) {
            return true;
        }

        Object resourceAccess = claims.get("resource_access");
        if (!(resourceAccess instanceof Map<?, ?> resourceAccessMap)) {
            return false;
        }

        Object clientAccess = resourceAccessMap.get(clientId);
        if (clientAccess instanceof Map<?, ?> clientAccessMap
                && containsRole(clientAccessMap.get("roles"), roleName)) {
            return true;
        }

        return resourceAccessMap.values().stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .anyMatch(access -> containsRole(access.get("roles"), roleName));
    }

    private boolean containsRole(Object roles, String roleName) {
        return roles instanceof Collection<?> roleCollection
                && roleCollection.stream().anyMatch(roleName::equals);
    }
}
