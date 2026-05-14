package org.User.command.model.request;

import lombok.Data;

import java.util.List;

@Data
public class AssignRoleRequest {
    private String userId; // UUID của User trên Keycloak
    private List<String> roleNames; // Danh sách tên các Role cần gán
}
