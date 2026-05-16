package org.User.query.controller;

import lombok.RequiredArgsConstructor;
import org.User.query.model.request.GetAdminUsersRequest;
import org.User.query.model.response.AdminUserDetailResponse;
import org.User.query.model.response.AdminUserResponse;
import org.User.query.model.response.PageResponse;
import org.User.query.model.response.UserResponse;
import org.User.query.queries.*;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminQueryController {
    @Autowired
    private QueryGateway queryGateway;
    private final GetAdminUsersQueryHandler getAdminUsersQueryHandler;
    private final GetAdminUserDetailQueryHandler getAdminUserDetailQueryHandler;
    @GetMapping("/me")
    public CompletableFuture<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        // Lấy userId (sub) từ token JWT
        String userId = jwt.getSubject();
        return queryGateway.query(
                new GetUserProfileQuery(userId),
                ResponseTypes.instanceOf(UserResponse.class)
        );
    }
    @GetMapping
    public PageResponse<AdminUserResponse> getUsers(GetAdminUsersRequest request) {
        GetAdminUsersQuery query = new GetAdminUsersQuery(
                request.getEmail(),
                request.getUserType(),
                request.getIsActive(),
                request.getRoleName(),
                request.getPage(),
                request.getSize()
        );
        return getAdminUsersQueryHandler.handle(query);
    }
    @GetMapping("/{id}")
    public AdminUserDetailResponse getUserById(
            @PathVariable("id") String id
    ) {
        GetAdminUserDetailQuery query =
                new GetAdminUserDetailQuery(id);
        return getAdminUserDetailQueryHandler.handle(query);
    }
}
