package org.User.query.repository;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.AdminUserDetailResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class AdminUserDetailNativeQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AdminUserDetailResponse getUserById(String userId) {
        String sql = """
            SELECT
                u.id,
                u.email,
                u.keycloak_uid,
                u.user_type,
                u.is_active,
                u.created_at,
                u.updated_at,
                r.role_name,
                p.permission_name
            FROM users u
            LEFT JOIN user_roles ur
                ON u.id = ur.user_id
            LEFT JOIN roles r
                ON ur.role_id = r.id
            LEFT JOIN role_permissions rp
                ON r.id = rp.role_id
            LEFT JOIN permissions p
                ON rp.permission_id = p.id
            WHERE u.id = :userId
        """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);

        return jdbcTemplate.query(sql, params, rs -> {

            AdminUserDetailResponse.AdminUserDetailResponseBuilder builder = null;

            Set<String> roles = new LinkedHashSet<>();
            Set<String> permissions = new LinkedHashSet<>();

            while (rs.next()) {
                if (builder == null) {
                    builder = AdminUserDetailResponse.builder()
                            .id(rs.getString("id"))
                            .email(rs.getString("email"))
                            .keycloakUid(rs.getString("keycloak_uid"))
                            .userType(rs.getString("user_type"))
                            .isActive(rs.getBoolean("is_active"))
                            .createdAt(toLocalDateTime(rs.getTimestamp("created_at")))
                            .updatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
                }

                String roleName = rs.getString("role_name");
                if (roleName != null) {
                    roles.add(roleName);
                }

                String permissionName = rs.getString("permission_name");
                if (permissionName != null) {
                    permissions.add(permissionName);
                }
            }
            if (builder == null) {
                return null;
            }

            return builder
                    .roles(roles)
                    .permissions(permissions)
                    .build();
        });
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null
                ? null
                : timestamp.toLocalDateTime();
    }
}
