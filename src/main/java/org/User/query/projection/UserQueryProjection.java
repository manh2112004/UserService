package org.User.query.projection;

import org.User.command.data.UserRepository;
import org.User.query.model.UserResponse;
import org.User.query.queries.GetUserProfileQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class UserQueryProjection {
    @Autowired
    private UserRepository userRepository;

    @QueryHandler
    public UserResponse handle(GetUserProfileQuery query) {
        return userRepository.findById(query.getUserId())
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .userType(user.getUserType().name())
                        .keycloakUid(user.getKeycloakUid())
                        .isActive(user.isActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + query.getUserId()));
    }
}
