package org.User.command.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUpdatedEvent {
    private String id;
    private String roleName;
    private String description;
    private List<String> permissionNames;
}
