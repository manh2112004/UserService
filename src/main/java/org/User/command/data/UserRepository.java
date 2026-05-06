package org.User.command.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {
    // Tìm kiếm người dùng theo Email (hữu ích cho việc kiểm tra trùng lặp hoặc đăng nhập)
    Optional<User> findByEmail(String email);

    // Tìm kiếm theo username
    Optional<User> findByUsername(String username);

    // Kiểm tra xem email đã tồn tại hay chưa
    boolean existsByEmail(String email);

    // Tìm kiếm theo Keycloak UID (trong trường hợp bạn cần truy vấn ngược)
    Optional<User> findByKeycloakUid(String keycloakUid);
}
