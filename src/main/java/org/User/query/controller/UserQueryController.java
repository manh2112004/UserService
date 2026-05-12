package org.User.query.controller;

import org.User.query.model.UserResponse;
import org.User.query.queries.GetUserProfileQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/users")
public class UserQueryController {
    @Autowired
    private QueryGateway queryGateway;

    @GetMapping("/me")
    public CompletableFuture<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        // Lấy userId (sub) từ token JWT
        String userId = jwt.getSubject();
        return queryGateway.query(
                new GetUserProfileQuery(userId),
                ResponseTypes.instanceOf(UserResponse.class)
        );
    }
}
