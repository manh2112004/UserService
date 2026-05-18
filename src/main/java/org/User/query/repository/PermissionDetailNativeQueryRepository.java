package org.User.query.repository;

import lombok.RequiredArgsConstructor;
import org.User.query.model.response.PermissionDetailResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PermissionDetailNativeQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PermissionDetailResponse getPermissionById(
            String permissionId
    ) {

        String sql = """
            SELECT
                p.id,
                p.permission_name,
                p.description
            FROM permissions p
            WHERE p.id = :permissionId
        """;

        MapSqlParameterSource params =
                new MapSqlParameterSource();

        params.addValue("permissionId", permissionId);

        return jdbcTemplate.query(
                sql,
                params,
                rs -> {

                    if (!rs.next()) {
                        return null;
                    }

                    return PermissionDetailResponse.builder()
                            .id(rs.getString("id"))
                            .permissionName(
                                    rs.getString("permission_name")
                            )
                            .description(
                                    rs.getString("description")
                            )
                            .build();
                }
        );
    }
}
