package org.User.query.queries;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.PermissionDetailResponse;
import org.User.query.repository.PermissionDetailNativeQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
@RequiredArgsConstructor
public class GetPermissionDetailQueryHandler {
    private final PermissionDetailNativeQueryRepository repository;

    public PermissionDetailResponse handle(
            GetPermissionDetailQuery query
    ) {

        PermissionDetailResponse response =
                repository.getPermissionById(
                        query.getPermissionId()
                );

        if (response == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Permission not found"
            );
        }

        return response;
    }
}
