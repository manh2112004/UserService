package org.User.command.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PermissionCreatedEvent {
    private String id;
    private String permissionName;
    private String description;
}
