package org.User.command.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
@Data
@AllArgsConstructor
public class CreatePermissionCommand {
    @TargetAggregateIdentifier
    private String id;
    private String permissionName;
    private String description;
}
