package org.User.query.model.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class RoleDetailResponse {
    private String id;

    private String roleName;

    private String description;

    private Set<String> permissions;
}
