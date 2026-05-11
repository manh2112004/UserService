package org.User.command.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserCommand {
    @TargetAggregateIdentifier
    private String userId;
    private String username;
    private String phoneNumber;
    private String email;
}
