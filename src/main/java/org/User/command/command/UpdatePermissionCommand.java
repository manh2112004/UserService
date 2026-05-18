package org.User.command.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionCommand {
    @TargetAggregateIdentifier
    private String id;
    private String permissionName;
    private String description;
}
