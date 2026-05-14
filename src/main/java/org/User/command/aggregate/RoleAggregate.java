package org.User.command.aggregate;

import org.User.command.command.AssignRoleToUserCommand;
import org.User.command.command.CreateRoleCommand;
import org.User.command.command.UpdateRoleCommand;
import org.User.command.event.RoleCreatedEvent;
import org.User.command.event.RoleUpdatedEvent;
import org.User.command.event.RolesAssignedToUserEvent;
import org.User.command.service.RoleService;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.List;

@Aggregate
public class RoleAggregate {
    @AggregateIdentifier
    private String id;
    private String roleName;
    private String description;
    private List<String> permissionIds;
    public RoleAggregate() {} // Axon requirement

    @CommandHandler
    public RoleAggregate(CreateRoleCommand command, RoleService roleService) {
        // Gọi Keycloak Service để tạo Role và gán các permissions con
        // Kết quả trả về là ID chính xác từ Keycloak
        String keycloakRoleId = roleService.createRoleInKeycloak(command);
        AggregateLifecycle.apply(new RoleCreatedEvent(
                keycloakRoleId,
                command.getRoleName(),
                command.getDescription(),
                command.getPermissionNames()
        ));
    }
    @EventSourcingHandler
    public void on(RoleCreatedEvent event) {
        this.id = event.getId();
        this.roleName = event.getRoleName();
    }
}
