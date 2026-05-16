package org.User.query.repository;
import lombok.RequiredArgsConstructor;
import org.User.query.model.response.AdminUserResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AdminUserNativeQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    public List<AdminUserResponse> searchUsers(
            String email,
            String userType,
            Boolean isActive,
            String roleName,
            int page,
            int size
    ) {
        // Object dùng để chứa các parameter truyền vào SQL
        MapSqlParameterSource params = new MapSqlParameterSource();

        StringBuilder userSql = new StringBuilder("""
            SELECT DISTINCT
                u.id,
                u.email,
                u.keycloak_uid,
                u.user_type,
                u.is_active,
                u.created_at,
                u.updated_at
            FROM users u
            LEFT JOIN user_roles ur ON u.id = ur.user_id
            LEFT JOIN roles r ON ur.role_id = r.id
            WHERE 1 = 1
        """);
        // Append các điều kiện filter nếu user truyền vào
        appendFilters(userSql, params, email, userType, isActive, roleName);
        // Sắp xếp theo thời gian tạo mới nhất
        // LIMIT = số phần tử mỗi trang
        // OFFSET = bỏ qua bao nhiêu record
        userSql.append("""
            ORDER BY u.created_at DESC
            LIMIT :limit OFFSET :offset
        """);

        params.addValue("limit", size);
        params.addValue("offset", page * size);
        // Execute query và map từng row DB thành object AdminUserResponse
        List<AdminUserResponse> users = jdbcTemplate.query(
                userSql.toString(),
                params,
                (rs, rowNum) -> AdminUserResponse.builder()
                        .id(rs.getString("id"))
                        .email(rs.getString("email"))
                        .keycloakUid(rs.getString("keycloak_uid"))
                        .userType(rs.getString("user_type"))
                        .isActive(rs.getBoolean("is_active"))
                        .createdAt(toLocalDateTime(rs.getTimestamp("created_at")))
                        .updatedAt(toLocalDateTime(rs.getTimestamp("updated_at")))
                        .roles(new LinkedHashSet<>())
                        .build()
        );
        // Nếu không có user nào thì return luôn
        if (users.isEmpty()) {
            return users;
        }
        // Map dùng để lookup user theo userId cực nhanh
        // Key   = userId
        // Value = AdminUserResponse
        Map<String, AdminUserResponse> userMap = new LinkedHashMap<>();
        // Danh sách userId dùng để query roles
        List<String> userIds = new ArrayList<>();
        for (AdminUserResponse user : users) {
            userMap.put(user.getId(), user);
            userIds.add(user.getId());
        }
        // SQL lấy role của toàn bộ users
        String roleSql = """
            SELECT
                ur.user_id,
                r.role_name
            FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            WHERE ur.user_id IN (:userIds)
        """;
        // Params cho role query
        MapSqlParameterSource roleParams = new MapSqlParameterSource();
        // Truyền list userIds vào SQL
        roleParams.addValue("userIds", userIds);
        // Query roles và gắn role vào từng user
        jdbcTemplate.query(roleSql, roleParams, rs -> {
            String userId = rs.getString("user_id");
            String roleNameResult = rs.getString("role_name");

            AdminUserResponse user = userMap.get(userId);
            if (user != null && roleNameResult != null) {
                user.getRoles().add(roleNameResult);
            }
        });

        return users;
    }

    public long countUsers(
            String email,
            String userType,
            Boolean isActive,
            String roleName
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(DISTINCT u.id)
            FROM users u
            LEFT JOIN user_roles ur ON u.id = ur.user_id
            LEFT JOIN roles r ON ur.role_id = r.id
            WHERE 1 = 1
        """);

        appendFilters(countSql, params, email, userType, isActive, roleName);

        Long total = jdbcTemplate.queryForObject(
                countSql.toString(),
                params,
                Long.class
        );

        return total == null ? 0 : total;
    }

    private void appendFilters(
            StringBuilder sql,
            MapSqlParameterSource params,
            String email,
            String userType,
            Boolean isActive,
            String roleName
    ) {
        if (email != null && !email.trim().isEmpty()) {
            sql.append(" AND LOWER(u.email) LIKE LOWER(:email)");
            params.addValue("email", "%" + email.trim() + "%");
        }

        if (userType != null && !userType.trim().isEmpty()) {
            sql.append(" AND u.user_type = :userType");
            params.addValue("userType", userType.trim());
        }

        if (isActive != null) {
            sql.append(" AND u.is_active = :isActive");
            params.addValue("isActive", isActive);
        }

        if (roleName != null && !roleName.trim().isEmpty()) {
            sql.append(" AND LOWER(r.role_name) = LOWER(:roleName)");
            params.addValue("roleName", roleName.trim());
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
