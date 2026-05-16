package org.User.query.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionResponse {
    private String id;

    private String permissionName;

    private String description;
}
