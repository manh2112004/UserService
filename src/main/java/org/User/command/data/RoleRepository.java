package org.User.command.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,String> {
    boolean existsByRoleName(String roleName);
}
