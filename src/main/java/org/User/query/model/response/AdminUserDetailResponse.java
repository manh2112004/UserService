package org.User.query.model.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class AdminUserDetailResponse {
    private String id;

    private String email;

    private String keycloakUid;

    private String userType;

    private Boolean isActive;

    private Set<String> roles;

    private Set<String> permissions;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
