package org.User.command.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class RefreshTokenCommand {
    @TargetAggregateIdentifier
    String userId;
}
