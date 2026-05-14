package org.User.command.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission,String> {
    Set<Permission> findAllByPermissionNameIn(List<String> permissionNames);
    boolean existsByPermissionName(String permissionName);
}
