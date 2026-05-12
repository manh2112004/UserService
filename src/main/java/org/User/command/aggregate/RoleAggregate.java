package org.User.command.aggregate;

import org.User.command.command.CreateRoleCommand;
import org.User.command.event.RoleCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class RoleAggregate {
    @AggregateIdentifier
    private String roleName; // Dùng roleName làm ID định danh cho Aggregate

    protected RoleAggregate() {}

    @CommandHandler
    public RoleAggregate(CreateRoleCommand command) {
        AggregateLifecycle.apply(new RoleCreatedEvent(
                command.getRoleName(),
                command.getDescription(),
                command.getPermissionIds()
        ));
    }
    @EventSourcingHandler
    public void on(RoleCreatedEvent event) {
        this.roleName = event.getRoleName();
    }
}
