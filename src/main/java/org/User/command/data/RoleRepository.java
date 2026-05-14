package org.User.command.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role,String> {
    boolean existsByRoleName(String roleName);
    Set<Role> findAllByRoleNameIn(List<String> roleNames);
}
