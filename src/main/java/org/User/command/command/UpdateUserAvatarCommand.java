package org.User.command.command;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class UpdateUserAvatarCommand {
    @TargetAggregateIdentifier
    private String userId;
    private String avatarUrl;
}
