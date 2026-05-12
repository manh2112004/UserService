package org.User.query.model;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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