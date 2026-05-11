package org.User.query.model;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String userType;
    private String keycloakUid;
    private boolean isActive;
    private LocalDateTime createdAt;
}
