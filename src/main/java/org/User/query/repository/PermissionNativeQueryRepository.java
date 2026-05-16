package org.User.query.repository;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.PermissionResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class PermissionNativeQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<PermissionResponse> getPermissions() {
        String sql = """
            SELECT
                p.id,
                p.permission_name,
                p.description
            FROM permissions p
            ORDER BY p.permission_name
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> PermissionResponse.builder()
                        .id(rs.getString("id"))
                        .permissionName(rs.getString("permission_name"))
                        .description(rs.getString("description"))
                        .build()
        );
    }
}
