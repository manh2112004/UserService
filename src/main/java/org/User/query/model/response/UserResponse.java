package org.User.query.model.response;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private String id;
    private String email;
    private String userType;
    private boolean isActive;
    private List<String> roles;
    private List<String> permissions;
}