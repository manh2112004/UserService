package org.User.command.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserStatusCommand {
    @TargetAggregateIdentifier
    private String userId;
    private boolean isActive;
}
