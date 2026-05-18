package org.User.query.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionDetailResponse {
    private String id;

    private String permissionName;

    private String description;
}
