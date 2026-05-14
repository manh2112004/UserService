package org.User.command.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignRoleToUserCommand {
    @TargetAggregateIdentifier
    private String userId; // ID của User (Aggregate Identifier)
    private List<String> roleNames;
}
