package org.User.query.repository;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.RoleResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class RoleNativeQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<RoleResponse> getRoles() {

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
            ORDER BY r.role_name
        """;

        return jdbcTemplate.query(sql, rs -> {

            Map<String, RoleResponse.RoleResponseBuilder> roleMap =
                    new LinkedHashMap<>();

            Map<String, Set<String>> permissionMap =
                    new LinkedHashMap<>();

            while (rs.next()) {

                String roleId = rs.getString("id");

                roleMap.putIfAbsent(
                        roleId,
                        RoleResponse.builder()
                                .id(roleId)
                                .roleName(rs.getString("role_name"))
                                .description(rs.getString("description"))
                );

                permissionMap.putIfAbsent(
                        roleId,
                        new LinkedHashSet<>()
                );

                String permissionName =
                        rs.getString("permission_name");

                if (permissionName != null) {
                    permissionMap
                            .get(roleId)
                            .add(permissionName);
                }
            }

            List<RoleResponse> result = new ArrayList<>();

            for (String roleId : roleMap.keySet()) {

                result.add(
                        roleMap.get(roleId)
                                .permissions(permissionMap.get(roleId))
                                .build()
                );
            }
            return result;
        });
    }
}
