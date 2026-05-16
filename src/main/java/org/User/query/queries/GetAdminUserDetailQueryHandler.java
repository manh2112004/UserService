package org.User.query.queries;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.AdminUserDetailResponse;
import org.User.query.repository.AdminUserDetailNativeQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GetAdminUserDetailQueryHandler {
    private final AdminUserDetailNativeQueryRepository repository;

    public AdminUserDetailResponse handle(GetAdminUserDetailQuery query) {

        AdminUserDetailResponse response =
                repository.getUserById(query.getUserId());

        if (response == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        return response;
    }
}
