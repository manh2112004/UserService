package org.User.query.queries;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.RoleDetailResponse;
import org.User.query.repository.RoleDetailNativeQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
@RequiredArgsConstructor
public class GetRoleDetailQueryHandler {
    private final RoleDetailNativeQueryRepository repository;

    public RoleDetailResponse handle(GetRoleDetailQuery query) {

        RoleDetailResponse response =
                repository.getRoleById(query.getRoleId());

        if (response == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Role not found"
            );
        }

        return response;
    }
}
