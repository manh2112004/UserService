package org.User.command.model.request;

import lombok.Data;

@Data
public class CreatePermissionRequest {
    private String permissionName; // ví dụ: USER_UPDATE_STATUS
    private String description;
}
