package org.User.query.queries;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.RoleResponse;
import org.User.query.repository.RoleNativeQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetRolesQueryHandler {
    private final RoleNativeQueryRepository repository;

    public List<RoleResponse> handle(GetRolesQuery query) {
        return repository.getRoles();
    }
}
