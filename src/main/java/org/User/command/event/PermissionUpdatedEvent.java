package org.User.command.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdatedEvent {
    private String id;
    private String permissionName;
    private String description;
}
