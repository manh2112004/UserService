package org.User.command.aggregate;

import lombok.NoArgsConstructor;
import org.User.command.command.CreatePermissionCommand;
import org.User.command.command.DeletePermissionCommand;
import org.User.command.command.UpdatePermissionCommand;
import org.User.command.event.PermissionCreatedEvent;
import org.User.command.event.PermissionDeletedEvent;
import org.User.command.event.PermissionUpdatedEvent;
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

    @CommandHandler
    public String handle(UpdatePermissionCommand command, RoleService permissionService) {
        permissionService.updatePermissionInKeycloak(
                this.permissionName,
                command.getPermissionName(),
                command.getDescription()
        );

        AggregateLifecycle.apply(
                PermissionUpdatedEvent.builder()
                        .id(command.getId())
                        .permissionName(command.getPermissionName())
                        .description(command.getDescription())
                        .build()
        );

        return "Cập nhật permission thành công";
    }

    @CommandHandler
    public String handle(DeletePermissionCommand command, RoleService permissionService) {
        permissionService.deletePermissionInKeycloak(this.permissionName);
        AggregateLifecycle.apply(new PermissionDeletedEvent(command.getId()));
        return "Xóa permission thành công";
    }

    @EventSourcingHandler
    public void on(PermissionCreatedEvent event) {
        this.id = event.getId(); // Gán ID từ event vào field của Aggregate
        this.permissionName = event.getPermissionName();
        this.description = event.getDescription();
    }

    @EventSourcingHandler
    public void on(PermissionUpdatedEvent event) {
        this.id = event.getId();
        this.permissionName = event.getPermissionName();
        this.description = event.getDescription();
    }

    @EventSourcingHandler
    public void on(PermissionDeletedEvent event) {
        this.id = event.getId();
        AggregateLifecycle.markDeleted();
    }
}
