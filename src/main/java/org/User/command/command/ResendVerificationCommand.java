package org.User.command.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationCommand {
    @TargetAggregateIdentifier
    private String userId;
    private String email;
}
