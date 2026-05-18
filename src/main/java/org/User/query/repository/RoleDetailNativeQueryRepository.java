package org.User.query.repository;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.RoleDetailResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.Set;


@Repository
@RequiredArgsConstructor
public class RoleDetailNativeQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RoleDetailResponse getRoleById(String roleId) {
        String sql = """
            SELECT
                r.id,
                r.role_name,
                r.description,
                p.permission_name
            FROM roles r
            LEFT JOIN role_permissions rp
                ON r.id = rp.role_id
            LEFT JOIN permissions p
                ON rp.permission_id = p.id
            WHERE r.id = :roleId
        """;

        MapSqlParameterSource params =
                new MapSqlParameterSource();

        params.addValue("roleId", roleId);

        return jdbcTemplate.query(sql, params, rs -> {

            RoleDetailResponse.RoleDetailResponseBuilder builder =
                    null;

            Set<String> permissions =
                    new LinkedHashSet<>();

            while (rs.next()) {

                if (builder == null) {

                    builder = RoleDetailResponse.builder()
                            .id(rs.getString("id"))
                            .roleName(rs.getString("role_name"))
                            .description(rs.getString("description"));
                }

                String permissionName =
                        rs.getString("permission_name");

                if (permissionName != null) {
                    permissions.add(permissionName);
                }
            }

            if (builder == null) {
                return null;
            }

            return builder
                    .permissions(permissions)
                    .build();
        });
    }
}
