package org.User.command.command;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserCommand {
    @TargetAggregateIdentifier
    private String userId; // keycloakUserId sẽ truyền vào đây
    private String fullName;
    private String email;
    private String userType;

}
