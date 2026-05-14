package org.User.command.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignPermissionToRoleCommand {
    @TargetAggregateIdentifier
    private String roleId;
    private List<String> permissionIds;
}
