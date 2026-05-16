package org.User.command.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role,String> {
    Set<Role> findAllByRoleNameIn(Collection<String> roleNames);
    Optional<Role> findByRoleName(String roleName);
    boolean existsByRoleName(String roleName);
}
