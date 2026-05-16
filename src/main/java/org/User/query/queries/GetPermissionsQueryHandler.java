package org.User.query.queries;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.PermissionResponse;
import org.User.query.repository.PermissionNativeQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPermissionsQueryHandler {
    private final PermissionNativeQueryRepository repository;

    public List<PermissionResponse> handle(GetPermissionsQuery query) {
        return repository.getPermissions();
    }
}
