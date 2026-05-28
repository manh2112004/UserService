package org.User.command.model.request;

import lombok.Data;

import java.util.List;

@Data
public class CreatePermissionsRequest {
    private List<CreatePermissionRequest> permissions;
}
