package org.User.command.aggregate;

import org.User.command.command.AssignPermissionToRoleCommand;
import org.User.command.command.CreateRoleCommand;
import org.User.command.command.DeleteRoleCommand;
import org.User.command.command.UpdateRoleCommand;
import org.User.command.event.PermissionAssignedToRoleEvent;
import org.User.command.event.RoleCreatedEvent;
import org.User.command.event.RoleDeletedEvent;
import org.User.command.event.RoleUpdatedEvent;
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
    @CommandHandler
    public void handle(AssignPermissionToRoleCommand command,RoleService roleService) {
        roleService.assignPermissionsToRole(command.getRoleId(), command.getPermissionIds());
        AggregateLifecycle.apply(new PermissionAssignedToRoleEvent(
                command.getRoleId(),
                command.getPermissionIds()
        ));
    }
    @CommandHandler
    public void handle(UpdateRoleCommand command) {
        AggregateLifecycle.apply(
                RoleUpdatedEvent.builder()
                        .id(command.getId())
                        .roleName(command.getRoleName())
                        .description(command.getDescription())
                        .build()
        );
    }
    @CommandHandler
    public String handle(DeleteRoleCommand command, RoleService roleService) {
        roleService.deleteRoleInKeycloak(command.getRoleId());
        AggregateLifecycle.apply(new RoleDeletedEvent(command.getRoleId()));
        return "Xóa role thành công";
    }

    @EventSourcingHandler
    public void on(RoleCreatedEvent event) {
        this.id = event.getId();
        this.roleName = event.getRoleName();
    }
    @EventSourcingHandler
    public void on(PermissionAssignedToRoleEvent event) {
        this.id = event.getRoleId();
        this.permissionIds = event.getPermissionIds();
    }

    @EventSourcingHandler
    public void on(RoleDeletedEvent event) {
        this.id = event.getRoleId();
        AggregateLifecycle.markDeleted();
    }
    @EventSourcingHandler
    public void on(RoleUpdatedEvent event) {
        this.id = event.getId();
        this.roleName = event.getRoleName();
        this.description = event.getDescription();
    }
}
