package org.User.command.model.request;

import lombok.Data;

import java.util.List;
@Data
public class AssignPermissionToRoleRequest {
    private String roleId;
    private List<String> permissionIds;
}
