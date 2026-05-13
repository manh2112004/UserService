package org.User.command.aggregate;

import lombok.NoArgsConstructor;
import org.User.command.command.CreatePermissionCommand;
import org.User.command.event.PermissionCreatedEvent;
import org.User.command.service.RoleService;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@NoArgsConstructor
public class PermissionAggregate {
    @AggregateIdentifier
    private String id;
    private String permissionName;
    private String description;
    @CommandHandler
    public PermissionAggregate(CreatePermissionCommand command, RoleService permissionService) {
        String keycloakId = permissionService.createPermissionInKeycloak(
                command.getPermissionName(),
                command.getDescription()
        );
        if (keycloakId == null) {
            throw new IllegalStateException("Không thể tạo Permission trên Keycloak!");
        }
        AggregateLifecycle.apply(new PermissionCreatedEvent(
                keycloakId,
                command.getPermissionName(),
                command.getDescription()
        ));
    }
    @EventSourcingHandler
    public void on(PermissionCreatedEvent event) {
        this.id = event.getId(); // Gán ID từ event vào field của Aggregate
        this.permissionName = event.getPermissionName();
        this.description = event.getDescription();
    }
}
