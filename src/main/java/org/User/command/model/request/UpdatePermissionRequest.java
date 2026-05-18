package org.User.command.model.request;

import lombok.Data;

@Data
public class UpdatePermissionRequest {
    private String permissionName;
    private String description;
}
